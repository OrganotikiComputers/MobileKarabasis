package organotiki.mobile.mobilestreet;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.realm.Realm;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.FInvoice;
import organotiki.mobile.mobilestreet.objects.FInvoiceLine;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.Invoice;
import organotiki.mobile.mobilestreet.objects.InvoiceLine;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;
import organotiki.mobile.mobilestreet.objects.OnlineReportType;

/**
 * Created by Thanasis on 30/5/2016.
 */
public class Sync extends AppCompatActivity implements Communicator {

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    RelativeLayout relativeLayoutIPs;
    Button btnSave, btnDeleteInvoices, btnSyncCusItem, btnSyncInvoice, btnUpdate,btnSyncEvents;
//    ProgressBar pbrSync;
    TextView txvDeviceID;
    EditText localIP,onlineIP;
    Realm realm;
    GlobalVar gVar;
    VolleyRequests request = new VolleyRequests();
    private long mLastClickTime;
    SyncMessagesFragment frag;
    AlertDialog mAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        try {
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            Toolbar toolbar = (Toolbar) findViewById(R.id.syncBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mLastClickTime = 0;

            txvDeviceID = (TextView) findViewById(R.id.textView_deviceID);
            String textDeviceID = "Ο κωδικός της συσκευής είναι: "+ Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            txvDeviceID.setText(textDeviceID);

            relativeLayoutIPs = (RelativeLayout) findViewById(R.id.relativeLayout_IPs);
            if (!(gVar.getMyUser().getID().equals("user"))) {
                relativeLayoutIPs.setVisibility(View.GONE);
            }

            btnDeleteInvoices = (Button) findViewById(R.id.button_delete_invoices);
            btnDeleteInvoices.setTransformationMethod(null);
            btnDeleteInvoices.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime > 1500) {
                        mLastClickTime = SystemClock.elapsedRealtime();
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Sync.this);

                        alertDialog.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.where(Invoice.class).findAll().deleteAllFromRealm();
                                        realm.where(InvoiceLine.class).findAll().deleteAllFromRealm();

                                        realm.where(FInvoice.class).findAll().deleteAllFromRealm();
                                        realm.where(FInvoiceLine.class).findAll().deleteAllFromRealm();
                                    }
                                });
                                Toast.makeText(Sync.this, "Τα παραστατικά διαγράφτηκαν.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        alertDialog.setNegativeButton(getString(R.string.cancel), null);
                        alertDialog.setMessage("Είστε σίγουροι πως θέλετε να διαγράψετε όλα τα παραστατικά(επιστροφές και εισπράξεις);");
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
//                        try {
//
//                            writeToFile(String.valueOf(localIP.getText()+"*"+String.valueOf(onlineIP.getText())));
//
//                        } catch (Exception e) {
//                            Log.e("asdfg", e.getMessage(), e);
//                        }
                    }
                }
            });

            btnSave = (Button) findViewById(R.id.button_save);
            btnSave.setTransformationMethod(null);
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime > 1500) {
                        mLastClickTime = SystemClock.elapsedRealtime();
                        try {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    gVar.setLocalIP(String.valueOf(localIP.getText()));
                                    gVar.setOnlineIP(String.valueOf(onlineIP.getText()));
                                }
                            });

                            ActivityCompat.requestPermissions(Sync.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                            if(writeToFile(String.valueOf(localIP.getText()+"*"+String.valueOf(onlineIP.getText())))){
                                Toast.makeText(Sync.this, "Η διεύθυνση αποθηκεύτηκε", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                }
            });

//            pbrSync = (ProgressBar) findViewById(R.id.progressBar_sync);
//            pbrSync.setVisibility(View.GONE);

            btnSyncCusItem = (Button) findViewById(R.id.button_syncCustomerItem);
            btnSyncCusItem.setTransformationMethod(null);
            btnSyncCusItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1500) {

                    } else {
                        mLastClickTime = SystemClock.elapsedRealtime();
                        try {

                            FragmentManager manager = getFragmentManager();
                            frag = new SyncMessagesFragment();
                            frag.setParentButton(0);
                            frag.show(manager, "Sync Messages Fragment");
//                            pbrSync.setVisibility(View.VISIBLE);
/*
                            request = new VolleyRequests();
                            request.sendAuthenticationRequest(Settings.Secure.getString(Sync.this.getContentResolver(), Settings.Secure.ANDROID_ID), Sync.this);
*/
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                }
            });

            btnSyncInvoice = (Button) findViewById(R.id.button_syncInvoice);
            btnSyncInvoice.setTransformationMethod(null);
            btnSyncInvoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1500) {

                    } else {
                        mLastClickTime = SystemClock.elapsedRealtime();
                        try {

                            FragmentManager manager = getFragmentManager();
                            frag = new SyncMessagesFragment();
                            frag.setParentButton(1);
                            frag.show(manager, "Sync Messages Fragment");
                            /*request = new VolleyRequests();
                            request.sendSyncEmail(Sync.this);*/
//                            pbrSync.setVisibility(View.VISIBLE);
                            Log.d("asdfg", "bye");
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                }
            });

            btnSyncEvents = (Button) findViewById(R.id.button_syncEvents);
            btnSyncEvents.setTransformationMethod(null);
            btnSyncEvents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1500) {

                    } else {
                        mLastClickTime = SystemClock.elapsedRealtime();
                        try {

                            FragmentManager manager = getFragmentManager();
                            frag = new SyncMessagesFragment();
                            frag.setParentButton(2);
                            frag.show(manager, "Sync Messages Fragment");
//                            pbrSync.setVisibility(View.VISIBLE);
/*
                            request = new VolleyRequests();
                            request.sendAuthenticationRequest(Settings.Secure.getString(Sync.this.getContentResolver(), Settings.Secure.ANDROID_ID), Sync.this);
*/
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                }
            });


            btnUpdate = (Button) findViewById(R.id.button_update);
            btnUpdate.setTransformationMethod(null);
            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (realm.where(Invoice.class).equalTo("isFinal", true).count()+realm.where(FInvoice.class).equalTo("isFinal", true).count()==0) {
                            Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.docs");
                            if (intent == null) {
                                // Bring user to the market or let them choose an app?
                                intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=" + "com.google.android.apps.docs"));
                            }
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }else{
                            Toast.makeText(Sync.this, "Έχετε παραστατικά έτοιμα για συγχρονισμό. Παρακαλώ συγχρονίστε τα πριν την ενημέρωση.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });

            onlineIP = (EditText) findViewById(R.id.editText_online_IP);
            onlineIP.setText(gVar.getOnlineIP());
            localIP = (EditText) findViewById(R.id.editText_local_IP);
            localIP.setText(gVar.getLocalIP());
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private boolean writeToFile(String data) {
        try {
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/documents");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (success) {
                // Do something on success
            } else {
                Toast.makeText(Sync.this, getString(R.string.noDirectoryCreated, Environment.getExternalStorageDirectory().getAbsolutePath() + "/documents"), Toast.LENGTH_SHORT).show();
                return false;
            }
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/documents/", "Server.txt");
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(data.getBytes());
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
                return false;
            } finally {
                stream.close();
            }
            return true;
        } catch (Exception e) {
            Log.e("asdfg", "File write failed: " + e.toString(),e);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(writeToFile(String.valueOf(localIP.getText()+"*"+String.valueOf(onlineIP.getText()))))
                        Toast.makeText(Sync.this, "Η διεύθυνση αποθηκεύτηκε", Toast.LENGTH_SHORT).show();


                } else {


                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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

        try {
            String message = jsonArray.getString("Message");
            frag.addNewMessage(message);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
        switch (position) {
            case 0:
            try {
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
                break;
            case 1:
                try {
                    frag.enableButton();
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }

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

    /*private class SyncGet extends AsyncTask<String, Void, String>{


        @Override
        protected String doInBackground(String... strings) {
            request  = new VolleyRequests();
            request.sendRequest(Settings.Secure.getString(Sync.this.getContentResolver(), Settings.Secure.ANDROID_ID),Sync.this);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
*/
    /*private class SyncSet extends AsyncTask<String, Void, String>{


        @Override
        protected String doInBackground(String... strings) {
            request  = new VolleyRequests();
            request.setInvoices(Sync.this);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }*/
}
