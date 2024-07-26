package organotiki.mobile.mobilestreet;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import io.realm.Realm;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.FInvoice;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.GpsCoordinates;
import organotiki.mobile.mobilestreet.objects.Invoice;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;
import organotiki.mobile.mobilestreet.objects.User;

public class MainScreen extends AppCompatActivity implements View.OnClickListener, Communicator, DialogInterface.OnDismissListener {

    LinearLayout lilUser;
    Button newOrder, loadInvoices, sync, customerReport, onlineReports, collections, returns, loadCollections, currencyCalculator, customerBrowser ,calendarEvents, bookmarks= null;
    Realm realm;
    GlobalVar gVar;
	CurrencyCalculatorFragment currencyCalculatorFragment;
    OnlineReportsFragment onlineReportsFragment;
    AlertDialog mAlertDialog;
    int lastPressed;
    VolleyRequests request;
    Intent intent;
    TextView txvUser, txvCompany, txvCompanySite, txvVersion;
    CustomerBrowserFragment customerBrowserFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        try {
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            request = new VolleyRequests();



            //startService(new Intent(this, GPSService.class));
          //  startService(new Intent(this, SendGPSCoordinatesService.class));

            Toolbar toolbar = findViewById(R.id.mainScreenBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                        0);
            }

            txvUser = findViewById(R.id.textView_user);
            String str = getString(R.string.user_)+ gVar.getMyUser().getFullName();
            txvUser.setText(str);

            txvCompany = findViewById(R.id.textView_company);
            str = getString(R.string.company_)+(gVar.getMyCompanySite()==null?" - ":gVar.getMyCompanySite().getMyCompany().getDescription())+ " / "+getString(R.string.tin_)+(gVar.getMyCompanySite()==null?" - ":gVar.getMyCompanySite().getMyCompany().getTIN());
            txvCompany.setText(str);

            txvCompanySite = findViewById(R.id.textView_company_site);
            str = getString(R.string.companySite_)+(gVar.getMyCompanySite()==null?" - ":gVar.getMyCompanySite().getDescription());
            txvCompanySite.setText(str);

            txvVersion= findViewById(R.id.textView_version);
            str = getString(R.string.version_)+ gVar.getVerNum();
            txvVersion.setText(str);

            lilUser = findViewById(R.id.linearLayout_user_details);
            lilUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        FragmentManager manager = getFragmentManager();
                        CompanySiteFragment frag = new CompanySiteFragment();
                        frag.show(manager, "Load Invoices Fragment");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });

            newOrder = findViewById(R.id.button_newOrder);
            newOrder.setOnClickListener(this);
            newOrder.setTransformationMethod(null);
            newOrder.setEnabled(false);
            newOrder.setAlpha(.5f);

            loadInvoices = findViewById(R.id.button_loadOrder);
            loadInvoices.setOnClickListener(this);
            loadInvoices.setTransformationMethod(null);

            sync = findViewById(R.id.button_sync);
            sync.setOnClickListener(this);
            sync.setTransformationMethod(null);

            customerReport = findViewById(R.id.button_customer_report);
            customerReport.setOnClickListener(this);
            customerReport.setTransformationMethod(null);

            onlineReports = findViewById(R.id.button_online_reports);
            onlineReports.setOnClickListener(this);
            onlineReports.setTransformationMethod(null);

            collections = findViewById(R.id.button_collections);
            collections.setOnClickListener(this);
            collections.setTransformationMethod(null);

            loadCollections = findViewById(R.id.button_loadCollections);
            loadCollections.setOnClickListener(this);
            loadCollections.setTransformationMethod(null);

            currencyCalculator = findViewById(R.id.button_currency_calculator);
            currencyCalculator.setOnClickListener(this);
            currencyCalculator.setTransformationMethod(null);

            customerBrowser = findViewById(R.id.button_customer_browser);
            customerBrowser.setOnClickListener(this);
            customerBrowser.setTransformationMethod(null);

            returns = findViewById(R.id.button_returns);
            returns.setOnClickListener(this);
            returns.setTransformationMethod(null);

            bookmarks = findViewById(R.id.button_bookmarks);
            bookmarks.setOnClickListener(this);
            bookmarks.setTransformationMethod(null);

            calendarEvents=findViewById(R.id.button_events);
            calendarEvents.setOnClickListener(this);
            calendarEvents.setTransformationMethod(null);

            GlobalVar gVar = realm.where(GlobalVar.class).findFirst();
            User user = realm.where(User.class).equalTo("ID", gVar.getMyUser().getID()).findFirst();
            String parentActivity = getIntent().getStringExtra("ParentActivity");
            if (parentActivity!=null) {
                if (parentActivity.equals("LogInScreen")){
                    Toast.makeText(this, getString(R.string.welcome, user.getFullName()), Toast.LENGTH_LONG).show();
                }
            }

            String[] permissions = Build.VERSION.SDK_INT >= 29 ? PERMISSIONS_Q_AND_ABOVE : PERMISSIONS_BELOW_Q;
            if (!hasPermissions(permissions)) {
                requestPermissions(permissions);
            } else {
                startLocationService();
            }


        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }

        //Toast.makeText(this, "Καλωσήλθες  " + realm.where(User.class).equalTo("ID", realm.where(GlobalVar.class).findFirst().getUserID()).findFirst().getFullName(), Toast.LENGTH_LONG).show();
    }


    private void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
    }

    private static final String[] PERMISSIONS_BELOW_Q = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WAKE_LOCK,
    };

    @RequiresApi(api = 29)
    private static final String[] PERMISSIONS_Q_AND_ABOVE = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WAKE_LOCK,
            //Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startLocationService() {
        try {
            if (!isServiceRunning(LocationService.class)) {
                Context context = getApplicationContext();
                Intent intent = new Intent(this, LocationService.class); // Build the intent for the service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                   // context.startService(intent);
                    ContextCompat.startForegroundService(this, intent);
                } else {
                    context.startService(intent);
                }
            } else {
                Log.i("LocationServiceStatus", "LocationService is already running.");
            }
        } catch (Exception ex) {
            Log.e("LocationServiceError", "Failed to start location service", ex);
        }
    }
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            } else {
                Log.e("PermissionError", "Required permissions not granted");
            }
        }
    }
    private static final int PERMISSIONS_REQUEST_CODE = 1;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_newOrder:
                try {
                    /*Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
                    String date = df.format(c.getTime());
                    String time = tf.format(c.getTime());
                    Invoice inv = new Invoice(UUID.randomUUID().toString(), Settings.Secure.getString(MainScreen.this.getContentResolver(), Settings.Secure.ANDROID_ID), gVar.getMyUser(), date, time,"");

                    realm.beginTransaction();
                    Invoice invoice=realm.copyToRealmOrUpdate(inv);
                    gVar.setMyInvoice(invoice);
                    gVar.getMyInvoice().setReturns(false);
                    realm.commitTransaction();
                    intent = new Intent(MainScreen.this, NewOrder.class);
                    startActivity(intent);*/
                    break;
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
            case R.id.button_loadOrder:
                try {
                    FragmentManager loadManager = getFragmentManager();
                    LoadInvoicesFragment loadFrag = new LoadInvoicesFragment();
                    loadFrag.show(loadManager, "Load Invoices Fragment");
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_sync:
                try {
                    intent = new Intent(MainScreen.this, Sync.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_customer_report:
                try {
                    intent = new Intent(MainScreen.this, CustomerReport.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_online_reports:
                try {
                    FragmentManager manager = getFragmentManager();
                    onlineReportsFragment = new OnlineReportsFragment();
                    onlineReportsFragment.show(manager, "onlineReports");
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_collections:
                try {
                    lastPressed = 1;
                    request.sendAuthenticationRequest(MainScreen.this);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_loadCollections:
                try {
                    FragmentManager loadManager = getFragmentManager();
                    LoadFInvoicesFragment loadFrag = new LoadFInvoicesFragment();
                    loadFrag.show(loadManager, "Load Collections Fragment");
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_returns:
                try {
                    lastPressed = 0;
                    request.sendAuthenticationRequest(MainScreen.this);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_currency_calculator:
                try{
                    FragmentManager loadManager = getFragmentManager();
                    currencyCalculatorFragment = new CurrencyCalculatorFragment();
                    currencyCalculatorFragment.show(loadManager, "Load Collections Fragment");
                }catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_customer_browser:
                try{
                    for(int i=0;i<realm.where(GpsCoordinates.class).findAll().size();i++){
                        Toast.makeText(this, realm.where(GpsCoordinates.class).findAll().get(i).toString(), Toast.LENGTH_LONG).show();
                    }
                    intent = new Intent(this, CustomerBrowser.class);
                    startActivity(this.intent);
                }catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_bookmarks:
                try {
                    intent = new Intent(MainScreen.this, Bookmarks.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_events:
                try {
                    FragmentManager eventFragment = getFragmentManager();
                    CalendarEventsFragment searchfrag = new CalendarEventsFragment();
                    searchfrag.show(eventFragment, "Event Fragment");
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {

        doExit();
    }

    private void doExit() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                MainScreen.this);

        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainScreen.this.finishAffinity();
            }
        });

        alertDialog.setNegativeButton(getString(R.string.no), null);

        alertDialog.setMessage("Θέλετε να βγείτε από την εφαρμογή;");
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
    public void respondVolleyRequestFinished(Integer position, JSONObject jsonObject) {
        try {
            switch (position) {
                case 0:
                    try {
                        switch (lastPressed) {
                            case 1:
                                String messageF = jsonObject.getString("Message");
                                Toast.makeText(MainScreen.this, messageF, Toast.LENGTH_LONG).show();
                                try {
                                    Calendar c = Calendar.getInstance();
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                    SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
                                    String date = df.format(c.getTime());
                                    String time = tf.format(c.getTime());
                                    FInvoice fInvoice = new FInvoice(UUID.randomUUID().toString(), Settings.Secure.getString(MainScreen.this.getContentResolver(), Settings.Secure.ANDROID_ID), date, time, gVar.getMyUser(), gVar.getMyUser().getUsername());

                                    realm.beginTransaction();
                                    FInvoice invoice = realm.copyToRealmOrUpdate(fInvoice);
                                    gVar.setMyFInvoice(invoice);
                                    realm.commitTransaction();
                                    Log.d("asdfg", String.valueOf(gVar.getMyFInvoice()));
                                    intent = new Intent(MainScreen.this, Collections.class);
                                    startActivity(intent);
                                    break;
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                            case 0:
                                String messageC = jsonObject.getString("Message");
                                Toast.makeText(MainScreen.this, messageC, Toast.LENGTH_LONG).show();
                                try {
                                    Calendar c = Calendar.getInstance();
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                    SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
                                    String date = df.format(c.getTime());
                                    String time = tf.format(c.getTime());
                                    Invoice inv = new Invoice(UUID.randomUUID().toString(), Settings.Secure.getString(MainScreen.this.getContentResolver(), Settings.Secure.ANDROID_ID), gVar.getMyUser(), gVar.getMyUser().getUsername(), date, time, true);

                                    realm.beginTransaction();
                                    Invoice invoice = realm.copyToRealmOrUpdate(inv);
                                    gVar.setMyInvoice(invoice);
                                    realm.commitTransaction();
                                    intent = new Intent(MainScreen.this, Returns.class);
                                    startActivity(intent);
                                    break;
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    break;
                case 1:
                    try {
                        String message = jsonObject.getString("Message");
                        Toast.makeText(MainScreen.this, message, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    break;
                case 2:
                    onlineReportsFragment.VolleyRequestCompleted();
                    break;
                case 3:
                    customerBrowserFragment.respondVolley();
                    break;
                case 4:
                    currencyCalculatorFragment.RespondAccounts(jsonObject);
                    break;
            }
        }catch (Exception e){
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    public void respondDate(Integer position, int year, int month, int day) {
        this.currencyCalculatorFragment.depositsFragment.respondDate(position, year, month, day);
    }

    @Override
    public void respondCompanySite() {
        txvCompany = findViewById(R.id.textView_company);
        String str = getString(R.string.company_)+(gVar.getMyCompanySite()==null?" - ":gVar.getMyCompanySite().getMyCompany().getDescription())+ " / "+getString(R.string.tin_)+(gVar.getMyCompanySite()==null?" - ":gVar.getMyCompanySite().getMyCompany().getTIN());
        txvCompany.setText(str);

        txvCompanySite = findViewById(R.id.textView_company_site);
        str = getString(R.string.companySite_)+(gVar.getMyCompanySite()==null?" - ":gVar.getMyCompanySite().getDescription());
        txvCompanySite.setText(str);
    }

    @Override
    public void respondRecentPurchases(ArrayList<InvoiceLineSimple> sLines) {

    }
	
	public void onDismiss(DialogInterface dialog) {
        this.currencyCalculatorFragment.CheckDifference();
        CurrencyCalculatorFragment currencyCalculatorFragment2 = this.currencyCalculatorFragment;
    }
}