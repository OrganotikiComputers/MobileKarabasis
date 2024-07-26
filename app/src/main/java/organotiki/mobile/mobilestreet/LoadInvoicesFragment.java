package organotiki.mobile.mobilestreet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.FInvoice;
import organotiki.mobile.mobilestreet.objects.FInvoiceLine;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.Invoice;
import organotiki.mobile.mobilestreet.objects.InvoiceLine;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class LoadInvoicesFragment extends DialogFragment {

    TextView title;
    ListView listView;
    Realm realm;
    GlobalVar gVar;
    Button close;
//    boolean isCollections = false;
    AlertDialog mAlertDialog;

    DecimalFormat formatter = new DecimalFormat("#,###,##0.00");

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_load_invoices, null);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            RealmResults<FInvoice> sentFInvoices = realm.where(FInvoice.class).equalTo("isSent", true).findAll();
            for (final FInvoice sentFInvoice: sentFInvoices) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", sentFInvoice.getID()).findAll().deleteAllFromRealm();
                        sentFInvoice.deleteFromRealm();
                    }
                });
            }

            title = (TextView) view.findViewById(R.id.textView_Title);
            title.setText(R.string.invoices);
            listView = (ListView) view.findViewById(R.id.listView_load_invoices);
            close = (Button) view.findViewById(R.id.button_close);
            close.setTransformationMethod(null);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            fillInvoiceList();
            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    private void fillInvoiceList() {


        try {
            ArrayList<Invoice> invoices = new ArrayList<Invoice>();
            invoices.addAll(realm.where(Invoice.class).findAll());
            MyListAdapter myListAdapter = new MyListAdapter(this.getActivity(),invoices);
            listView.setAdapter(myListAdapter);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

//    public void setCollections(boolean collections) {
//        isCollections = collections;
//    }

    private class MyListAdapter extends ArrayAdapter {

        private ArrayList<Invoice> invoices = new ArrayList<Invoice>();
        private Activity activity;

        public MyListAdapter(Activity activity, ArrayList<Invoice> invoices) {
            super(activity, 0, invoices);
            this.invoices=invoices;
            this.activity= activity;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

                final Invoice invoice= invoices.get(position);
                final ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    LayoutInflater inflater =  LayoutInflater.from(getContext());
                    convertView = inflater.inflate(R.layout.list_view_load_invoices, parent, false);
                    convertView.setClickable(true);
                    convertView.setFocusable(true);

                    final View finalConvertView = convertView;
                    convertView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(finalConvertView.getWindowToken(), 0);
                        }

                    });

                    holder.Date = (TextView) convertView.findViewById(R.id.textView_date);
                    holder.CusName = (TextView) convertView.findViewById(R.id.textView_customer_name);
                    holder.Total = (TextView) convertView.findViewById(R.id.textView_total);
                    holder.Open = (ImageButton) convertView.findViewById(R.id.imageButton_open);
                    holder.Delete = (ImageButton) convertView.findViewById(R.id.imageButton_delete);
                    Log.d("asdfg", String.valueOf(invoice.isFinal()));

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.ref = position;
                holder.TransactionID = invoice.getID();
                holder.Date.setText(invoice.getDate()+" "+invoice.getTime());
                holder.CusName.setText(invoice.getMyCustomer()==null?"":invoice.getMyCustomer().getName());
                holder.Total.setText(formatter.format(invoice.getTotal()));
                holder.Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                                    getActivity());

                            alertDialog.setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            try {
                                                realm.where(InvoiceLine.class).equalTo("myInvoice.ID",invoice.getID()).findAll().deleteAllFromRealm();
                                                invoice.deleteFromRealm();
                                            } catch (Exception e) {
                                                Log.e("asdfg", e.getMessage(), e);
                                            }
                                        }
                                    });
                                    fillInvoiceList();
                                }
                            });

                            alertDialog.setNegativeButton("Όχι", null);
                            alertDialog.setMessage("Θες να την διαγράψεις την παραγγελία;");
                            alertDialog.setTitle("Mobile Store");
                            mAlertDialog = alertDialog.create();
                            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                    positiveButton.setTransformationMethod(null);

                                    Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                                    negativeButton.setTransformationMethod(null);
                                }
                            });

                            mAlertDialog.show();

                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });
                holder.Delete.setEnabled(!invoice.isFinal());
                holder.Delete.setImageAlpha(invoice.isFinal()?130:255);
                convertView.setBackgroundColor(invoice.isFinal()? ContextCompat.getColor(LoadInvoicesFragment.this.getActivity(), R.color.colorGreen):Color.DKGRAY/*getResources().getColor(R.color.colorPrimary)*/);
                holder.Open.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (invoice.isFinal()) {
                            if (invoice.isReturns()) {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        gVar.setMyInvoice(invoice);
                                    }
                                });
                                Intent intent = new Intent(activity, CheckOutReturns.class);
                                startActivity(intent);
                                Log.d("asdfg", "final-negsell");
                            } else {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        gVar.setMyInvoice(invoice);
                                    }
                                });
                                Intent intent = new Intent(activity, CheckOut.class);
                                startActivity(intent);
                                Log.d("asdfg", "final-not negsell");
                            }
                        } else {
                            if (invoice.isReturns()) {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        gVar.setMyInvoice(invoice);
                                    }
                                });
                                Intent intent = new Intent(activity, Returns.class);
                                startActivity(intent);
                                Log.d("asdfg", "not final-negsell");
                            } else {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        gVar.setMyInvoice(invoice);
                                    }
                                });
                                Intent intent = new Intent(activity, NewOrder.class);
                                startActivity(intent);
                                Log.d("asdfg", "not final-not negsell");
                            }
                        }
                    }
                });
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (invoice.isFinal()) {
                            if (invoice.isReturns()) {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        gVar.setMyInvoice(invoice);
                                    }
                                });
                                Intent intent = new Intent(activity, CheckOutReturns.class);
                                startActivity(intent);
                                Log.d("asdfg", "final-negsell");
                            } else {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        gVar.setMyInvoice(invoice);
                                    }
                                });
                                Intent intent = new Intent(activity, CheckOut.class);
                                startActivity(intent);
                                Log.d("asdfg", "final-not negsell");
                            }
                        } else {
                            if (invoice.isReturns()) {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        gVar.setMyInvoice(invoice);
                                    }
                                });
                                Intent intent = new Intent(activity, Returns.class);
                                startActivity(intent);
                                Log.d("asdfg", "not final-negsell");
                            } else {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        gVar.setMyInvoice(invoice);
                                    }
                                });
                                Intent intent = new Intent(activity, NewOrder.class);
                                startActivity(intent);
                                Log.d("asdfg", "not final-not negsell");
                            }
                        }
                    }
                });




            return convertView;
        }

        private class ViewHolder {
            String TransactionID;
            TextView Date;
            TextView CusName;
            TextView Total;
            ImageButton Open;
            ImageButton Delete;
            int ref;
        }
    }
}