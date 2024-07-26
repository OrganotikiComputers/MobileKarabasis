package organotiki.mobile.mobilestreet;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;

import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Company;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.CustomerDetail;
import organotiki.mobile.mobilestreet.objects.CustomerDetailTab;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;
import organotiki.mobile.mobilestreet.objects.OnlineReportType;

/**
 * Created by Thanasis on 7/6/2016.
 */
public class CustomerReport extends AppCompatActivity implements Communicator,LocationListener {

    Realm realm;
    GlobalVar gVar;
    DecimalFormat decim = new DecimalFormat("0.00");
    Customer customer;
    Address address;
    AlertDialog mAlertDialog;
    MenuItem mitOnlineReport, mitChangeCustomer, mitAdditionalDetails, mitB2B,mitCustomerWebService,mitCustomerGPS;
    Button saveNotes;
    EditText edtNotes;
    TextView txvCompany1, txvCompany2, txvBalance1EY, txvBalance1SY, txvBalance2EY, txvBalance2SY, txvTotal1, txvTotal2, txvOverallBalance, txvLetterCount,txvB2BCode;
    VolleyRequests request;
	Button btnGreen;
    Button btnRed;
    Button btnYellow;
    int serviceLevel;
    Location currentLocation;
    LocationManager locationManager;
    String Latitude,Longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_customer_report);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            request = new VolleyRequests();

            Toolbar toolbar = findViewById(R.id.customerReportBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                        0);
            }

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

			Bundle b = getIntent().getExtras();
            if (b != null) {
                this.customer = (Customer) this.realm.where(Customer.class).equalTo("ID", b.getString("CustomerID")).findFirst();
                this.address = (Address) this.realm.where(Address.class).equalTo("ID", b.getString("AddressID")).findFirst();
                request.getCustomerBalance(CustomerReport.this, this.customer);
            }
            if (savedInstanceState != null) {
                customer = realm.where(Customer.class).equalTo("ID", savedInstanceState.getString("CustomerID")).findFirst();
                address = realm.where(Address.class).equalTo("ID", savedInstanceState.getString("AddressID")).findFirst();
            }

            txvLetterCount = findViewById(R.id.textView_lettersCount);
            edtNotes = findViewById(R.id.editText_customer_notes);
            edtNotes.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            edtNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        txvLetterCount.setText(getString(R.string.characters_d_d, String.valueOf(edtNotes.getText()).equals("") ? 0 : String.valueOf(edtNotes.getText()).length(), 250));
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
            saveNotes = findViewById(R.id.button_save);
            saveNotes.setTransformationMethod(null);
            saveNotes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                customer.setMessage(String.valueOf(edtNotes.getText()));
                                customer.setNew(true);
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    });
                    request.setCustomerMessage(CustomerReport.this, customer);
                }
            });
			btnGreen = (Button) findViewById(R.id.button_green_service);
            btnGreen.setTransformationMethod((TransformationMethod) null);
            btnGreen.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    try {
                        if (customer != null) {
                            serviceLevel = 2;
                            request.setCustomerService(CustomerReport.this,customer,serviceLevel);
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
            btnYellow = (Button) findViewById(R.id.button_yellow_service);
            btnYellow.setTransformationMethod((TransformationMethod) null);
            btnYellow.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    try {
                        if (customer != null) {
                            serviceLevel = 1;
                            request.setCustomerService(CustomerReport.this,customer,serviceLevel);
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
            btnRed = (Button) findViewById(R.id.button_red_service);
            btnRed.setTransformationMethod((TransformationMethod) null);
            btnRed.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    try {
                        if (customer != null) {
                            serviceLevel = 0;
                            request.setCustomerService(CustomerReport.this,customer,serviceLevel);
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
            FillCustomerReport();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.customer_report_menu, menu);
            mitOnlineReport = menu.findItem(R.id.menuItem_online_report);
            mitChangeCustomer = menu.findItem(R.id.menuItem_change_customer_details);
            mitAdditionalDetails = menu.findItem(R.id.menuItem_additional_customer_details);
            mitB2B = menu.findItem(R.id.menuItem_b2b);
            mitCustomerWebService = menu.findItem(R.id.menuItem_customerWebService);
            mitCustomerGPS = menu.findItem(R.id.menuItem_customerGPSLocation);

        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (customer == null) {
            edtNotes.setEnabled(false);
            saveNotes.setEnabled(false);
            mitOnlineReport.setEnabled(false);
            mitOnlineReport.getIcon().setAlpha(130);
            mitChangeCustomer.setVisible(false);
            mitAdditionalDetails.setEnabled(false);
            mitAdditionalDetails.getIcon().setAlpha(130);
            mitB2B.setEnabled(false);
            mitB2B.getIcon().setAlpha(130);
            mitCustomerWebService.setEnabled(false);
            mitCustomerWebService.getIcon().setAlpha(130);
            mitCustomerGPS.setEnabled(false);
            mitCustomerGPS.getIcon().setAlpha(130);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
            if (customer != null) {
                outState.putString("CustomerID", customer.getID());
                outState.putString("AddressID", address.getID());
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItem_online_report:
                try {
                    request.sendRequest(CustomerReport.this, "SenService/GetSenOnlineReportType", "");
                    return super.onOptionsItemSelected(item);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                return false;
            case R.id.menuItem_search_customer:
                try {
                    FragmentManager searchmanager = getFragmentManager();
                    SearchCustomerFragment searchfrag = new SearchCustomerFragment();
                    searchfrag.show(searchmanager, "Search Customer Fragment");

                    return super.onOptionsItemSelected(item);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                return false;
            case R.id.menuItem_change_customer_details:
                try {
                    FragmentManager createmanager = getFragmentManager();
                    CreateCustomerFragment createfrag = new CreateCustomerFragment();
                    createfrag.setCustomerDetails(customer, address);
                    createfrag.show(createmanager, "Create Customer Fragment");
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                return true;
            case R.id.menuItem_additional_customer_details:
                try {
					this.realm.executeTransaction(new Realm.Transaction() {
                        public void execute(@NonNull Realm realm) {
                            try {
                                realm.delete(CustomerDetailTab.class);
                                realm.delete(CustomerDetail.class);
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    });
                    Intent intent = new Intent(CustomerReport.this, AdditionalCustomerDetails.class);
                    intent.putExtra("CustomerID", customer.getID());
                    intent.putExtra("AddressID", address.getID());
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                return true;
            case R.id.menuItem_b2b:
                try {
                    request.getCustomerB2BCredentials(CustomerReport.this, customer);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                return true;
            case R.id.menuItem_customerWebService:
                try{
                  /*  String fulladdress=address.getStreet();
                    if(!TextUtils.isEmpty(address.getCity())) fulladdress+=","+address.getCity();
                    if(!TextUtils.isEmpty(address.getPostalCode())) fulladdress+=","+address.getPostalCode();
                    getdistanceFromAddress(fulladdress);*/

                    if(TextUtils.isEmpty(address.getLatitude())||TextUtils.isEmpty(address.getLongitude())){
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CustomerReport.this);

                        alertDialog.setNegativeButton("OK", null);

                        alertDialog.setMessage("Δεν υπάρχει το στίγμα του πελάτη.Παρακαλώ πατήστε στο εικονίδιο 'Λήψη στίγματος' και προσπαθήστε ξανά.");
                        alertDialog.setTitle(getString(R.string.app_name));
                        mAlertDialog = alertDialog.create();
                        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                                negativeButton.setTransformationMethod(null);
                            }
                        });

                        mAlertDialog.show();
                    }else{
                        Geocoder coder = new Geocoder(CustomerReport.this);
                        List<android.location.Address> currentaddress;
                        currentaddress=coder.getFromLocation(Double.parseDouble(address.getLatitude()),Double.parseDouble(address.getLongitude()),5);
                        boolean isClose=getdistanceFromAddress(currentaddress);
                        Intent intent = new Intent(this,CustomerWebService.class);
                        intent.putExtra("customerCode", customer.getCode());
                        intent.putExtra("isCLose", isClose);
                        startActivity(intent);
                    }

                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }

                return true;
            case R.id.menuItem_customerGPSLocation:
                try{
                    String fulladdress=address.getStreet();
                   /* String latitude=address.getLatitude();
                    String longitude=address.getLongitude();*/
                    if(!TextUtils.isEmpty(address.getCity())) fulladdress+=","+address.getCity();
                    if(!TextUtils.isEmpty(address.getPostalCode())) fulladdress+=","+address.getPostalCode();
                    ///latitude and longtitude !=null
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(CustomerReport.this);

                    alertDialog.setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                if(currentLocation==null){
                                    currentLocation=getLastKnownLocation();
                                }
                                Geocoder coder = new Geocoder(CustomerReport.this);
                                List<android.location.Address> currentaddress;
                                currentaddress=coder.getFromLocation(currentLocation.getLatitude(),currentLocation.getLongitude(),5);
                                       /* List<android.location.Address> customerAddress;
                                        currentaddress=coder.getFromLocation(address.getLatitude(),address.getLongitude(),5);*/
                                Latitude=String.valueOf(currentLocation.getLatitude());
                                Longitude=String.valueOf(currentLocation.getLongitude());
                                request.UpdateCustomerAddressCoordinates(CustomerReport.this,String.valueOf(currentLocation.getLatitude()),String.valueOf(currentLocation.getLongitude()),address.getID());
                                /*boolean isClose=getdistanceFromAddress(currentaddress);
                                if(!isClose){
                                    Toast.makeText(CustomerReport.this, "Λάθος διεύθυνση", Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(CustomerReport.this, String.valueOf(currentaddress.get(0).getAddressLine(0)), Toast.LENGTH_LONG).show();
                                }*/

                            } catch (Exception e) {
                                Toast.makeText(CustomerReport.this, "Δεν ήταν δυνατό να παρθεί το στίγμα σας,βεβαιωθείτε ότι είναι ανοιχτό το GPS και η βέλτιστη τοποθεσία.", Toast.LENGTH_LONG).show();
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    });

                    alertDialog.setNegativeButton("Όχι", null);

                    alertDialog.setMessage("Θέλετε να καταχωρηθεί η τρέχουσα διεύθυνση στο στίγμα του πελάτη;");
                    alertDialog.setTitle(getString(R.string.app_name));
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
                    Toast.makeText(CustomerReport.this, "Δεν ήταν δυνατό να παρθεί το στίγμα σας,βεβαιωθείτε ότι είναι ανοιχτό το GPS και η βέλτιστη τοποθεσία.", Toast.LENGTH_LONG).show();
                    Log.e("asdfg", e.getMessage(), e);
                }

                return true;
            case android.R.id.home:
                try {
                    finish();
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                return true;

        }
        return false;
    }

    @Override
    public void respondCustomerSearch(Customer customer, Address address) {
        try {
            this.customer = customer;
            this.address = address;
            request.getCustomerBalance(CustomerReport.this, customer);
            edtNotes.setEnabled(true);
            saveNotes.setEnabled(true);
            FillCustomerReport();
            mitOnlineReport.setEnabled(true);
            mitOnlineReport.getIcon().setAlpha(255);
//            mitChangeCustomer.setEnabled(true);
//            mitChangeCustomer.getIcon().setAlpha(255);
            mitAdditionalDetails.setEnabled(true);
            mitAdditionalDetails.getIcon().setAlpha(255);
            mitB2B.setEnabled(true);
            mitB2B.getIcon().setAlpha(255);
            mitCustomerWebService.setEnabled(true);
            mitCustomerWebService.getIcon().setAlpha(255);
            mitCustomerGPS.setEnabled(true);
            mitCustomerGPS.getIcon().setAlpha(255);
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
    public void respondVolleyRequestFinished(Integer position, JSONObject jsonObject) {
        switch (position) {
            case 0:
                try {
                    if (jsonObject == null || !jsonObject.getBoolean("CustomerMessageResult")) {
                        Toast.makeText(CustomerReport.this, "Τα σχόλια τιμολόγησης δεν αποθηκεύτηκαν.", Toast.LENGTH_LONG).show();
                    } else {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                customer.setNew(false);
                            }
                        });
                        Toast.makeText(CustomerReport.this, "Τα σχόλια τιμολόγησης αποθηκεύτηκαν επιτυχώς.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 1:
                try {
                    Log.d("asdfg", "hi");
                    if (jsonObject != null ) {
                        if (!jsonObject.isNull("CustomerB2BResult")) {
                            Intent intent = new Intent(CustomerReport.this, B2B.class);
                            JSONObject jo = jsonObject.getJSONObject("CustomerB2BResult");
                            intent.putExtra("Username", jo.getString("Username"));
                            intent.putExtra("Password", jo.getString("Password"));
                            startActivity(intent);
                        } else {
                            Toast.makeText(CustomerReport.this, "Δεν βρέθηκαν στοιχεία σύνδεσης του πελάτη για το B2B.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(CustomerReport.this, "Δεν βρέθηκαν στοιχεία σύνδεσης του πελάτη για το B2B.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 2:
                try {
                    OnlineReportType type = realm.where(OnlineReportType.class).equalTo("Code", "PDF").findFirst();
                    Log.d("asdfg", "hi");
                    if (type.getFromDate() || type.getToDate() || type.getCustomer()) {
                        Intent intent = new Intent(CustomerReport.this, OnlineReports.class);
                        intent.putExtra("CustomerID", customer.getID());
                        intent.putExtra("AddressID", address.getID());
                        intent.putExtra("ReportID", type.getID());
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(CustomerReport.this, MyWebView.class);
                        intent.putExtra("ReportID", type.getID());
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 3:
                try {
                    Log.d("asdfg", String.valueOf(jsonObject));
                    if (!(jsonObject.length() == 0) || jsonObject.getString("GetBalancesResult").equals("null") || jsonObject.getString("GetBalancesResult") == null || jsonObject.isNull("GetBalancesResult")) {
                        final JSONObject jo = jsonObject.getJSONObject("GetBalancesResult");
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                try {
                                    Double aey = jo.getDouble("AEY");
                                    Double asy = jo.getDouble("ASY");
                                    Double fey = jo.getDouble("FEY");
                                    Double fsy = jo.getDouble("FSY");
									serviceLevel = jo.getInt("ServiceLevel");
									SetCustomerService();
                                    txvBalance1EY.setText(decim.format(aey));
                                    txvBalance1SY.setText(decim.format(asy));
                                    txvTotal1.setText(decim.format(aey + asy));
                                    txvBalance2EY.setText(decim.format(fey));
                                    txvBalance2SY.setText(decim.format(fsy));
                                    txvTotal2.setText(decim.format(fey + fsy));
                                    txvOverallBalance.setText(decim.format(aey + asy + fey + fsy));
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                            }
                        });

                    }
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 4:
                try {
                    Log.d("asdfg", String.valueOf(jsonObject));
                    if (jsonObject.length() != 0 || jsonObject.getBoolean("SetServiceLevelResult")) {
                        SetCustomerService();
                    }
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 5:
                try {
                    Log.d("asdfg", String.valueOf(jsonObject));
                    if (jsonObject.length() != 0 ) {
                       if(jsonObject.getInt("UpdateCustomerAddressCoordinatesResult")==1){
                           realm.executeTransaction(new Realm.Transaction() {
                               @Override
                               public void execute(Realm realm) {
                                   try {
                                       /*address.setEmail("das");*/
                                       address.setLatitude(Latitude);
                                       address.setLongitude(Longitude);
                                       Toast.makeText(CustomerReport.this, "Το στίγμα του πελάτη αποθηκεύτηκε επιτυχώς!", Toast.LENGTH_LONG).show();
                                   } catch (Exception e) {
                                       Log.e("asdfg", e.getMessage(), e);
                                   }
                               }
                           });
                       }else{
                           Toast.makeText(CustomerReport.this, "Δεν αποθηκεύτηκε το στίγμα του πελάτη.", Toast.LENGTH_LONG).show();
                       }
                    }else{
                        Toast.makeText(CustomerReport.this, "Δεν αποθηκεύτηκε το στίγμα του πελάτη.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(CustomerReport.this, "Δεν αποθηκεύτηκε το στίγμα του πελάτη.", Toast.LENGTH_LONG).show();
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

    private void FillCustomerReport() {
        try {

            txvCompany1 = findViewById(R.id.textView_company1_title);
            txvCompany1.setText(realm.where(Company.class).equalTo("InAppID", "1").findFirst().getDescription());
            txvCompany2 = findViewById(R.id.textView_company2_title);
            txvCompany2.setText(realm.where(Company.class).equalTo("InAppID", "2").findFirst().getDescription());

            if (customer != null) {
                String code = customer.getCode();
                String name = customer.getName();
                String tin = customer.getTIN();
                /*String credit = customer.getCreditText();
                String debit = customer.getDebitText();
                String balance = customer.getBalanceText();*/

                TextView tvCusCode = findViewById(R.id.textView_customer_code);
                tvCusCode.setText(code);
                TextView tvCusName = findViewById(R.id.textView_customer_name);
                tvCusName.setText(name);
                TextView tvCusTIN = findViewById(R.id.textView_customer_tin);
                tvCusTIN.setText(tin);
                txvB2BCode= findViewById(R.id.textView_customer_B2BCode);
                txvB2BCode.setText(customer.getB2bCode());
                txvBalance1EY = findViewById(R.id.textView_balance1EY);
                txvBalance1SY = findViewById(R.id.textView_balance1SY);
                txvBalance2EY = findViewById(R.id.textView_balance2EY);
                txvBalance2SY = findViewById(R.id.textView_balance2SY);
                txvTotal1 = findViewById(R.id.textView_total1);
                txvTotal2 = findViewById(R.id.textView_total2);
                txvOverallBalance = findViewById(R.id.textView_overall_balance);
                edtNotes.setText(customer.getMessage());
            }

            if (address != null) {
                String street = address.getStreet();
                String city = address.getCity();
                String postalCode = address.getPostalCode();
                String phone1 = address.getPhone1();
                String phone2 = address.getPhone2();
                String mobile = address.getMobile();
                String email = address.getEmail();

                TextView tvCusAddress = findViewById(R.id.textView_customer_address);
                tvCusAddress.setText(street);
                TextView tvCusCity = findViewById(R.id.textView_customer_city);
                tvCusCity.setText(city);
                TextView tvCusPostalCode = findViewById(R.id.textView_customer_postalcode);
                tvCusPostalCode.setText(postalCode);
                TextView tvCusPhone1 = findViewById(R.id.textView_customer_phone1);
                tvCusPhone1.setText(phone1);
                TextView tvCusPhone2 = findViewById(R.id.textView_customer_phone2);
                tvCusPhone2.setText(phone2);
                TextView tvCusMobile = findViewById(R.id.textView_customer_mobile);
                tvCusMobile.setText(mobile);
                TextView tvCusEmail = findViewById(R.id.textView_customer_email);
                tvCusEmail.setText(email);
            }


        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
	public void SetCustomerService() {
        GradientDrawable borderGreen = new GradientDrawable();
        borderGreen.setColor(getResources().getColor(R.color.colorGreen));
        borderGreen.setStroke(5, this.serviceLevel == 2 ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.colorAccent));
        this.btnGreen.setBackground(borderGreen);
        GradientDrawable borderYellow = new GradientDrawable();
        borderYellow.setColor(getResources().getColor(R.color.colorA));
        borderYellow.setStroke(5, this.serviceLevel == 1 ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.colorAccent));
        this.btnYellow.setBackground(borderYellow);
        GradientDrawable borderRed = new GradientDrawable();
        borderRed.setColor(getResources().getColor(R.color.colorRed));
        borderRed.setStroke(5, this.serviceLevel == 0 ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.colorAccent));
        this.btnRed.setBackground(borderRed);
    }

    public boolean getdistanceFromAddress(List<android.location.Address> currentAddress) {
        try{
            if (currentLocation == null) {
                currentLocation=getLastKnownLocation();
            }
            double radius = 100;
            Location loc = new Location("dummyprovider");
            android.location.Address location = currentAddress.get(0);
            loc.setLatitude(location.getLatitude());
            loc.setLongitude(location.getLongitude());
            double distance = loc.distanceTo(currentLocation);
            double distance2=0;
            if(currentAddress.size()>1) {
                for (int i = 1; i < currentAddress.size(); i++) {
                    location = currentAddress.get(i);
                    loc.setLatitude(location.getLatitude());
                    loc.setLongitude(location.getLongitude());
                    distance2 = loc.distanceTo(currentLocation);
                    if (distance2 < distance) distance = distance2;
                }
            }
        /*    List<android.location.Address> address2;
            address2=coder.getFromLocation(currentLocation.getLatitude(),currentLocation.getLongitude(),5);

        location.getLatitude();
          location.getLongitude();

          double latitude = (double) (location.getLatitude() * 1E6);
          double longitude = (double) (location.getLongitude() * 1E6);
          loc.setLatitude(location.getLatitude());
          loc.setLongitude(location.getLongitude());
            Toast.makeText(this, String.valueOf(address2.get(0).getAddressLine(0)), Toast.LENGTH_SHORT).show();*/

            if (distance < radius) {
                return true;
            }else{
                return false;
            }
        }catch (Exception ex){
            return false;
        }


    }

    public Location getLocationFromAddress(String strAddress){
        Geocoder coder = new Geocoder(this);
        List<android.location.Address> address;


        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null || address.size()==0) {
                return null;
            }
            if (currentLocation == null) {
                currentLocation=getLastKnownLocation();
            }
            double radius = 500;
            Location loc = new Location("dummyprovider");
            android.location.Address location = address.get(0);
            loc.setLatitude(location.getLatitude());
            loc.setLongitude(location.getLongitude());
            int closestAddress=0;
            double distance = loc.distanceTo(currentLocation);
            double distance2=0;
            if(address.size()>1) {
                for (int i = 1; i < address.size(); i++) {
                    location = address.get(i);
                    loc.setLatitude(location.getLatitude());
                    loc.setLongitude(location.getLongitude());
                    distance2 = loc.distanceTo(currentLocation);
                    if (distance2 < distance) {
                        distance = distance2;
                        closestAddress=i;
                    }
                }
            }
            location =address.get(closestAddress);
            loc.setLatitude(location.getLatitude());
            loc.setLongitude(location.getLongitude());
            return loc;


        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Location getLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                        0);
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            Location l = locationManager.getLastKnownLocation(provider);


            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {

                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}