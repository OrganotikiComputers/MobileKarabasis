package organotiki.mobile.mobilestreet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.sf.andpdf.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmQuery;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Company;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLine;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;

/**
 * Created by Thanasis on 18/7/2016.
 */
public class CheckOutReturns extends AppCompatActivity implements Communicator {
    Double ExtraChargeManage;
    Double ExtraChargeTransfer;
    Realm realm;
    GlobalVar gVar;
    ArrayList<InvoiceLine> lines;
    DecimalFormat decim = new DecimalFormat("0.00");
    DecimalFormat formatter = new DecimalFormat("#,###,##0.00");
    ListViewReturnsCheckOutAdapter myListAdapter;
    VolleyRequests request;
    AlertDialog mAlertDialog;
    String[] emaillist;
    private long mLastClickTime=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_returns);

        realm = Realm.getDefaultInstance();
        gVar = realm.where(GlobalVar.class).findFirst();

        request = new VolleyRequests();

        Toolbar toolbar = (Toolbar) findViewById(R.id.checkOutBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        lines = new ArrayList<>();
        lines.addAll(realm.where(InvoiceLine.class).equalTo("myInvoice.ID",gVar.getMyInvoice().getID()).findAll());

        FillCheckOut();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.complete_check_out:
                try {
//                    request.getCustomerEmails(CheckOutReturns.this, gVar.getMyInvoice().getMyCustomer());
                    doSend();
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                return false;
            case android.R.id.home:
                finish();
                return true;

        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.check_out_menu, menu);
        if (gVar.getMyInvoice().isFinal()){
            MenuItem item = menu.findItem(R.id.complete_check_out);
            item.setVisible(false);
            //menu.getItem(0).setEnabled(false); // here pass the index of save menu item
        }
        return true;
    }

    private void doSend() {
        try {
//            String message = gVar.getMyInvoice().getMyAddress().getEmail()== null || gVar.getMyInvoice().getMyAddress().getEmail().equals("")?"Σε ποιο e-mail θέλετε να σταλεί η είσπραξη;":"Η είσπραξη θα σταλεί στο : "+gVar.getMyInvoice().getMyAddress().getEmail()+"\nΑν θέλετε να σταλεί και κάπου αλλού προσθέστε το λογαριασμό e-mail.";
            String message = getString(R.string.setReturnsQuestion);
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CheckOutReturns.this);

            alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        if (SystemClock.elapsedRealtime() - mLastClickTime > 1500) {
                            mLastClickTime = SystemClock.elapsedRealtime();
                            request.setInvoice(CheckOutReturns.this, gVar.getMyInvoice().getID());
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
            alertDialog.setNegativeButton(R.string.no, null);
//        alertDialog.setNeutralButton("Άκυρο",null);
            alertDialog.setMessage(message);
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

    private void FillCheckOut() {
        String str;
        String str2;
        String str3 = "";
        try {
            this.myListAdapter = new ListViewReturnsCheckOutAdapter(this, this.lines);
            ((ListView) findViewById(R.id.listViewCheckOutItems)).setAdapter(this.myListAdapter);
            this.myListAdapter.sortAdapter();
            Customer myCustomer = this.gVar.getMyInvoice().getMyCustomer();
            Address myAddress = this.gVar.getMyInvoice().getMyAddress();
            ((TextView) findViewById(R.id.textView_customer_code)).setText("Κωδικός: " + myCustomer.getCode());
            ((TextView) findViewById(R.id.textView_customer_name)).setText("Όνομα: " + myCustomer.getName());
            ((TextView) findViewById(R.id.textView_customer_tin)).setText("ΑΦΜ: " + myCustomer.getTIN());
            ((TextView) findViewById(R.id.textView_customer_address)).setText("Διεύθυνση: " + myAddress.getStreet());
            ((TextView) findViewById(R.id.textView_customer_city)).setText("Πόλη: " + myAddress.getCity());
            ((TextView) findViewById(R.id.textView_customer_postalcode)).setText("Τ.Κ.: " + myAddress.getPostalCode());
            StringBuilder sb = new StringBuilder();
            sb.append("");
            if (myAddress.getPhone1() == null || myAddress.getPhone1().isEmpty()) {
                str = "";
            } else {
                str = myAddress.getPhone1().replaceFirst("\\s+$", "") + " - ";
            }
            sb.append(str);
            String sb2 = sb.toString();
            StringBuilder sb3 = new StringBuilder();
            sb3.append(sb2);
            if (myAddress.getPhone2() == null || myAddress.getPhone2().isEmpty()) {
                str2 = "";
            } else {
                str2 = myAddress.getPhone2().replaceFirst("\\s+$", "") + " - ";
            }
            sb3.append(str2);
            String sb4 = sb3.toString();
            if (sb4.length() > 3) {
                str3 = sb4.substring(0, sb4.length() - 3);
            }
            ((TextView) findViewById(R.id.textView_customer_phone)).setText("Τηλέφωνα: " + str3);
            ((TextView) findViewById(R.id.textView_customer_mobile)).setText("Κινητό: " + myAddress.getMobile());
            ((TextView) findViewById(R.id.textView_customer_email)).setText("E-mail: " + myAddress.getEmail());
            ((TextView) findViewById(R.id.textView_Entry_Type)).setText(getString(R.string.defaultInvoiceType) + " - " + getString(R.string.defaultPaymentTerm));
            ((TextView) findViewById(R.id.textView_subtotal_AEY)).setText(this.formatter.format(this.gVar.getMyInvoice().getTotalAEY()));
            ((TextView) findViewById(R.id.textView_subtotal_ASY)).setText(this.formatter.format(this.gVar.getMyInvoice().getTotalASY()));
            ((TextView) findViewById(R.id.textView_subtotal_FEY)).setText(this.formatter.format(this.gVar.getMyInvoice().getTotalFEY()));
            ((TextView) findViewById(R.id.textView_subtotal_FSY)).setText(this.formatter.format(this.gVar.getMyInvoice().getTotalFSY()));
            this.ExtraChargeTransfer = CalculateExtraChargeTransfer();
            ((TextView) findViewById(R.id.textView_extraChargeTransfer)).setText(this.formatter.format(this.ExtraChargeTransfer));
            TextView textView = (TextView) findViewById(R.id.textView_extraChargeManagementCost);
            this.ExtraChargeManage = 0.0;
            Iterator<InvoiceLine> it = this.lines.iterator();
            while (it.hasNext()) {
                this.ExtraChargeManage = Double.valueOf(this.ExtraChargeManage.doubleValue() + it.next().getManageCost().doubleValue());
            }
            textView.setText(this.formatter.format(this.ExtraChargeManage));
            ((TextView) findViewById(R.id.textView_total)).setText("Σύνολο: " + this.formatter.format((this.gVar.getMyInvoice().getTotal().doubleValue() - this.ExtraChargeTransfer.doubleValue()) - this.ExtraChargeManage.doubleValue()) + "€");
            ((TextView) findViewById(R.id.textView_generalNotes)).setText(getString(R.string.generalNotes) + ": " + this.gVar.getMyInvoice().getNotes());
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private Double CalculateExtraChargeTransfer() {
        Double chargeValue = 0.0;
        HashMap<String, List<InvoiceLine>> linesMap = new HashMap<>();
        Log.d("asdfg", "lines count:" + this.lines.size());
        Iterator<InvoiceLine> it = this.lines.iterator();
        while (it.hasNext()) {
            InvoiceLine line = it.next();
            Log.d("asdfg", "DocID:" + line.getDocID() + ",DocValue:" + line.getDocValue() + ",ChargePapi:" + line.getChargePapi() + ",isExtraCharge:" + line.isExtraCharge() + ",ExtraChargeValue:" + line.getExtraChargeValue() + ",ExtraChargeLimit:" + line.getExtraChargeLimit());
            if (!linesMap.containsKey(line.getDocID())) {
                List<InvoiceLine> list = new ArrayList<>();
                list.add(line);
                linesMap.put(line.getDocID(), list);
            } else {
                linesMap.get(line.getDocID()).add(line);
            }
        }
        for (Map.Entry<String, List<InvoiceLine>> entry : linesMap.entrySet()) {
            String key = entry.getKey();
            List<InvoiceLine> docLines = entry.getValue();
            if (docLines.get(0).isExtraCharge() && docLines.get(0).getChargePapi().doubleValue() == 0.0) {
                Double returnValue = 0.0;
                for (InvoiceLine line2 : docLines) {
                    returnValue = Double.valueOf(returnValue.doubleValue() + line2.getValue().doubleValue());
                }
                Log.d("asdfg", "DocValue - returnValue:" + (docLines.get(0).getDocValue().doubleValue() - returnValue.doubleValue()));
                if (docLines.get(0).getDocValue().doubleValue() - returnValue.doubleValue() < docLines.get(0).getExtraChargeLimit().doubleValue() && docLines.get(0).getDocValue().doubleValue() - returnValue.doubleValue() > 0.0) {
                    chargeValue = Double.valueOf(chargeValue.doubleValue() + docLines.get(0).getExtraChargeValue().doubleValue());
                }
            }
        }
        return chargeValue;
    }


    @Override
    public void respondCustomerSearch(Customer customer, Address address) {

    }

    @Override
    public void respondInvoiceType() {

    }

    @Override
    public void respondPaymentTerm() {

    }


    @Override
    public void respondCustomerCreate() {

    }

    @Override
    public void respondVolleyRequestFinished(Integer position, JSONObject jsonArray) {
        Log.d("asdfg", "position = "+position);
        switch (position) {
            case 0:
                try {
                    JSONArray array = jsonArray.getJSONArray("GetCustomerEmailsResult") ;
                    int l = array.length();
                    emaillist = new String[l];
                    if (l>0) {
                        for (int i=0;i<l; i++){
                            emaillist[i] = String.valueOf(array.get(i));
                        }
                    }
                    doSend();
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 1:
                /*try {
                    Log.d("asdfg", jsonArray.toString());
                    isSaved = jsonArray.getInt("UpdateCustomerResult") == 1;
                    if (isSaved) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                gVar.getMyInvoice().getMyCustomer().setNew(false);
                            }
                        });
                        Toast.makeText(CheckOutReturns.this, "Ο πελάτης ενημερώθηκε επιτυχώς.", Toast.LENGTH_LONG).show();
                        request.setInvoice(CheckOutReturns.this, gVar.getMyInvoice().getID());
                    } else {
                        Toast.makeText(CheckOutReturns.this, "Ο πελάτης δεν ενημερώθηκε. Προσπαθήστε ξανά.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }*/
                break;
            case 2:
                try {
//                    Log.d("asdfg", jsonArray.toString());
//                    isSaved = jsonArray.getInt("SendCustomerDetailsEmailResult") == 1;
                    if (gVar.getMyInvoice().isFinal()) {
//                        Toast.makeText(CheckOutReturns.this, "Το e-mail στάλθηκε επιτυχώς.", Toast.LENGTH_LONG).show();
                        mAlertDialog.dismiss();
                        Intent i = new Intent(CheckOutReturns.this,MainScreen.class);
                        startActivity(i);
                    } /*else {
                        Toast.makeText(CheckOutReturns.this, "Το e-mail δεν στάλθηκε. Προσπαθήστε ξανά.", Toast.LENGTH_LONG).show();
                    }*/
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
        }
    }

    @Override
    public void respondDate(Integer position, int year, int month, int day) {

    }

    @Override
    public void respondCompanySite() {

    }

    @Override
    public void respondRecentPurchases(ArrayList<InvoiceLineSimple> sLines) {

    }


    //region old mAdapter
    /*private class MyListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (lines != null && lines.size() != 0) {
                return lines.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return lines.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            try {
                final ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    LayoutInflater inflater = CheckOutReturns.this.getLayoutInflater();
                    convertView = inflater.inflate(R.layout.listview_returns_checkout, null);
//                    convertView.setClickable(true);
//                    convertView.setFocusable(true);
                    final View finalConvertView = convertView;
                    convertView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(finalConvertView.getWindowToken(), 0);
                        }

                    });

                    holder.LastDate = (TextView) convertView.findViewById(R.id.textView_date);
                    holder.LastCompany = (TextView) convertView.findViewById(R.id.textView_company);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentManager manager = getFragmentManager();
                            ItemDetailsFragment frag = new ItemDetailsFragment();
                            frag.setLine(lines.get(holder.ref));
                            frag.isReadOnly(true);
                            frag.show(manager, "Item Details Fragment");
                        }
                    });
                    holder.Image = (ImageView) convertView.findViewById(R.id.imageView_image);
                    holder.Code = (TextView) convertView.findViewById(R.id.cusCode);
                    holder.Description = (TextView) convertView.findViewById(R.id.description);
                    holder.Quantity = (TextView) convertView.findViewById(R.id.quantity);
                    holder.Price = (TextView) convertView.findViewById(R.id.price);
                    holder.Value = (TextView) convertView.findViewById(R.id.value);
                    holder.Guarantee = (SwitchCompat) convertView.findViewById(R.id.switch_guarantee);
                    holder.Guarantee.setEnabled(false);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.ref = position;
                holder.ID = lines.get(position).getID();

                *//*String filepath = Environment.getExternalStorageDirectory().getPath() + "/Pictures/MobileStreet/logo.jpg";
                File file = new File(filepath);
                if(file.exists()){
                    Log.d("asdfg", filepath);
                    holder.Image.setImageURI(Uri.parse(filepath));
                }else{
                    holder.Image.setImageResource(R.drawable.logoo);
                    Log.d("asdfg", filepath+" does not exist");
                }
                holder.Image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            FragmentManager manager = getFragmentManager();
                            ImageFragment frag = new ImageFragment();
                            frag.setImageCode(lines.get(position).getMyItem().getCode());
                            frag.show(manager, "Image Fragment");
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });*//*
                holder.Image.setImageResource(R.drawable.logo);
                holder.Code.setText(lines.get(position).getMyItem().getCode());
                holder.Description.setText(lines.get(position).getMyItem().getDescription());
                holder.Quantity.setText(lines.get(position).getQuantityText());
                holder.Price.setText(lines.get(position).getPriceText());
                holder.Value.setText(lines.get(position).getValueText());
                String lastdatetext = lines.get(position).getLastUpdate()+(lines.get(position).getOverdue()!=2?"":" - "+lines.get(position).getTypeCode());
                holder.LastDate.setText(lastdatetext);
                holder.LastCompany.setText(lines.get(position).getLastCompany()==null?"":lines.get(position).getLastCompany().getDescription());
                holder.Guarantee.setChecked(lines.get(position).isGuarantee());
                setBackground(convertView, lines.get(position).isGuarantee(),lines.get(position).getLastCompany());
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
            return convertView;
        }

        private class ViewHolder {
            String ID;
            ImageView Image;
            TextView Code;
            TextView Description;
            TextView Price;
            TextView Quantity;
            TextView Value;
            TextView LastDate;
            TextView LastCompany;
            SwitchCompat Guarantee;
            int ref;
        }
    }


    public void setBackground(View view, Boolean isChecked, Company company){
        if (isChecked) {
            view.setBackgroundResource(R.color.colorRed);
        } else if (company == null) {
            view.setBackgroundResource(android.R.color.transparent);
        } else if (company.getID().equals("1")) {
            view.setBackgroundResource(R.color.colorA);
        } else {
            view.setBackgroundResource(R.color.colorF);
        }
    }*/
    //endregion

    public class ListViewReturnsCheckOutAdapter extends ArrayAdapter<InvoiceLine> {

        Realm realm;
        GlobalVar gVar;

        private ArrayList<InvoiceLine> lines;
        private Activity activity;

        public class ViewHolder {
            String ID;
            ImageView Image;
            TextView Code;
            TextView Description;
            TextView LastQuantity;
            TextView Price;
            TextView Quantity;
            TextView Value;
            TextView LastDate;
            TextView LastCompany;
            TextView ManagementCost;
            AppCompatCheckBox Guarantee;
            AppCompatCheckBox Script;
            int ref;
        }

        ListViewReturnsCheckOutAdapter(Activity activity, ArrayList<InvoiceLine> invoiceLines) {
            super(activity, 0, invoiceLines);
            this.lines = invoiceLines;
            this.activity = activity;
            Log.d("asdfg", "in-" + lines.size());
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            try {
                // Get the data item for this position
                final InvoiceLine invoiceLine = lines.get(position);
                realm = Realm.getDefaultInstance();
                gVar = realm.where(GlobalVar.class).findFirst();

                // Check if an existing view is being reused, otherwise inflate the view
                final ViewHolder viewHolder;
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(R.layout.listview_returns_checkout, parent, false);
                    viewHolder.Image = (ImageView) convertView.findViewById(R.id.imageView_image);
                    viewHolder.Code = (TextView) convertView.findViewById(R.id.code);
                    viewHolder.Description = (TextView) convertView.findViewById(R.id.description);
                    viewHolder.ManagementCost = (TextView) convertView.findViewById(R.id.managementCost);
                    viewHolder.LastQuantity = (TextView) convertView.findViewById(R.id.last_quantity);
                    viewHolder.Price = (TextView) convertView.findViewById(R.id.price);
                    viewHolder.Quantity = (TextView) convertView.findViewById(R.id.quantity);
                    viewHolder.Value = (TextView) convertView.findViewById(R.id.value);
                    viewHolder.LastDate = (TextView) convertView.findViewById(R.id.textView_date);
                    viewHolder.LastCompany = (TextView) convertView.findViewById(R.id.textView_company);
                    viewHolder.Guarantee = (AppCompatCheckBox) convertView.findViewById(R.id.checkBox_guarantee);
                    viewHolder.Guarantee.setEnabled(false);
                    viewHolder.Script = (AppCompatCheckBox) convertView.findViewById(R.id.checkBox_script);
                    viewHolder.Script.setEnabled(false);

                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentManager manager = activity.getFragmentManager();
                            ItemDetailsFragment frag = new ItemDetailsFragment();
                            frag.setLine(lines.get(viewHolder.ref));
                            frag.show(manager, "Items Details Fragment");
                        }
                    });

                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                viewHolder.ref = position;
                viewHolder.ID = invoiceLine.getMyItem().getID();
                // Populate the data into the template view using the data object
                viewHolder.Code.setText(invoiceLine.getMyItem().getCode());

                viewHolder.Image.setImageResource(R.drawable.logo);
                viewHolder.Description.setText(invoiceLine.getMyItem().getDescription());
                viewHolder.LastQuantity.setText(invoiceLine.getLastQuantityText());
                viewHolder.Price.setText(invoiceLine.getPriceText());
                viewHolder.Quantity.setText(invoiceLine.getQuantityText());
                viewHolder.Value.setText(invoiceLine.getValueText());
                viewHolder.ManagementCost.setText(invoiceLine.getManageCostText());
                String lastdatetext = invoiceLine.getLastDate()+(invoiceLine.getOverdue()==1?" ΠΕΡΑΝ ΤΟΥ ΕΠΙΤΡΕΠΤΟΥ":"");
                viewHolder.LastDate.setText(lastdatetext);
                viewHolder.LastCompany.setText(invoiceLine.getLastCompany() == null ? "" : invoiceLine.getLastCompany().getDescription()+"-"+invoiceLine.getTypeCode()+"/"+invoiceLine.getDosCode()+"-"+invoiceLine.getDocNumber());
//            Log.d("asdfg", invoiceLine.getLastUpdate() + " " + (invoiceLine.getLastCompany()==null?"":invoiceLine.getLastCompany().getDescription()));


                viewHolder.Guarantee.setChecked(invoiceLine.isGuarantee());
                viewHolder.Script.setChecked(invoiceLine.isFromCustomer());
                setBackground(convertView, viewHolder, viewHolder.Guarantee.isChecked(), invoiceLine.getMyInvoice() == null ? null : invoiceLine.getLastCompany());

//            sortAdapter();
                return convertView;
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
            return null;
        }

        private void setBackground(View view, ViewHolder holder, Boolean isChecked, Company company) {
            if (isChecked) {
                view.setBackgroundResource(R.color.colorRed);
                setTextColor(false, holder);
            } else if (company == null) {
                view.setBackgroundResource(android.R.color.transparent);
                setTextColor(false, holder);
            } else if (company.getInAppID().equals("1")) {
                view.setBackgroundResource(R.color.colorA);
                setTextColor(true, holder);
            } else {
                view.setBackgroundResource(R.color.colorF);
                setTextColor(true, holder);
            }
        }

        private void setTextColor(boolean isBlack, ViewHolder holder){
            if (isBlack){
                holder.Code.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
                holder.Description.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
                holder.LastQuantity.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
                holder.Price.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
                holder.Quantity.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
                holder.ManagementCost.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
                holder.Value.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
                holder.LastDate.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
                holder.LastCompany.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            /*ContextThemeWrapper newContext = new ContextThemeWrapper(activity, R.style.BlackCheckBox);
            holder.Guarantee = new AppCompatCheckBox(newContext);*/
            /*holder.Guarantee.setAlpha(1);*/
            }else{
                holder.Code.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                holder.Description.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                holder.LastQuantity.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                holder.Price.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                holder.Quantity.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                holder.ManagementCost.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                holder.Value.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                holder.LastDate.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                holder.LastCompany.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
            }

        }

        void sortAdapter() {
            myListAdapter.sort(new Comparator<InvoiceLine>() {
                @Override
                public int compare(InvoiceLine o1, InvoiceLine o2) {
                    try {
                        int value1 = o1/*.getMyInvoiceType()*/.isGuarantee() != o2/*.getMyInvoiceType()*/.isGuarantee() ? o1/*.getMyInvoiceType()*/.isGuarantee() ? 1 : -1 : 0;
//                    Log.d("asdfg", "value1 = "+String.valueOf(value1));
                        if (value1 == 0) {
//                        Log.d("asdfg","o1.company = "+o1.getLastCompany()+" o2.company = "+o2.getLastCompany());
                            if (o1.getLastCompany() != null && o2.getLastCompany() != null) {
                                int value2 = o1.getLastCompany().getInAppID().compareTo(o2.getLastCompany().getInAppID());
//                            Log.d("asdfg","value2 = "+ value2);
                                if (value2 == 0) {
//                                Log.d("asdfg", String.valueOf(o1.getTypeCode().compareTo(o2.getTypeCode())));
                                    return o1.getTypeCode().compareTo(o2.getTypeCode());
                                } else {
//                                Log.d("asdfg","hi5");
                                    return value2;
                                }
                            }
                        }
                        return value1;
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                        return 0;
                    }
                }
            });
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean checkIfExists(String itemID,String invoiceID){

        RealmQuery<InvoiceLine> query = realm.where(InvoiceLine.class)
                .equalTo("myItem.ID", itemID).equalTo("myInvoice.ID", invoiceID);

        return query.count() != 0;
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}