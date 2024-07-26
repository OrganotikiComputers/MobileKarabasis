package organotiki.mobile.mobilestreet;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;
import organotiki.mobile.mobilestreet.objects.OnlineReportType;

/**
 * Created by Thanasis on 7/6/2016.
 */
public class OnlineReports extends AppCompatActivity implements Communicator {

    Button start;
    private TextView txvFrom, txvTo, txvCustomerCode, txvCustomerName;
    private int fromYear, fromMonth, fromDay, toYear, toMonth, toDay = 0;
    Customer customer;
    Address address;
    Realm realm;
    GlobalVar gVar;
    private Calendar cal = Calendar.getInstance();
    OnlineReportType report;
    RelativeLayout layoutFrom, layoutTo;
    LinearLayout layoutCompany;
    ShapeDrawable rectShapeDrawable;
    Spinner sprCompany;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_reports);
        try {
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            final Toolbar toolbar = (Toolbar) findViewById(R.id.onlineReportsBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            rectShapeDrawable = new ShapeDrawable(); // pre defined class

            // get paint
            Paint paint = rectShapeDrawable.getPaint();

            // set border color, stroke and stroke width
            paint.setColor(Color.LTGRAY);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10); // you can change the value of 5

            String cid = getIntent().getStringExtra("CustomerID");
            String aid = getIntent().getStringExtra("AddressID");
            if(cid!=null && !cid.equals("")) {
                customer = realm.where(Customer.class).equalTo("ID", cid).findFirst();
                address = realm.where(Address.class).equalTo("ID", aid).findFirst();
            }
            report = realm.where(OnlineReportType.class).equalTo("ID",getIntent().getStringExtra("ReportID")).findFirst();

            if (savedInstanceState != null) {
                fromYear = savedInstanceState.getInt("fromYear");
                fromMonth = savedInstanceState.getInt("fromMonth");
                fromDay = savedInstanceState.getInt("fromDay");
                toYear = savedInstanceState.getInt("toYear");
                toMonth = savedInstanceState.getInt("toMonth");
                toDay = savedInstanceState.getInt("toDay");
                customer = realm.where(Customer.class).equalTo("ID",savedInstanceState.getString("CustomerID")).findFirst();
                address = realm.where(Address.class).equalTo("ID",savedInstanceState.getString("AddressID")).findFirst();

                Log.d("asdfg", fromDay + "/" + fromMonth + "/" + fromYear);
                Log.d("asdfg", toDay + "/" + toMonth + "/" + toYear);

            } else {
                fromDay = cal.get(Calendar.DAY_OF_MONTH);
                fromMonth = cal.get(Calendar.MONTH)+1;
                fromYear = cal.get(Calendar.YEAR);

                toDay = cal.get(Calendar.DAY_OF_MONTH);
                toMonth = cal.get(Calendar.MONTH)+1;
                toYear = cal.get(Calendar.YEAR);

            }

            layoutFrom = (RelativeLayout) findViewById(R.id.relativeLayout_from); // id fetch from xml
            layoutTo = (RelativeLayout) findViewById(R.id.relativeLayout_to); // id fetch from xml
            RelativeLayout layoutCus = (RelativeLayout) findViewById(R.id.relativeLayout_cus); // id fetch from xml
            layoutCompany = (LinearLayout) findViewById(R.id.linearLayout_company);


            if (!report.getCustomer()){
                layoutCus.setVisibility(View.GONE);
            }else{
                ImageButton imbCus = (ImageButton) findViewById(R.id.imageButton_search_customer);
                if (imbCus != null) {
                    imbCus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentManager searchmanager = getFragmentManager();
                            SearchCustomerFragment searchfrag = new SearchCustomerFragment();
                            searchfrag.show(searchmanager, "Search Customer Fragment");

                        }
                    });
                }

                txvCustomerCode = (TextView) findViewById(R.id.textView_customer_code);
                txvCustomerName = (TextView) findViewById(R.id.textView_customer_name);
                fillCustomer();

                if (layoutCus != null) {
                    layoutCus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentManager searchmanager = getFragmentManager();
                            SearchCustomerFragment searchfrag = new SearchCustomerFragment();
                            searchfrag.show(searchmanager, "Search Customer Fragment");

                        }
                    });
                }
            }

            if (!report.getFromDate()){
                layoutFrom.setVisibility(View.GONE);
            }else{
                txvFrom = (TextView) findViewById(R.id.textView_from);
                ImageButton imbFrom = (ImageButton) findViewById(R.id.imageButton_from);
                if (imbFrom != null) {
                    imbFrom.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startDatePicker(0);

                        }
                    });
                }
                String txt = getResources().getString(R.string.from_) + fromDay + "/" + fromMonth + "/" + fromYear;
                txvFrom.setText(txt);

                if (layoutFrom != null) {
                    layoutFrom.setBackground(rectShapeDrawable);
                    layoutFrom.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startDatePicker(0);
                        }
                    });
                }
            }

            if (!report.getToDate()){
                layoutTo.setVisibility(View.GONE);
            }else{
                txvTo = (TextView) findViewById(R.id.textView_to);
                ImageButton imbTo = (ImageButton) findViewById(R.id.imageButton_to);
                if (imbTo != null) {
                    imbTo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startDatePicker(1);
                        }
                    });
                }

                String txt1 = getResources().getString(R.string.to_) + toDay + "/" + toMonth + "/" + toYear;
                txvTo.setText(txt1);

                if (layoutTo != null) {
                    layoutTo.setBackground(rectShapeDrawable);
                    layoutTo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startDatePicker(1);
                        }
                    });
                }
            }

            if (!report.getCompany()){
                layoutCompany.setVisibility(View.GONE);
            }else{
                sprCompany = (Spinner) findViewById(R.id.spinner_company);
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(OnlineReports.this,
                        R.array.companiesArray, android.R.layout.simple_spinner_item);
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the mAdapter to the spinner
                sprCompany.setAdapter(adapter);
                sprCompany.setSelection(0);
            }

            start = (Button) findViewById(R.id.button_start);
            start.setTransformationMethod(null);
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (customer==null && report.getCustomer()) {
                        Toast.makeText(OnlineReports.this, "Δεν έχετε επιλέξει πελάτη.", Toast.LENGTH_SHORT).show();
                    }else {
                        try {
                            Intent intent = new Intent(OnlineReports.this, MyWebView.class);
                            intent.putExtra("ReportID",report.getID());
                            if (report.getFromDate()) {
                                intent.putExtra("fromYear", fromYear);
                                intent.putExtra("fromMonth", fromMonth);
                                intent.putExtra("fromDay", fromDay);
                            }
                            if (report.getToDate()) {
                                intent.putExtra("toYear", toYear);
                                intent.putExtra("toMonth", toMonth);
                                intent.putExtra("toDay", toDay);
                            }
                            if (report.getCustomer()) {
                                intent.putExtra("CustomerID", customer.getID());
                                intent.putExtra("AddressID", address.getID());
                            }
                            if (report.getCompany()) {
                                intent.putExtra("Company", sprCompany.getSelectedItemPosition());
                            }
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("fromYear", fromYear);
        outState.putInt("fromMonth", fromMonth);
        outState.putInt("fromDay", fromDay);
        outState.putInt("toYear", toYear);
        outState.putInt("toMonth", toMonth);
        outState.putInt("toDay", toDay);
        outState.putString("CustomerID", customer.getID());
    }

    @Override
    public void respondCustomerSearch(Customer customer, Address address) {
        try {
            this.customer = customer;
            this.address = address;
            fillCustomer();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
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

    }

    @Override
    public void respondDate(Integer position, int year, int month, int day) {
        try {
            if (position == 0) {
                fromYear = year;
                fromMonth = month;
                fromDay = day;
                //String txt = getResources().getString(R.string.from_) + fromDay + "/" + fromMonth + "/" + fromYear;
                String txt = getResources().getString(R.string.from_) + fromDay + "/" + fromMonth + "/" + fromYear;
                txvFrom.setText(txt);
                if (layoutFrom != null) {
                    layoutFrom.setBackground(rectShapeDrawable);
                }
            } else {
                toYear = year;
                toMonth = month;
                toDay = day;
                String txt = getResources().getString(R.string.to_) + toDay + "/" + toMonth + "/" + toYear;
                txvTo.setText(txt);
                if (layoutTo != null) {
                    layoutTo.setBackground(rectShapeDrawable);
                }
            }
        } catch (Resources.NotFoundException e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    public void respondCompanySite() {

    }

    @Override
    public void respondRecentPurchases(ArrayList<InvoiceLineSimple> sLines) {

    }

    private void fillCustomer(){
        try {
            if (customer!=null){
                String codetxt = getResources().getString(R.string.code) + ": " + customer.getCode();
                txvCustomerCode.setText(codetxt);

                String nametxt = getResources().getString(R.string.name) + ": " + customer.getName();
                txvCustomerName.setText(nametxt);
            }
        } catch (Resources.NotFoundException e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private void startDatePicker (int position){
        FragmentManager manager = getFragmentManager();
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setPosition(position);
        fragment.setLimit(true);
        fragment.show(manager, "datePicker");
    }
}
