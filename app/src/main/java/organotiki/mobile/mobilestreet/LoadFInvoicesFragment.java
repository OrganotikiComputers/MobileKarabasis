package organotiki.mobile.mobilestreet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

/**
 * Created by Thanasis on 25/7/2016.
 */
public class LoadFInvoicesFragment extends DialogFragment {

    TextView title, cashTotal, securitiesTotal, expensesTotal, txvSubtotalAEY, txvSubtotalASY, txvSubtotalFEY, txvSubtotalFSY;
    EditText hotel, meals, fuel, misc;
    ListView listView;
    Realm realm;
    GlobalVar gVar;
    Button close;
    boolean isCollections = false;
    Double totalCash;
    DecimalFormat decim = new DecimalFormat("0.00");
    AlertDialog mAlertDialog;

    DecimalFormat formatter = new DecimalFormat("#,###,##0.00");

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_load_finvoices, null);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            RealmResults<FInvoice> sentInvoices = realm.where(FInvoice.class).equalTo("isSent", true).findAll();
            for (final FInvoice sentfInvoice: sentInvoices) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", sentfInvoice.getID()).findAll().deleteAllFromRealm();
                        sentfInvoice.deleteFromRealm();
                    }
                });
            }

            title = (TextView) view.findViewById(R.id.textView_Title);
            cashTotal = (TextView) view.findViewById(R.id.textView_deposit);
            securitiesTotal = (TextView) view.findViewById(R.id.textView_securities_total);
            txvSubtotalAEY = (TextView) view.findViewById(R.id.textView_subtotal_AEY);
            txvSubtotalAEY.setText(decim.format((Double)realm.where(FInvoice.class).sum("Cash1")+ (Double)realm.where(FInvoiceLine.class).equalTo("myCompany.InAppID", "1").sum("Value")));
            txvSubtotalASY = (TextView) view.findViewById(R.id.textView_subtotal_ASY);
            txvSubtotalASY.setText(decim.format(realm.where(FInvoice.class).sum("Cash2")));
            txvSubtotalFEY = (TextView) view.findViewById(R.id.textView_subtotal_FEY);
            txvSubtotalFEY.setText(decim.format((Double)realm.where(FInvoice.class).sum("Cash3")+ (Double)realm.where(FInvoiceLine.class).equalTo("myCompany.InAppID", "2").sum("Value")));
            txvSubtotalFSY = (TextView) view.findViewById(R.id.textView_subtotal_FSY);
            txvSubtotalFSY.setText(decim.format(realm.where(FInvoice.class).sum("Cash4")));
            expensesTotal = (TextView) view.findViewById(R.id.textView_expenses_total);
            expensesTotal.setText(gVar.getMyUser().getTotalExpensesText());
            hotel = (EditText) view.findViewById(R.id.editText_hotel);
            hotel.setText(gVar.getMyUser().getHotelExpensesText());
            hotel.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(final Editable s) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            gVar.getMyUser().setHotelExpensesText(String.valueOf(s));
                        }
                    });
                    expensesTotal.setText(gVar.getMyUser().getTotalExpensesText());
                    cashTotal.setText(getResources().getString(R.string.cashTotal_, String.valueOf(totalCash-gVar.getMyUser().getTotalExpenses())));
                }
            });
            meals = (EditText) view.findViewById(R.id.editText_meals);
            meals.setText(gVar.getMyUser().getMealsExpensesText());
            meals.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(final Editable s) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            gVar.getMyUser().setMealsExpensesText(String.valueOf(s));
                        }
                    });
                    expensesTotal.setText(gVar.getMyUser().getTotalExpensesText());
                    cashTotal.setText(getResources().getString(R.string.cashTotal_, String.valueOf(totalCash-gVar.getMyUser().getTotalExpenses())));
                }
            });
            fuel = (EditText) view.findViewById(R.id.editText_fuel);
            fuel.setText(gVar.getMyUser().getFuelExpensesText());
            fuel.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(final Editable s) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            gVar.getMyUser().setFuelExpensesText(String.valueOf(s));
                        }
                    });
                    expensesTotal.setText(gVar.getMyUser().getTotalExpensesText());
                    cashTotal.setText(getResources().getString(R.string.cashTotal_, String.valueOf(totalCash-gVar.getMyUser().getTotalExpenses())));
                }
            });
            misc = (EditText) view.findViewById(R.id.editText_misc);
            misc.setText(gVar.getMyUser().getMiscExpensesText());
            misc.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(final Editable s) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            gVar.getMyUser().setMiscExpensesText(String.valueOf(s));
                        }
                    });
                    expensesTotal.setText(gVar.getMyUser().getTotalExpensesText());
                    cashTotal.setText(getResources().getString(R.string.cashTotal_, String.valueOf(totalCash-gVar.getMyUser().getTotalExpenses())));
                }
            });
            title.setText(R.string.collections);
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
            ArrayList<FInvoice> invoices = new ArrayList<>();
            invoices.addAll(realm.where(FInvoice.class).findAll());

            totalCash =0.0;
            Double total2 =0.0;
            int l = invoices.size();
            for (int i =0; i<l;i++){
                if (invoices.get(i).isFinal()) {
                    totalCash += invoices.get(i).getCash1()+invoices.get(i).getCash2()+invoices.get(i).getCash3()+invoices.get(i).getCash4();
                    total2 += (Double)realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", invoices.get(i).getID()).sum("Value");
                    Log.d("asdfg", "cash total: "+totalCash+" securities total: "+total2);
                }
            }
            cashTotal.setText(getResources().getString(R.string.cashTotal_, decim.format(totalCash-gVar.getMyUser().getTotalExpenses()).replace(",", ".")));
            securitiesTotal.setText(getResources().getString(R.string.securitiesTotal_, decim.format(total2).replace(",",".")));

            MyListAdapter myListAdapter = new MyListAdapter(this.getActivity(), invoices);
            listView.setAdapter(myListAdapter);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void setCollections(boolean collections) {
        isCollections = collections;
    }

    private class MyListAdapter extends ArrayAdapter {

        private ArrayList<FInvoice> invoices = new ArrayList<>();
        private Activity activity;

        public MyListAdapter(Activity activity, ArrayList<FInvoice> invoices) {
            super(activity, 0, invoices);
            this.invoices = invoices;
            this.activity = activity;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            try {
                final FInvoice invoice = invoices.get(position);
                final ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    LayoutInflater inflater = LayoutInflater.from(getContext());
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
                    Log.d("asdfg", String.valueOf(gVar.getMyFInvoice()));

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.ref = position;
                holder.FInvoiceID = invoice.getID();
                holder.Date.setText(invoice.getDate() + " " + invoice.getTime());
                holder.CusName.setText(invoice.getMyCustomer() == null ? "" : invoice.getMyCustomer().getName());
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
                                            /*RealmResults<FInvoice> invoices = realm.where(FInvoice.class).equalTo("myTransaction.ID", transaction.getID()).findAll();
                                            for (FInvoice invoice:invoices){
                                                realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", invoice.getID()).findAll().deleteAllFromRealm();
                                                invoice.deleteFromRealm();
                                            }*/
                                            realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID",invoice.getID()).findAll().deleteAllFromRealm();
                                            invoice.deleteFromRealm();
                                        }
                                    });
                                    fillInvoiceList();
                                }
                            });

                            alertDialog.setNegativeButton("Όχι", null);
                            alertDialog.setMessage("Θες να την διαγράψεις την παραγγελία;");
                            alertDialog.setTitle(R.string.app_name);
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
                holder.Delete.setImageAlpha(invoice.isFinal() ? 130 : 255);
                convertView.setBackgroundColor(invoice.isFinal() ? ContextCompat.getColor(LoadFInvoicesFragment.this.getActivity(), R.color.colorGreen) : Color.DKGRAY/*getResources().getColor(R.color.colorPrimary)*/);
                holder.Open.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("asdfg", String.valueOf(gVar.getMyFInvoice()));
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        gVar.setMyFInvoice(invoice);
                                    }
                                });
                                Intent intent = new Intent(activity, Collections.class);
                                startActivity(intent);
                    }
                });
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("asdfg", String.valueOf(gVar.getMyFInvoice()));
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                gVar.setMyFInvoice(invoice);
                            }
                        });
                        Intent intent = new Intent(activity, Collections.class);
                        startActivity(intent);
                    }
                });


            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }

            return convertView;
        }

        private class ViewHolder {
            String FInvoiceID;
            TextView Date;
            TextView CusName;
            TextView Total;
            ImageButton Open;
            ImageButton Delete;
            int ref;
        }
    }
}