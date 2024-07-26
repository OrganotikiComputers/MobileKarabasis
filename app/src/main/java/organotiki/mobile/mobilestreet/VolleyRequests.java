package organotiki.mobile.mobilestreet;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.AlternateCode;
import organotiki.mobile.mobilestreet.objects.Bank;
import organotiki.mobile.mobilestreet.objects.Bookmark;
import organotiki.mobile.mobilestreet.objects.BrowserCustomer;
import organotiki.mobile.mobilestreet.objects.Company;
import organotiki.mobile.mobilestreet.objects.CompanySite;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.CustomerDetail;
import organotiki.mobile.mobilestreet.objects.CustomerDetailRow;
import organotiki.mobile.mobilestreet.objects.CustomerDetailTab;
import organotiki.mobile.mobilestreet.objects.Deposit;
import organotiki.mobile.mobilestreet.objects.FInvoice;
import organotiki.mobile.mobilestreet.objects.FInvoiceLine;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.GpsCoordinates;
import organotiki.mobile.mobilestreet.objects.Invoice;
import organotiki.mobile.mobilestreet.objects.InvoiceLine;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;
import organotiki.mobile.mobilestreet.objects.Item;
import organotiki.mobile.mobilestreet.objects.OnlineReportType;
import organotiki.mobile.mobilestreet.objects.ReturnBalance;
import organotiki.mobile.mobilestreet.objects.Setting;
import organotiki.mobile.mobilestreet.objects.UserEvents;

import static java.lang.Math.abs;

/**
 * Created by Thanasis on 1/7/2016.
 */
public class VolleyRequests {

    private int jsonSize = 1000;
    Realm realm = Realm.getDefaultInstance();
    GlobalVar gVar = realm.where(GlobalVar.class).findFirst();
    int ciSuccess, ciFail, fiSuccess, fiFail, cSuccess, cFail;
    private Context mContext;
    private Communicator comm;
    private RequestQueue requestQueue;
    String CmpDescription, CmpSiteDescription;
    DecimalFormat decim = new DecimalFormat("0.00");

    //region Check if Device is Authenticated and Updated
    void sendAuthenticationRequest(Context context) {
        try {

            comm = (Communicator) context;
            mContext = context;
            String deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            HashMap<String, String> params = new HashMap<>();
            String url = gVar.getOnlineIP() + "SenService/GetSenDevice?DevID=" + deviceId + "&VerNum=" + gVar.getVerNum();
            Log.d("asdfg", params.toString());
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                VolleyLog.v("asdfg", response.toString(4));
                                Log.d("asdfg", String.valueOf(response));
                                checkAuthentication(response);
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    JSONObject object = new JSONObject();
                    try {
                        object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    comm.respondVolleyRequestFinished(1, object);
                    VolleyLog.e("asdfg", error.getMessage());
                    Log.e("asdfg", error.getMessage(), error);
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }


    private void checkAuthentication(JSONObject json) {
        try {
            String authentication;
            JSONObject object = new JSONObject();
            if (!json.isNull("VerifyDeviceResult")) {
                authentication = json.getString("VerifyDeviceResult");
                final String sParts[] = authentication.split("\\.");
                final int l = sParts.length;
                if (l == 5) {
                    if (sParts[0].equals("1")) {
                        String mParts[] = gVar.getVerNum().split("\\.");
                        if (sParts[1].equals(mParts[0]) && sParts[2].equals(mParts[1]) && sParts[3].equals(mParts[2])) {
                            if (sParts[4].equals(mParts[3])) {
                                object.put("Message", "H συσκευή είναι έτοιμη για χρήση.");
                                comm.respondVolleyRequestFinished(0, object);
                                if (mContext instanceof Sync) {
                                    sendVolleyRequest("SenService/GetSENSetting", "");
                                }
                            } else {
                                object.put("Message", "Βρέθηκε νεότερη έκδοση της εφαρμογής.\nΠαρακαλώ εγκαταστήστε την όσο το δυνατόν συντομότερο.");
                                comm.respondVolleyRequestFinished(0, object);
                                if (mContext instanceof Sync) {
                                    sendVolleyRequest("SenService/GetSENSetting", "");
                                }
                            }
                        } else {
                            object.put("Message", "Βρέθηκε νεότερη έκδοση της εφαρμογής που πρέπει να εγκατασταθεί άμεσα.\nΠαρακαλώ εγκαταστήστε την αφού συγχρονίσετε τα παραστατικά και προσπαθείστε ξανά");
                            comm.respondVolleyRequestFinished(1, object);
                        }
                    } else {
                        object.put("Message", "H συσκευή δεν είναι δηλωμένη.");
                        comm.respondVolleyRequestFinished(1, object);
                    }
                } else {
                    object.put("Message", "Υπήρξε κάποιο πρόβλημα με το Διακομιστή.");
                    comm.respondVolleyRequestFinished(1, object);
                }
            } else {
                object.put("Message", "Υπήρξε κάποιο πρόβλημα με το Διακομιστή.");
                comm.respondVolleyRequestFinished(1, object);
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    //endregion

    //region Send Simple Requests
    void sendRequest(Context context, String requestText, String requeststring) {
        comm = (Communicator) context;
        mContext = context;
        sendVolleyRequest(requestText, requeststring);
    }

    private void sendVolleyRequest(final String request, final String n) {
        try {
            HashMap<String, String> params = new HashMap<>();
            String url = gVar.getOnlineIP() + request;
            url += n.equals("") ? "" : "?Row=" + n + "&Date=" + gVar.getLastUpdate();
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                VolleyLog.d("asdfg", response.toString(4));
                                Log.d("asdfg", String.valueOf(response));

                                if (request.contains("GetMobCustomer")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetCustomersResult");
                                    //                                Log.d("asdfg", String.valueOf(jsonArray));
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    realm.createOrUpdateAllFromJson(Customer.class, jsonArray);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                                Log.d("asdfg", "number of customers: " + String.valueOf(realm.where(Customer.class).count()));
                                            }
                                        });
//                                        Toast.makeText(mContext, "Περάστηκαν οι πρώτοι "+ n +" πελάτες.", Toast.LENGTH_SHORT).show();
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("Message", "Περάστηκαν οι πρώτοι " + String.valueOf(Integer.parseInt(n) + jsonSize) + " πελάτες.");
                                        comm.respondVolleyRequestFinished(0, jsonObject);
                                        sendVolleyRequest("SenService/GetMobCustomer", String.valueOf(Integer.parseInt(n) + jsonSize));
                                    } else {
//                                        Toast.makeText(mContext, "Ο συγχρονισμός των πελατών ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("Message", "Ο συγχρονισμός των πελατών ολοκληρώθηκε.");
                                        comm.respondVolleyRequestFinished(0, jsonObject);
                                        sendVolleyRequest("SenService/GetMobAddress", "0");
                                    }
                                } else if (request.contains("GetMobAddress")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetAddressesResult");
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    realm.createOrUpdateAllFromJson(Address.class, jsonArray);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                                Log.d("asdfg", "number of addresses: " + String.valueOf(realm.where(Address.class).count()));
                                            }
                                        });
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("Message", "Περάστηκαν οι πρώτες " + String.valueOf(Integer.parseInt(n) + jsonSize) + " διευθύνσεις πελατών.");
                                        comm.respondVolleyRequestFinished(0, jsonObject);
//                                        Toast.makeText(mContext, "Περάστηκαν οι πρώτες "+ String.valueOf(Integer.parseInt(n) + jsonSize) +" διευθύνσεις πελατών.", Toast.LENGTH_SHORT).show();
                                        sendVolleyRequest("SenService/GetMobAddress", String.valueOf(Integer.parseInt(n) + jsonSize));
                                    } else {
                                        JSONObject jsonObject = new JSONObject();
                                        Calendar cal = Calendar.getInstance();
                                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                        final String today = df.format(cal.getTime());
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                gVar.setLastUpdate(today);
                                                gVar.setErrorItem("0");
                                                gVar.setErrorAlternateCode("0");
                                            }
                                        });
                                        jsonObject.put("Message", "Ο συγχρονισμός των διευθύνσεων των πελατών ολοκληρώθηκε.\n O συγχρονισμός ολοκληρώθηκε, μπορείτε να κλείσετε το παράθυρο.");
                                        comm.respondVolleyRequestFinished(1, jsonObject);
//                                        Toast.makeText(mContext, "Ο συγχρονισμός των διευθύνσεων των πελατών ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
//                                        comm.respondVolleyRequestFinished(0, new JSONObject());
                                    }
                                } else if (request.contains("GetMobItem")) {
                                    if (response.isNull("getItemsResult")) {
                                        final JSONArray jsonArray = response.getJSONArray("GetItemsResult");
                                        if (jsonArray.length() > 0) {
                                            realm.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    try {
                                                        realm.createOrUpdateAllFromJson(Item.class, jsonArray);
                                                        gVar.setErrorItem(n);
                                                    } catch (Exception e) {
                                                        Log.e("asdfg", e.getMessage(), e);
                                                    }
                                                    Log.d("asdfg", "number of items: " + String.valueOf(realm.where(Item.class).count()));
                                                }
                                            });
                                            JSONObject jsonObject = new JSONObject();
                                            jsonObject.put("Message", "Περάστηκαν τα πρώτα " + String.valueOf(Integer.parseInt(n) + jsonSize) + " είδη.");
                                            comm.respondVolleyRequestFinished(0, jsonObject);
                                            //                                        Toast.makeText(mContext, "Περάστηκαν τα πρώτα "+ n +" είδη.", Toast.LENGTH_SHORT).show();
                                            sendVolleyRequest("SenService/GetMobItem", (String.valueOf(Integer.parseInt(n) + jsonSize)));
                                        } else {
                                            JSONObject jsonObject = new JSONObject();
                                            jsonObject.put("Message", "Ο συγχρονισμός των ειδών ολοκληρώθηκε.");
                                            comm.respondVolleyRequestFinished(0, jsonObject);
                                            //                                        Toast.makeText(mContext, "Ο συγχρονισμός των ειδών ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                            sendVolleyRequest("SenService/GetMobAlterCode", gVar.getErrorAlternateCode());
                                        }
                                    } else {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("Message", "O συγχρονισμός ολοκληρώθηκε, μπορείτε να κλείσετε το παράθυρο.");
                                        comm.respondVolleyRequestFinished(1, jsonObject);
                                    }
                                } else if (request.contains("GetMobAlterCode")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetAlterCodesResult");
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    realm.createOrUpdateAllFromJson(AlternateCode.class, jsonArray);
                                                    gVar.setErrorAlternateCode(n);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                                Log.d("asdfg", "number of Alternate Codes: " + String.valueOf(realm.where(AlternateCode.class).count()));
                                            }
                                        });
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("Message", "Περάστηκαν οι πρώτοι " + String.valueOf(Integer.parseInt(n) + jsonSize) + " εναλλακτικοί κωδικοί.");
                                        comm.respondVolleyRequestFinished(0, jsonObject);
//                                        Toast.makeText(mContext, "Περάστηκαν οι πρώτοι "+ String.valueOf(Integer.parseInt(n) + jsonSize) +" εναλλακτικοί κωδικοί.", Toast.LENGTH_SHORT).show();
                                        sendVolleyRequest("SenService/GetMobAlterCode", String.valueOf(Integer.parseInt(n) + jsonSize));
                                    } else {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("Message", "Ο συγχρονισμός των εναλλακτικών κωδικών ολοκληρώθηκε.");
                                        comm.respondVolleyRequestFinished(0, jsonObject);
//                                        Toast.makeText(mContext, "Ο συγχρονισμός των εναλλακτικών κωδικών ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
//                                        getImages();
                                        sendVolleyRequest("SenService/GetMobCustomer", "0");
                                    }
                                }else if (request.contains("GetSENSetting")) {
                            final JSONArray jsonArray5 = response.getJSONArray("GetSettingsResult");
                            Log.d("asdfg", String.valueOf(jsonArray5));
                            if (jsonArray5.length() > 0) {
                                VolleyRequests.this.realm.executeTransaction(new Realm.Transaction() {
                                    public void execute(Realm realm) {
                                        try {
                                            realm.createOrUpdateAllFromJson(Setting.class, jsonArray5);
                                        } catch (Exception e) {
                                            Log.e("asdfg", e.getMessage(), e);
                                        }
                                    }
                                });
                                JSONObject jsonObject10 = new JSONObject();
                                jsonObject10.put("Message", "Ο συγχρονισμός των ρυθμίσεων ολοκληρώθηκε.");
                                VolleyRequests.this.comm.respondVolleyRequestFinished(0, jsonObject10);
                            }
                            VolleyRequests.this.sendVolleyRequest("SenService/GetMobBank", "");
                            Log.d("asdfg", "number of Settings: " + VolleyRequests.this.realm.where(Setting.class).count());
                        } 
                                //region Unused synchronization requests
                                /*else if (request.contains("GetSyncUser")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetSyncUserResult");
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    Log.d("asdfg", "number of Users: " + String.valueOf(realm.where(User.class).count()));
                                                    realm.createOrUpdateAllFromJson(User.class, jsonArray);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                                Log.d("asdfg", "number of Users: " + String.valueOf(realm.where(User.class).count()));
                                            }
                                        });
                                    }
                                    Toast.makeText(mContext, "Ο συγχρονισμός των πωλητών ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                    if (gVar.getMyUser().getID().equals("user")) {
//                                        comm.respondVolleyRequestFinished(0, new JSONObject());
                                        sendVolleyRequest("SenService/GetMobBank", "");
                                    } else {
//                                        sendVolleyRequest("GetSyncCategory", "");
                                        sendVolleyRequest("SenService/GetMobBank", "");
                                    }
                                } *//*else if (request.contains("GetMobVAT")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetVATsResult");
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    realm.createOrUpdateAllFromJson(VAT.class, jsonArray);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                                Log.d("asdfg", "number of VAT: " + String.valueOf(realm.where(VAT.class).count()));
                                            }
                                        });
                                    }
                                    Toast.makeText(mContext, "Ο συγχρονισμός των ΦΠΑ ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                    sendVolleyRequest("SenService/GetMobInvoiceType", "");
                                }*/ /*else if (request.contains("GetMobInvoiceType")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetInvoiceTypesResult");
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    realm.createOrUpdateAllFromJson(InvoiceType.class, jsonArray);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                                Log.d("asdfg", "number of Invoice Types: " + String.valueOf(realm.where(InvoiceType.class).count()));
                                            }
                                        });
                                    }
                                    Toast.makeText(mContext, "Ο συγχρονισμός των τύπων παραστατικού ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                    sendVolleyRequest("SenService/GetMobPayTerm", "");
                                }*//* else if (request.contains("GetMobPayTerm")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetPayTermsResult");
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    realm.createOrUpdateAllFromJson(PaymentTerm.class, jsonArray);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                                Log.d("asdfg", "number of Payment Terms: " + String.valueOf(realm.where(PaymentTerm.class).count()));
                                            }
                                        });
                                    }
                                    Toast.makeText(mContext, "Ο συγχρονισμός των τρόπων πληρωμής ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                    sendVolleyRequest("SenService/GetMobItem", "0");
                                } *//*else if (request.contains("GetMobMeasurementUnit")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetMeasurementUnitsResult");
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    realm.createOrUpdateAllFromJson(MeasurementUnit.class, jsonArray);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                            }
                                        });
                                        Toast.makeText(mContext, "Ο συγχρονισμός των μονάδων μέτρησης ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                    }
                                    sendVolleyRequest("SenService/GetMobVAT", "");
                                    Log.d("asdfg", "number of Units: " + String.valueOf(realm.where(MeasurementUnit.class).count()));
                                } *//*else if (request.contains("GetMobCategory")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetCategoriesResult");
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    realm.createOrUpdateAllFromJson(ItemCategory.class, jsonArray);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                            }
                                        });
                                        Toast.makeText(mContext, "Ο συγχρονισμός των κατηγοριών των ειδών ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                    }
                                    sendVolleyRequest("SenService/GetMobMeasurementUnit", "");
                                    Log.d("asdfg", "number of Item Categories: " + String.valueOf(realm.where(ItemCategory.class).count()));
                                } */
                                //endregion
                                else if (request.contains("GetMobBank")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetBanksResult");
                                    Log.d("asdfg", String.valueOf(jsonArray));
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    realm.createOrUpdateAllFromJson(Bank.class, jsonArray);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                            }
                                        });
//                                        Toast.makeText(mContext, "Ο συγχρονισμός των τραπεζών ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("Message", "Ο συγχρονισμός των τραπεζών ολοκληρώθηκε.");
                                        comm.respondVolleyRequestFinished(0, jsonObject);
                                    }
                                    sendVolleyRequest("SenService/GetMobCompany", "");//sendVolleyRequest("GetSyncCompany", "");
                                    Log.d("asdfg", "number of Banks: " + String.valueOf(realm.where(Bank.class).count()));
                                } else if (request.contains("GetMobCompany")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetCompaniesResult");
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    if (gVar.getMyCompanySite() != null) {
                                                        CmpDescription = gVar.getMyCompanySite().getMyCompany().getDescription();
                                                    } else {
                                                        CmpDescription = null;
                                                    }
                                                    realm.createOrUpdateAllFromJson(Company.class, jsonArray);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                            }
                                        });
                                        if (!(mContext instanceof SplashScreen)) {
                                            JSONObject jsonObject = new JSONObject();
                                            jsonObject.put("Message", "Ο συγχρονισμός των εταιριών ολοκληρώθηκε.");
                                            comm.respondVolleyRequestFinished(0, jsonObject);
//                                            Toast.makeText(mContext, "Ο συγχρονισμός των εταιριών ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    sendVolleyRequest("SenService/GetMobSite", "");
                                    //comm.respondVolleyRequestFinished(new JSONArray());
                                    Log.d("asdfg", "number of Companies: " + String.valueOf(realm.where(Company.class).count()));
                                } else if (request.contains("GetMobSite")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetSitesResult");
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    if (gVar.getMyCompanySite() != null) {
                                                        CmpSiteDescription = gVar.getMyCompanySite().getDescription();
                                                    } else {
                                                        CmpSiteDescription = null;
                                                    }
                                                    realm.createOrUpdateAllFromJson(CompanySite.class, jsonArray);
                                                    if (CmpDescription != null && CmpSiteDescription != null) {
                                                        gVar.setMyCompanySite(realm.where(CompanySite.class).equalTo("myCompany.Description", CmpDescription).equalTo("Description", CmpSiteDescription).findFirst());
                                                    }
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                            }
                                        });
                                        if (!(mContext instanceof SplashScreen)) {
                                            JSONObject jsonObject = new JSONObject();
                                            jsonObject.put("Message", "Ο συγχρονισμός των καταστημάτων ολοκληρώθηκε.");
                                            comm.respondVolleyRequestFinished(0, jsonObject);
//                                            Toast.makeText(mContext, "Ο συγχρονισμός των καταστημάτων ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    if (!(mContext instanceof SplashScreen)) {
                                        sendVolleyRequest("SenService/GetMobItem", gVar.getErrorItem());
                                    }
                                    //comm.respondVolleyRequestFinished(new JSONArray());
                                    Log.d("asdfg", "number of Company Sites: " + String.valueOf(realm.where(CompanySite.class).count()));
                                } else if (request.contains("GetSenOnlineReport")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetOnlineReportResult");
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    realm.delete(OnlineReportType.class);
                                                    realm.createOrUpdateAllFromJson(OnlineReportType.class, jsonArray);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                            }
                                        });
                                    }
                                    Log.d("asdfg", "number of Online Report Types: " + String.valueOf(realm.where(OnlineReportType.class).count()));
                                    comm.respondVolleyRequestFinished(2, new JSONObject());
                                }
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    comm.respondVolleyRequestFinished(1, object);
//                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("Error: ", error.getMessage());
                }
            });
            ////itan 15000 stin ekdosi 1.1.1.34 to timeout
            req.setRetryPolicy(new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region LogIn
    void LogIn(Context context, String username, String password) {
        mContext = context;
        comm = (Communicator) mContext;
        HashMap<String, String> params = new HashMap<>();
        params.put("Username", username);
        params.put("Password", password);
        String url = gVar.getOnlineIP() + "SenService/PostSenLogIn";
        Log.d("asdfg", params.toString());
        Log.d("asdfg", url);
        JsonObjectRequest req = new JsonObjectRequest(url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            VolleyLog.v("asdfg", response.toString(4));
                            Log.d("asdfg", String.valueOf(response));
                            comm.respondVolleyRequestFinished(2, response);
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext.getApplicationContext(), "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                VolleyLog.e("asdfg", error.getMessage());
                Log.e("asdfg", error.getMessage(), error);
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(req);
    }
    //endregion

    //region Get Customer Details
    public void getCustomerDetails(Context context, String request, String CusID) {
        try {
            this.mContext = context;
            this.comm = (Communicator) context;
            HashMap<String, String> params = new HashMap<>();
            String url = (this.gVar.getOnlineIP() + request) + "?CusID=" + CusID;
            Log.d("asdfg", url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(0, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    try {
                        Log.d("asdfg", String.valueOf(response));
                        VolleyLog.d("asdfg", response.toString(4));
                        final JSONArray jsonArray = response.getJSONArray("GetCustomerDetailsResult");
                        if (jsonArray.length() > 0) {
                            VolleyRequests.this.realm.executeTransaction(new Realm.Transaction() {
                                public void execute(Realm realm) {
                                    try {
                                        realm.delete(CustomerDetail.class);
                                        realm.delete(CustomerDetailRow.class);
                                        realm.delete(CustomerDetailTab.class);
                                        realm.createAllFromJson(CustomerDetailTab.class, jsonArray);
                                    } catch (Exception e) {
                                        Log.e("asdfg", e.getMessage(), e);
                                    }
                                }
                            });
                        }
                        Log.d("asdfg", "number of Customer Detail Tabs: " + VolleyRequests.this.realm.where(CustomerDetailTab.class).count());
                        Log.d("asdfg", "number of Customer Detail Rows: " + VolleyRequests.this.realm.where(CustomerDetailRow.class).count());
                        Log.d("asdfg", "number of Customer Details: " + VolleyRequests.this.realm.where(CustomerDetail.class).count());
                        VolleyRequests.this.comm.respondVolleyRequestFinished(1, new JSONObject());
                    } catch (Exception e) {
                        JSONObject object = new JSONObject();
                        try {
                            object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                        } catch (Exception e1) {
                            Log.e("asdfg", e1.getMessage(), e1);
                        }
                        VolleyRequests.this.comm.respondVolleyRequestFinished(1, object);
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    VolleyRequests.this.comm.respondVolleyRequestFinished(1, object);
                    VolleyLog.e("Error: ", error.getMessage(), error);
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (this.requestQueue == null) {
                this.requestQueue = Volley.newRequestQueue(this.mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            this.requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    //endregion

    //region Get User Customers
    void getUserCustomers(Context context) {
        try {
            mContext = context;
            comm = (Communicator) context;
            HashMap<String, String> params = new HashMap<>();
            String url = gVar.getOnlineIP() + "SenService/GetSenUserCustomers?UserName=" + gVar.getMyUser().getUsername();
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("asdfg", String.valueOf(response));
                                VolleyLog.d("asdfg", response.toString(4));
                                final JSONArray jsonArray = response.getJSONArray("GetUserCustomersResult");
                                int length = jsonArray.length();
                                if (length > 0) {
                                    realm.executeTransaction(new Realm.Transaction() {
										public void execute(Realm realm) {
										try {
											realm.createOrUpdateAllFromJson(BrowserCustomer.class, jsonArray);
										} catch (Exception e) {
											Log.e("asdfg", e.getMessage(), e);
										}
									}
									});
                                    
                                }else{
                                    JSONObject object = new JSONObject();
                                    try {
                                        object.put("Message", "Δεν βρέθηκαν πελάτες.");
                                    } catch (Exception e1) {
                                        Log.e("asdfg", e1.getMessage(), e1);
                                    }
                                    comm.respondVolleyRequestFinished(1, object);
                                }
                                Log.d("asdfg", "number of Browser Customer: " + String.valueOf(realm.where(BrowserCustomer.class).count()));
                                comm.respondVolleyRequestFinished(3, new JSONObject());
                            } catch (Exception e) {
                                JSONObject object = new JSONObject();
                                try {
                                    object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                                } catch (Exception e1) {
                                    Log.e("asdfg", e1.getMessage(), e1);
                                }
                                comm.respondVolleyRequestFinished(1, object);
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    comm.respondVolleyRequestFinished(1, object);
//                    comm.respondVolleyRequestFinished(0, new JSONObject());
//                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("Error: ", error.getMessage(), error);
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Get Bookmarks
    void getBookmarks(Context context) {
        try {
            mContext = context;
            comm = (Communicator) context;
            HashMap<String, String> params = new HashMap<>();
            String url = gVar.getOnlineIP() + "SenService/GetSenBookmarks";
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("asdfg", String.valueOf(response));
                                VolleyLog.d("asdfg", response.toString(4));
                                final JSONArray jsonArray = response.getJSONArray("GetBookmarksResult");
                                if (jsonArray.length() > 0) {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            try {
                                                realm.delete(Bookmark.class);
                                                realm.createOrUpdateAllFromJson(Bookmark.class, jsonArray);
                                            } catch (Exception e) {
                                                Log.e("asdfg", e.getMessage(), e);
                                            }
                                        }
                                    });
                                }
                                Log.d("asdfg", "number of Bookmarks: " + String.valueOf(realm.where(CustomerDetail.class).count()));
                                comm.respondVolleyRequestFinished(0, new JSONObject());
                            } catch (Exception e) {
                                JSONObject object = new JSONObject();
                                try {
                                    object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                                } catch (Exception e1) {
                                    Log.e("asdfg", e1.getMessage(), e1);
                                }
                                comm.respondVolleyRequestFinished(-1, object);
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    comm.respondVolleyRequestFinished(-1, object);
//                    comm.respondVolleyRequestFinished(0, new JSONObject());
//                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("Error: ", error.getMessage(), error);
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Get Bookmark Content
    void getBookmark(Context context, String ID) {
        try {
            mContext = context;
            comm = (Communicator) context;
            HashMap<String, String> params = new HashMap<>();
            String url = gVar.getOnlineIP() + "SenService/GetSenBookmark?ID="+ID;
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("asdfg", String.valueOf(response));
                                VolleyLog.d("asdfg", response.toString(4));
                                comm.respondVolleyRequestFinished(1, response);
                            } catch (Exception e) {
                                JSONObject object = new JSONObject();
                                try {
                                    object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                                } catch (Exception e1) {
                                    Log.e("asdfg", e1.getMessage(), e1);
                                }
                                comm.respondVolleyRequestFinished(-1, object);
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    comm.respondVolleyRequestFinished(-1, object);
//                    comm.respondVolleyRequestFinished(0, new JSONObject());
//                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("Error: ", error.getMessage(), error);
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Get Bookmark File
    void getBookmarkFile(Context context, String ID) {
        try {
            mContext = context;
            comm = (Communicator) context;
            HashMap<String, String> params = new HashMap<>();
            String url = gVar.getOnlineIP() + "/SenService/GetSenBookmark?ID="+ID;
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("asdfg", String.valueOf(response));
                                VolleyLog.d("asdfg", response.toString(4));
                                comm.respondVolleyRequestFinished(2, response);
                            } catch (Exception e) {
                                JSONObject object = new JSONObject();
                                try {
                                    object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                                } catch (Exception e1) {
                                    Log.e("asdfg", e1.getMessage(), e1);
                                }
                                comm.respondVolleyRequestFinished(-1, object);
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    comm.respondVolleyRequestFinished(-1, object);
//                    comm.respondVolleyRequestFinished(0, new JSONObject());
//                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("Error: ", error.getMessage(), error);
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Send Email of Synchronization

    void sendSyncEmail(Context context) {
        try {
            comm = (Communicator) context;
            mContext = context;
            RealmResults<Invoice> sentInvoices = realm.where(Invoice.class).equalTo("isSent", true).findAll();
            for (final Invoice sentInvoice : sentInvoices) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            realm.where(InvoiceLine.class).equalTo("myInvoice.ID", sentInvoice.getID()).findAll().deleteAllFromRealm();
                            sentInvoice.deleteFromRealm();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });
            }
            RealmResults<FInvoice> sentFInvoices = realm.where(FInvoice.class).equalTo("isSent", true).findAll();
            for (final FInvoice sentFInvoice : sentFInvoices) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", sentFInvoice.getID()).findAll().deleteAllFromRealm();
                            sentFInvoice.deleteFromRealm();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });
            }

//            sendInvoicestoSen();
            final RealmResults<Invoice> invoices = realm.where(Invoice.class).equalTo("isFinal", true).equalTo("Username", gVar.getMyUser().getUsername()).findAll();
            Log.d("asdfg", String.valueOf(invoices));
            JSONArray invoiceArray = new JSONArray();
            for (Invoice inv : invoices) {
                invoiceArray.put(inv.getID());
            }
            final RealmResults<FInvoice> finvoices = realm.where(FInvoice.class).equalTo("isFinal", true).equalTo("Username", gVar.getMyUser().getUsername()).findAll();
            Log.d("asdfg", String.valueOf(finvoices));
            JSONArray fInvoiceArray = new JSONArray();
            for (FInvoice finv : finvoices) {
                fInvoiceArray.put(finv.getID());
            }


            Double cashTotal = 0.0;
            int l = finvoices.size();
            for (int i = 0; i < l; i++) {
                if (finvoices.get(i).isFinal()) {
                    cashTotal += finvoices.get(i).getCash1() + finvoices.get(i).getCash2() + finvoices.get(i).getCash3() + finvoices.get(i).getCash4();

                }
            }
            cashTotal -= gVar.getMyUser().getTotalExpenses();
            Double currencyTotal = CalculateCurrencyTotal();
            Log.d("asdfg", "cash total: " + cashTotal + " currency total: " + currencyTotal);
            String message = "";

            if (invoices.size() + finvoices.size() <= 0) {
                message += "Δεν έχετε ολοκληρωμένα παραστατικά.";
            }
            Log.d("asdfg", "currencyTotal= " + decim.format(currencyTotal).replace(",", ".") + ", cashTotal= " + decim.format(cashTotal).replace(",", "."));
            if (Double.parseDouble(decim.format(currencyTotal).replace(",", ".")) <= Double.parseDouble(decim.format(cashTotal).replace(",", ".")) - 5 || Double.parseDouble(decim.format(currencyTotal).replace(",", ".")) >= Double.parseDouble(decim.format(cashTotal).replace(",", ".")) + 5) {
                message += message.equals("") ? "Το σύνολο των νομισμάτων απέχει πολύ από το σύνολο που θα έπρεπε να έχετε." : "\nΤο σύνολο των νομισμάτων απέχει πολύ από το σύνολο που θα έπρεπε να έχετε.";
            }
            Log.d("asdfg", "message=" + message);
			JSONArray jSONArray3 = new JSONArray();
			RealmList<Deposit> myDeposits = this.gVar.getMyUser().getMyDebosits();
			for (int i2 = 0; i2 < myDeposits.size(); i2++) {
				JSONObject jSONObject = new JSONObject();
				try {
					try {
						jSONObject.put("ID", myDeposits.get(i2).getID());
						jSONObject.put("Date", myDeposits.get(i2).getDate());
						jSONObject.put("Value", myDeposits.get(i2).getValue());
                        jSONObject.put("Account", myDeposits.get(i2).getAccount());
                    } catch (Exception e) {
						Log.e("asdfg", e.getMessage(), e);
						jSONArray3.put(jSONObject);
					}
					} catch (Exception e) {
						Log.e("asdfg", e.getMessage(), e);
						jSONArray3.put(jSONObject);
					}
					jSONArray3.put(jSONObject);
			}
			
			
            if (message.equals("")) {
                JSONObject joSync = new JSONObject();
                try {
                    joSync.put("ID", UUID.randomUUID().toString());
                    joSync.put("CompanyID", gVar.getMyCompanySite().getMyCompany().getID());
                    joSync.put("CompanySiteID", gVar.getMyCompanySite().getID());
                    joSync.put("User", gVar.getMyUser().getUsername().toUpperCase());
                    joSync.put("Name", gVar.getMyUser().getFullName());
                    joSync.put("Com", invoiceArray);
                    joSync.put("Fin", fInvoiceArray);
                    joSync.put("Hotel", gVar.getMyUser().getHotelExpenses());
                    joSync.put("Meal", gVar.getMyUser().getMealsExpenses());
                    joSync.put("Fuel", gVar.getMyUser().getFuelExpenses());
                    joSync.put("Misc", gVar.getMyUser().getMiscExpenses());
                    joSync.put("C500", gVar.getMyUser().getCurrency500());
                    joSync.put("C200", gVar.getMyUser().getCurrency200());
                    joSync.put("C100", gVar.getMyUser().getCurrency100());
                    joSync.put("C50", gVar.getMyUser().getCurrency50());
                    joSync.put("C20", gVar.getMyUser().getCurrency20());
                    joSync.put("C10", gVar.getMyUser().getCurrency10());
                    joSync.put("C5", gVar.getMyUser().getCurrency5());
                    joSync.put("C2", gVar.getMyUser().getCurrency2());
                    joSync.put("C1", gVar.getMyUser().getCurrency1());
                    joSync.put("C050", gVar.getMyUser().getCurrency050());
                    joSync.put("C020", gVar.getMyUser().getCurrency020());
                    joSync.put("C010", gVar.getMyUser().getCurrency010());
                    joSync.put("C005", gVar.getMyUser().getCurrency005());
                    joSync.put("Deposit", gVar.getMyUser().getDeposit());
					joSync.put("Deposits", jSONArray3);
                    joSync.put("CCheckValue", gVar.getMyUser().getCCheckValue());
                    joSync.put("CCheckCount", gVar.getMyUser().getCCheckCount());
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }


                HashMap<String, JSONObject> invoiceFinal = new HashMap<>();
                invoiceFinal.put("SyncEmail", joSync);

                String url = gVar.getOnlineIP() + "SenService/PostMobSyncSumUp";
                Log.d("asdfg", url);
                Log.d("asdfg", String.valueOf(new JSONObject(invoiceFinal)));
                JsonObjectRequest req = new JsonObjectRequest(url, new JSONObject(invoiceFinal),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    VolleyLog.v("asdfg", response.toString());
                                    Log.d("asdfg", response.toString());
                                    boolean isSaved = response.getBoolean("SyncSumUpResult");
                                    if (!isSaved) {
//                                    Toast.makeText(mContext, "Το E-mail δεν στάλθηκε. Προσπαθήστε ξανά.", Toast.LENGTH_LONG).show();
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("Message", "Το E-mail δεν στάλθηκε. Προσπαθήστε ξανά.");
                                        comm.respondVolleyRequestFinished(1, jsonObject);
                                        return;
                                    }
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                gVar.getMyUser().setCurrency500(0);
                                                gVar.getMyUser().setCurrency200(0);
                                                gVar.getMyUser().setCurrency100(0);
                                                gVar.getMyUser().setCurrency50(0);
                                                gVar.getMyUser().setCurrency20(0);
                                                gVar.getMyUser().setCurrency10(0);
                                                gVar.getMyUser().setCurrency5(0);
                                                gVar.getMyUser().setCurrency2(0);
                                                gVar.getMyUser().setCurrency1(0);
                                                gVar.getMyUser().setCurrency050(0);
                                                gVar.getMyUser().setCurrency020(0);
                                                gVar.getMyUser().setCurrency010(0);
                                                gVar.getMyUser().setCurrency005(0);
                                                gVar.getMyUser().setDeposit(0.0);
                                                gVar.getMyUser().setCCheckValue(0.0);
                                                gVar.getMyUser().setCCheckCount(0);
                                                gVar.getMyUser().setHotelExpenses(0.0);
                                                gVar.getMyUser().setMealsExpenses(0.0);
                                                gVar.getMyUser().setFuelExpenses(0.0);
                                                gVar.getMyUser().setMiscExpenses(0.0);
												gVar.getMyUser().setMyDebosits(new RealmList());
                                            }
                                        });
//                                    Toast.makeText(mContext, "To E-mail στάλθηκε επιτυχώς.", Toast.LENGTH_LONG).show();
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("Message", "To E-mail στάλθηκε επιτυχώς.");
                                        comm.respondVolleyRequestFinished(0, jsonObject);
                                        sendInvoicesToSen();

                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                            comm.respondVolleyRequestFinished(1, jsonObject);
                            VolleyLog.e("Error: ", error.getMessage());
                            Log.e("asdfg", error.getMessage(), error);
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });
                req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                if (requestQueue == null) {
                    requestQueue = Volley.newRequestQueue(mContext);
                    Log.d("asdfg", "Requesting new queue");
                }
                requestQueue.add(req);
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Message", message);
                comm.respondVolleyRequestFinished(1, jsonObject);
                /*SendCoordinates();*/
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private Double CalculateCurrencyTotal() {
        double currency500 = (double) this.gVar.getMyUser().getCurrency500();
        Double.isNaN(currency500);
        Double sub500 = Double.valueOf(currency500 * 500.0d);
        double currency200 = (double) this.gVar.getMyUser().getCurrency200();
        Double.isNaN(currency200);
        Double sub200 = Double.valueOf(currency200 * 200.0d);
        double currency100 = (double) this.gVar.getMyUser().getCurrency100();
        Double.isNaN(currency100);
        Double sub100 = Double.valueOf(currency100 * 100.0d);
        double currency50 = (double) this.gVar.getMyUser().getCurrency50();
        Double.isNaN(currency50);
        Double sub50 = Double.valueOf(currency50 * 50.0d);
        double currency20 = (double) this.gVar.getMyUser().getCurrency20();
        Double.isNaN(currency20);
        Double sub20 = Double.valueOf(currency20 * 20.0d);
        double currency10 = (double) this.gVar.getMyUser().getCurrency10();
        Double.isNaN(currency10);
        Double sub10 = Double.valueOf(currency10 * 10.0d);
        double currency5 = (double) this.gVar.getMyUser().getCurrency5();
        Double.isNaN(currency5);
        Double sub5 = Double.valueOf(currency5 * 5.0d);
        double currency2 = (double) this.gVar.getMyUser().getCurrency2();
        Double.isNaN(currency2);
        Double sub2 = Double.valueOf(currency2 * 2.0d);
        Double sub1 = Double.valueOf((double) this.gVar.getMyUser().getCurrency1());
        double currency050 = (double) this.gVar.getMyUser().getCurrency050();
        Double.isNaN(currency050);
        Double sub050 = Double.valueOf(currency050 * 0.5d);
        double currency020 = (double) this.gVar.getMyUser().getCurrency020();
        Double.isNaN(currency020);
        Double sub020 = Double.valueOf(currency020 * 0.2d);
        double currency010 = (double) this.gVar.getMyUser().getCurrency010();
        Double.isNaN(currency010);
        Double sub010 = Double.valueOf(currency010 * 0.1d);
        double currency005 = (double) this.gVar.getMyUser().getCurrency005();
        Double.isNaN(currency005);
        return Double.valueOf(sub500.doubleValue() + sub200.doubleValue() + sub100.doubleValue() + sub50.doubleValue() + sub20.doubleValue() + sub10.doubleValue() + sub5.doubleValue() + sub2.doubleValue() + sub1.doubleValue() + sub050.doubleValue() + sub020.doubleValue() + sub010.doubleValue() + Double.valueOf(currency005 * 0.05d).doubleValue() + this.gVar.getMyUser().getCCheckValue().doubleValue() + Double.valueOf(this.gVar.getMyUser().getMyDebosits().sum("Value").doubleValue()).doubleValue());
    }
    //endregion

    //region Send All Final Commercial Invoices to SEN

    private void sendInvoicesToSen() {
        try {
            ciSuccess = 0;
            ciFail = 0;
            final RealmResults<Invoice> invoices = realm.where(Invoice.class).equalTo("isFinal", true).equalTo("Username", gVar.getMyUser().getUsername()).findAll();
            Log.d("asdfg", String.valueOf(invoices));
            if (invoices.size() > 0) {
                sendInvoiceToSen(invoices, 0);
            } else {
                sendFInvoicesToSen();
            }

        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }


    private void sendInvoiceToSen(final RealmResults<Invoice> invoices, final int i) {
        try {
            final Invoice invoice = invoices.get(i);
            HashMap<String, JSONObject> params = new HashMap<>();

            String url = gVar.getLocalIP() + "SenService/GetMobSendComInvoice?InvoiceID=" + invoice.getID() + "&Username=" + gVar.getMyUser().getUsername() + "&Password=" + gVar.getMyUser().getPassword();
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                VolleyLog.v("asdfg", response.toString());
                                Log.d("asdfg", response.toString());
                                boolean isSaved = response.getBoolean("SendComInvoicesResult");
                                if (!isSaved) {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("Message", "Δεν στάλθηκε η επιστροφή με πελάτη: " + invoice.getMyCustomer().getName());
                                    comm.respondVolleyRequestFinished(0, jsonObject);
                                    ciFail++;
                                } else {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            invoice.setSent(true);
                                        }
                                    });
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("Message", "Στάλθηκε η επιστροφή με πελάτη: " + invoice.getMyCustomer().getName());
                                    comm.respondVolleyRequestFinished(0, jsonObject);
                                    ciSuccess++;
                                }

                                Log.d("asdfg", "i = " + i + ", invoice size = " + invoices.size());
                                if (i >= invoices.size() - 1) {
                                    RealmResults<Invoice> sentInvoices = realm.where(Invoice.class).equalTo("isSent", true).findAll();
                                    for (final Invoice sentInvoice : sentInvoices) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                realm.where(InvoiceLine.class).equalTo("myInvoice.ID", sentInvoice.getID()).findAll().deleteAllFromRealm();
                                                sentInvoice.deleteFromRealm();
                                            }
                                        });
                                    }
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("Message", ciSuccess + " επιστροφές περάστηκαν επιτυχώς, ενώ " + ciFail + " απέτυχαν.");
                                    comm.respondVolleyRequestFinished(0, jsonObject);
//                                            Toast.makeText(mContext, ciSuccess + " επιστροφές περάστηκαν επιτυχώς, ενώ " + ciFail + " απέτυχαν.", Toast.LENGTH_LONG).show();
                                    sendFInvoicesToSen();
                                } else {
                                    sendInvoiceToSen(invoices, i + 1);
                                }
                            } catch (Exception e) {
                                try {
                                    JSONObject object = new JSONObject();
                                    object.put("Message", "Υπήρξε κάποιο πρόβλημα. Προσπαθείστε ξανά.");
                                    comm.respondVolleyRequestFinished(1, object);
                                } catch (Exception e1) {
                                    Log.e("asdfg", e1.getMessage(), e1);
                                }
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        JSONObject object = new JSONObject();
                        object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                        comm.respondVolleyRequestFinished(1, object);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
//                        comm.respondVolleyRequestFinished(0, new JSONObject());
//                        Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("Error: ", error.getMessage());
                    Log.e("asdfg", error.getMessage(), error);
                }
            });

            req.setRetryPolicy(new DefaultRetryPolicy(120000, 0, 2));

            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
            //comm.respondVolleyRequestFinished(new JSONArray());
//            setFInvoices(mContext);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Send All Final Financial Invoices to SEN
    private void sendFInvoicesToSen() {
        try {
            fiSuccess = 0;
            fiFail = 0;
            final RealmResults<FInvoice> invoices = realm.where(FInvoice.class).equalTo("isFinal", true).equalTo("Username", gVar.getMyUser().getUsername()).findAll();
            Log.d("asdfg", String.valueOf(invoices));
            if (invoices.size() == 0) {
                //SendCoordinates();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Message", "O συγχρονισμός ολοκληρώθηκε, μπορείτε να κλείσετε το παράθυρο.");
                comm.respondVolleyRequestFinished(1, jsonObject);
            } else {
                sendFInvoiceToSen(invoices, 0);
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private void sendFInvoiceToSen(final RealmResults<FInvoice> invoices, final int i) {
        try {

            if (invoices.size() == 0) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Message", "O συγχρονισμός ολοκληρώθηκε, μπορείτε να κλείσετε το παράθυρο.");
                comm.respondVolleyRequestFinished(1, jsonObject);
            }
            final FInvoice invoice = invoices.get(i);
            HashMap<String, JSONObject> params = new HashMap<>();

            String url = gVar.getLocalIP() + "SenService/GetMobSendFinInvoice?InvoiceID=" + invoice.getID() + "&Username=" + gVar.getMyUser().getUsername() + "&Password=" + gVar.getMyUser().getPassword();
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                VolleyLog.v("asdfg", response.toString());
                                Log.d("asdfg", response.toString());
                                boolean isSaved = response.getBoolean("SendFinInvoicesResult");
                                if (!isSaved) {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("Message", "Δεν στάλθηκε η είσπραξη με πελάτη: " + invoice.getMyCustomer().getName());
                                    comm.respondVolleyRequestFinished(0, jsonObject);
                                    fiFail++;
                                } else {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            invoice.setSent(true);
                                        }
                                    });
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("Message", "Στάλθηκε η είσπραξη με πελάτη: " + invoice.getMyCustomer().getName());
                                    comm.respondVolleyRequestFinished(0, jsonObject);
                                    fiSuccess++;
                                }
                                if (i >= invoices.size() - 1) {
                                    Log.d("asdfg", "i = " + i + ", invoice size = " + invoices.size());
                                    RealmResults<FInvoice> sentFInvoices = realm.where(FInvoice.class).equalTo("isSent", true).findAll();
                                    for (final FInvoice sentFInvoice : sentFInvoices) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", sentFInvoice.getID()).findAll().deleteAllFromRealm();
                                                sentFInvoice.deleteFromRealm();
                                            }
                                        });
                                    }
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("Message", fiSuccess + " εισπράξεις περάστηκαν επιτυχώς, ενώ " + fiFail + " απέτυχαν.\nO συγχρονισμός ολοκληρώθηκε, μπορείτε να κλείσετε το παράθυρο.");
                                    comm.respondVolleyRequestFinished(1, jsonObject);
//                                            Toast.makeText(mContext, ciSuccess + " επιστροφές περάστηκαν επιτυχώς, ενώ " + ciFail + " απέτυχαν.", Toast.LENGTH_LONG).show();
                                } else {
                                    Log.d("asdfg", "i = " + i + ", invoice size = " + invoices.size());
                                    sendFInvoiceToSen(invoices, i + 1);
                                }
                            } catch (Exception e) {
                                try {
                                    JSONObject object = new JSONObject();
                                    object.put("Message", "Υπήρξε κάποιο πρόβλημα. Προσπαθείστε ξανά.");
                                    comm.respondVolleyRequestFinished(1, object);
                                } catch (Exception e1) {
                                    Log.e("asdfg", e1.getMessage(), e1);
                                }
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        RealmResults<Invoice> sentInvoices = realm.where(Invoice.class).equalTo("isSent", true).findAll();
                        for (final Invoice sentInvoice : sentInvoices) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.where(InvoiceLine.class).equalTo("myInvoice.ID", sentInvoice.getID()).findAll().deleteAllFromRealm();
                                    sentInvoice.deleteFromRealm();
                                }
                            });
                        }
                        RealmResults<FInvoice> sentFInvoices = realm.where(FInvoice.class).equalTo("isSent", true).findAll();
                        for (final FInvoice sentFInvoice : sentFInvoices) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", sentFInvoice.getID()).findAll().deleteAllFromRealm();
                                    sentFInvoice.deleteFromRealm();
                                }
                            });
                        }
                        JSONObject object = new JSONObject();
                        object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                        comm.respondVolleyRequestFinished(1, object);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
//                        comm.respondVolleyRequestFinished(0, new JSONObject());
//                        Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("Error: ", error.getMessage());
                    Log.e("asdfg", error.getMessage(), error);
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(120000, 0, 2));

            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
//            comm.respondVolleyRequestFinished(0, new JSONObject());
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Set Current Commercial Invoice
    void setInvoice(Context context, String ID) {
        try {
            comm = (Communicator) context;
            mContext = context;
            final RealmResults<Invoice> invoices = realm.where(Invoice.class).equalTo("ID", ID).findAll();
            for (final Invoice invoice : invoices) {
                JSONArray jaInvoiceLines = new JSONArray();
                JSONArray jaGuarantees = new JSONArray();
                RealmResults<InvoiceLine> lines = realm.where(InvoiceLine.class).equalTo("myInvoice.ID", invoice.getID()).findAll();
                int ilLength = lines.size();
                for (int i = 0; i < ilLength; i++) {
                    JSONObject il = new JSONObject();
                    try {
                        il.put("InvoiceID", invoice.getID());
                        il.put("GUID", lines.get(i).getID());
                        il.put("ID", lines.get(i).getMyItem().getID());
                        il.put("Price", lines.get(i).getWPrice());
						il.put("VPrice", lines.get(i).getPrice());
                        il.put("Quantity", lines.get(i).getQuantity());
                        il.put("Discount", lines.get(i).getDiscount());
                        il.put("Notes", lines.get(i).getNotes());
                        il.put("LastCompany", lines.get(i).getLastCompany() == null ? "" : lines.get(i).getLastCompany().getInAppID());
                        String parts[] = lines.get(i).getTypeCode().split("-");
                        il.put("Type", parts[0]);
                        il.put("WrhID", lines.get(i).getWrhID());
                        il.put("BraID", lines.get(i).getBraID());
                        il.put("Site", lines.get(i).getLastCompany().equals(gVar.getMyCompanySite().getMyCompany()) ? gVar.getMyCompanySite().getID() : realm.where(CompanySite.class).equalTo("myCompany.ID", lines.get(i).getLastCompany().getID()).equalTo("isMain", true).findFirst().getID());
                        il.put("isEY", lines.get(i).isEY());
                        il.put("isGuarantee", lines.get(i).isGuarantee());
                        il.put("fromCustomer", lines.get(i).isFromCustomer());
						il.put("DocID", lines.get(i).getDocID());
                        il.put("DocValue", lines.get(i).getDocValue());
                        il.put("ChargePapi", lines.get(i).getChargePapi());
                        il.put("IsExtraCharge", lines.get(i).isExtraCharge());
                        il.put("ExtraChargeValue", lines.get(i).getExtraChargeValue());
                        il.put("ExtraChargeLimit", lines.get(i).getExtraChargeLimit());
                        il.put("SmartCost",lines.get(i).getManageCost());

                        if (lines.get(i).isGuarantee()) {
                            JSONObject guar = new JSONObject();
                            guar.put("LineID", lines.get(i).getID());
                            guar.put("DosCode", lines.get(i).getDosCode());
                            guar.put("DocNumber", lines.get(i).getDocNumber());
                            guar.put("Date", lines.get(i).getLastDate());
                            guar.put("Manufacturer", lines.get(i).getManufacturer());
                            guar.put("Model", lines.get(i).getModel());
                            guar.put("Year1", lines.get(i).getYear1());
                            guar.put("EngineCode", lines.get(i).getEngineCode());
                            guar.put("Year2", lines.get(i).getYear2());
                            guar.put("KMTraveled", lines.get(i).getKMTraveled());
                            guar.put("ReturnCause", lines.get(i).getReturnCause());
                            guar.put("Observations", lines.get(i).getObservations());
                            jaGuarantees.put(guar);
                        }


                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    jaInvoiceLines.put(il);
                }

                JSONObject joIn = new JSONObject();
                try {
                    joIn.put("ID", invoice.getID());
                    joIn.put("Device", invoice.getDeviceID());
                    /*joIn.put("TypeID", invoice.getMyType().getID());
                    joIn.put("PaymentID", invoice.getMyPayment().getID());*/
                    joIn.put("CustomerID", invoice.getMyCustomer().getID());
                    joIn.put("Email1", "");
                    joIn.put("Email2", "");
                    joIn.put("InAppCmp", gVar.getMyCompanySite().getMyCompany().getInAppID());
                    joIn.put("Date", invoice.getDate());
                    joIn.put("Time", invoice.getTime());
                    joIn.put("Username", invoice.getUsername());
                    /*joIn.put("isMail", invoice.getMyTransaction().isMail());
                    joIn.put("isPrint", invoice.getMyTransaction().isPrint());*/
                    joIn.put("Notes", invoice.getNotes());
					joIn.put("ManagementCostPercent", invoice.getManagementCostPercent());
                    joIn.put("Lines", jaInvoiceLines);
                    joIn.put("Guarantees", jaGuarantees);

                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }


                HashMap<String, JSONObject> invoiceFinal = new HashMap<>();
                invoiceFinal.put("invoice", joIn);

                Log.d("asdfg", String.valueOf(invoiceFinal));
                String url = gVar.getOnlineIP();
                JsonObjectRequest req = new JsonObjectRequest(url + "SenService/PostSenComInvoice", new JSONObject(invoiceFinal),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    VolleyLog.v("asdfg", response.toString());
                                    Log.d("asdfg", response.toString());
                                    boolean isSaved = response.getInt("InsertComInvoiceResult") == 1;
                                    if (!isSaved) {
//                                        fiFail++;
                                        Toast.makeText(mContext, "Η επιστροφή δεν αποθηκεύτηκε. Προσπαθήστε ξανά.", Toast.LENGTH_LONG).show();
                                    } else {
//                                        fiSuccess++;
                                        Toast.makeText(mContext, "Η επιστροφή αποθηκεύτηκε επιτυχώς.", Toast.LENGTH_LONG).show();

                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                invoice.setFinal(true);
                                            }
                                        });
                                    }
                                    comm.respondVolleyRequestFinished(2, response);
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        comm.respondVolleyRequestFinished(2, new JSONObject());
                        Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                        VolleyLog.e("Error: ", error.getMessage());
                        Log.e("asdfg", error.getMessage(), error);
                    }
                });
                req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                if (requestQueue == null) {
                    requestQueue = Volley.newRequestQueue(mContext);
                    Log.d("asdfg", "Requesting new queue");
                }
                requestQueue.add(req);
            }
            //comm.respondVolleyRequestFinished(new JSONArray());
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Set Current Financial Invoice
    void setFInvoice(Context context, final String ID, String Email1, String Email2) {
        try {
            comm = (Communicator) context;
            mContext = context;
            final FInvoice invoice = realm.where(FInvoice.class).equalTo("ID", ID).notEqualTo("Total", 0.0).findFirst();
            JSONArray jaInvoiceLines = new JSONArray();
            RealmResults<FInvoiceLine> lines = realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", invoice.getID()).findAll();
            int ilLength = lines.size();
            for (int i = 0; i < ilLength; i++) {
                JSONObject il = new JSONObject();
                try {
                    il.put("InvoiceID", invoice.getID());
                    il.put("CompanyID", lines.get(i).getMyCompany().getInAppID());
                    il.put("BankID", lines.get(i).getMyBank() == null ? "" : lines.get(i).getMyBank().getID());
                    il.put("ExDate", lines.get(i).getExDate());
                    il.put("Number", lines.get(i).getNumber() == null ? "" : lines.get(i).getNumber());
                    il.put("Value", lines.get(i).getValue());
                    il.put("Site", lines.get(i).getMyCompany().equals(gVar.getMyCompanySite().getMyCompany()) ? gVar.getMyCompanySite().getID() : realm.where(CompanySite.class).equalTo("myCompany.ID", lines.get(i).getMyCompany().getID()).equalTo("isMain", true).findFirst().getID());

                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                jaInvoiceLines.put(il);
            }

            JSONObject joIn = new JSONObject();
            try {
                joIn.put("ID", invoice.getID());
                joIn.put("Device", invoice.getDeviceID());
                joIn.put("CustomerID", invoice.getMyCustomer().getID());
                joIn.put("Email1", Email1);
                joIn.put("Email2", Email2);
                joIn.put("Cash1", abs(invoice.getCash1()));
                joIn.put("Cash2", abs(invoice.getCash2()));
                joIn.put("Cash3", abs(invoice.getCash3()));
                joIn.put("Cash4", abs(invoice.getCash4()));
                joIn.put("DocNumber1", Long.parseLong(invoice.getDocNumber1().equals("") ? "0" : invoice.getDocNumber1()));
                joIn.put("DocNumber2", Long.parseLong(invoice.getDocNumber2().equals("") ? "0" : invoice.getDocNumber2()));
                joIn.put("DocNumber3", Long.parseLong(invoice.getDocNumber3().equals("") ? "0" : invoice.getDocNumber3()));
                joIn.put("DocNumber4", Long.parseLong(invoice.getDocNumber4().equals("") ? "0" : invoice.getDocNumber4()));
                joIn.put("Notes1", invoice.getNotes1());
                joIn.put("Notes2", invoice.getNotes2());
                joIn.put("Notes3", invoice.getNotes3());
                joIn.put("Notes4", invoice.getNotes4());
                joIn.put("Date", invoice.getDate());
                joIn.put("Time", invoice.getTime());
                joIn.put("Username", invoice.getUsername());
                joIn.put("FullName", gVar.getMyUser().getFullName());
                joIn.put("Notes", invoice.getNotes() == null ? "" : invoice.getNotes());
                joIn.put("Cmp1", "1");
                joIn.put("Cmp2", "2");
                joIn.put("Site1", gVar.getMyCompanySite().getMyCompany().getInAppID().equals("1") ? gVar.getMyCompanySite().getID() : realm.where(CompanySite.class).equalTo("myCompany.InAppID", "1").equalTo("isMain", true).findFirst().getID());
                joIn.put("Site2", gVar.getMyCompanySite().getMyCompany().getInAppID().equals("2") ? gVar.getMyCompanySite().getID() : realm.where(CompanySite.class).equalTo("myCompany.InAppID", "2").equalTo("isMain", true).findFirst().getID());
                joIn.put("ParentSite", gVar.getMyCompanySite().getID());
                joIn.put("IsFromCashier", false);
                joIn.put("IsReturn", invoice.getTotal() < 0);
                joIn.put("Lines", jaInvoiceLines);
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }


            HashMap<String, JSONObject> invoiceFinal = new HashMap<>();
            invoiceFinal.put("invoice", joIn);

            String url = gVar.getOnlineIP() + "SenService/PostSenFinInvoice";
            Log.d("asdfg", url + String.valueOf(new JSONObject(invoiceFinal)));
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(invoiceFinal),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                VolleyLog.v("asdfg", response.toString());
                                Log.d("asdfg", response.toString());
                                boolean isSaved = response.getInt("InsertFinInvoiceResult") == 1;
                                if (!isSaved) {
                                    fiFail++;
                                    Toast.makeText(mContext, "Η είσπραξη δεν αποθηκεύτηκε. Προσπαθήστε ξανά.", Toast.LENGTH_LONG).show();
                                    comm.respondVolleyRequestFinished(2, new JSONObject());
                                } else {
                                    fiSuccess++;
                                    Toast.makeText(mContext, "Η είσπραξη αποθηκεύτηκε επιτυχώς.", Toast.LENGTH_LONG).show();

                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            invoice.setFinal(true);
                                        }
                                    });

                                    comm.respondVolleyRequestFinished(2, new JSONObject());
                                }
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    comm.respondVolleyRequestFinished(2, new JSONObject());
                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("Error: ", error.getMessage());
                    Log.e("asdfg", error.getMessage(), error);
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Set All New Customers
    private void setCustomers() {
        try {
//            final int j = 0;
//            int k = 0;
            final RealmResults<Customer> customers = realm.where(Customer.class).equalTo("isNew", true).findAll();
//            final int count = customers.size();
            for (final Customer cus : customers) {
                final Address adr = realm.where(Address.class).equalTo("myCustomer.ID", cus.getID()).findFirst();
                HashMap<String, String> params = new HashMap<>();
                params.put("ID", "");
                params.put("Code", "");
				params.put("Name", cus.getName());
                params.put("TIN", cus.getTIN());
                params.put("Address", adr.getStreet());
                params.put("City", adr.getCity());
                params.put("PostalCode", adr.getPostalCode());
                params.put("Phone1", String.valueOf(adr.getPhone1()));
                params.put("Phone2", String.valueOf(adr.getPhone2()));
                params.put("Mobile", String.valueOf(adr.getMobile()));
                params.put("Email", String.valueOf(adr.getEmail()));
                params.put("TaxLevel", String.valueOf(cus.getTaxLevel()));

                JSONObject obj = new JSONObject(params);
                final HashMap<String, JSONObject> customer = new HashMap<>();
                customer.put("customer", obj);
                Log.d("asdfg", String.valueOf(customer));

                //String url = getString(R.string.MBUrl);
                String url = gVar.getOnlineIP();

                JsonObjectRequest req = new JsonObjectRequest(url + "CreateCustomer", new JSONObject(customer),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    VolleyLog.v("asdfg", response.toString(4));
                                    Log.d("asdfg", response.toString());
                                    JSONArray jsonArray = response.getJSONArray("CreateCustomerResult");
                                    final JSONObject jo = jsonArray.getJSONObject(0);

                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            try {
                                                cus.setID(jo.getString("ID"));
                                                adr.setID(jo.getString("AddressID"));
                                            } catch (Exception e) {
                                                Log.e("asdfg", e.getMessage(), e);
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        comm.respondVolleyRequestFinished(0, new JSONObject());
                        Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                        VolleyLog.e("Error: ", error.getMessage());
                        Log.e("asdfg", error.getMessage(), error);
                    }
                });
                req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                if (requestQueue == null) {
                    requestQueue = Volley.newRequestQueue(mContext/*.getApplicationContext()*/);
                    Log.d("asdfg", "Requesting new queue");
                }
                requestQueue.add(req);
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
        sendVolleyRequest("GetSyncUser", "");
    }
    //endregion

    //region Update Customer
    public void updateCustomer(Context context, String ID) {
        try {
            mContext = context;
            comm = (Communicator) mContext;
//            final int j = 0;
//            int k = 0;
            final RealmResults<Customer> customers = realm.where(Customer.class).equalTo("ID", ID).findAll();
//            final int count = customers.size();
            for (final Customer cus : customers) {
                final Address adr = realm.where(Address.class).equalTo("myCustomer.ID", cus.getID()).findFirst();
                HashMap<String, String> params = new HashMap<>();
                params.put("ID", cus.getID());
                params.put("Code", cus.getCode());
                params.put("Name", cus.getName());
                params.put("TIN", cus.getTIN());
                params.put("Notes", cus.getNotes());
                params.put("AddressID", adr.getID());
                params.put("Address", adr.getStreet());
                params.put("City", adr.getCity());
                params.put("PostalCode", adr.getPostalCode());
                params.put("Phone1", String.valueOf(adr.getPhone1()));
                params.put("Phone2", String.valueOf(adr.getPhone2()));
                params.put("Mobile", String.valueOf(adr.getMobile()));
                params.put("Email", String.valueOf(adr.getEmail()));
                params.put("TaxLevel", String.valueOf(cus.getTaxLevel()));

                JSONObject obj = new JSONObject(params);
                final HashMap<String, JSONObject> customer = new HashMap<>();
                customer.put("customer", obj);
                Log.d("asdfg", String.valueOf(customer));

                //String url = getString(R.string.MBUrl);
                String url = gVar.getOnlineIP();

                JsonObjectRequest req = new JsonObjectRequest(url + "UpdateCustomer", new JSONObject(customer),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    VolleyLog.v("asdfg", response.toString(4));
                                    Log.d("asdfg", response.toString());
                                    comm.respondVolleyRequestFinished(1, response);
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        comm.respondVolleyRequestFinished(1, new JSONObject());
                        Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                        VolleyLog.e("Error: ", error.getMessage());
                        Log.e("asdfg", error.getMessage(), error);
                    }
                });
                req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                if (requestQueue == null) {
                    requestQueue = Volley.newRequestQueue(mContext/*.getApplicationContext()*/);
                    Log.d("asdfg", "Requesting new queue");
                }
                requestQueue.add(req);
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Get Customer Secondary Emails
    void getCustomerEmails(Context context, final Customer customer) {
        try {
            mContext = context;
            comm = (Communicator) mContext;
            HashMap<String, String> params = new HashMap<>();
            String url = gVar.getOnlineIP() + "SenService/GetMobCustomerEmail?CusID=" + customer.getID();
            Log.d("asdfg", url);

            final JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("asdfg", String.valueOf(response));
                            try {
                                comm.respondVolleyRequestFinished(0, response);

                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//                    comm.respondVolleyRequestFinished(0, new JSONObject());
                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("Error: ", error.getMessage());
                    Log.e("asdfg", error.getMessage(), error);
                }
            });

            req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext/*.getApplicationContext()*/);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Send Online Report Email to Customer
    void sendOnlineReportEmail(Context context, final String link) {
        try {
            mContext = context;
            comm = (Communicator) mContext;
            HashMap<String, String> params = new HashMap<>();
            String url = gVar.getOnlineIP() + link;
            Log.d("asdfg", url);

            final JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("asdfg", String.valueOf(response));
                                comm.respondVolleyRequestFinished(1, response);

                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//                    comm.respondVolleyRequestFinished(1, new JSONObject());
                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("Error: ", error.getMessage());
                    Log.e("asdfg", error.getMessage(), error);
                }
            });

            req.setRetryPolicy(new DefaultRetryPolicy(60000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext/*.getApplicationContext()*/);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Send Financial Invoice Receipt Email to Customer
    void sendFInvoiceEmail(Context context, final String link) {
        try {
            mContext = context;
            comm = (Communicator) mContext;
            HashMap<String, String> params = new HashMap<>();
            String url = gVar.getOnlineIP() + link;
            Log.d("asdfg", url);

            final JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("asdfg", String.valueOf(response));
                                boolean isSaved = response.getBoolean("ReSendEmailResult");
                                if (!isSaved) {
                                    Toast.makeText(mContext, "Το e-mail δεν στάλθηκε. Προσπαθήστε ξανά.", Toast.LENGTH_LONG).show();
                                    comm.respondVolleyRequestFinished(2, new JSONObject());
                                } else {
                                    Toast.makeText(mContext, "Το e-mail στάλθηκε επιτυχώς.", Toast.LENGTH_LONG).show();

                                    comm.respondVolleyRequestFinished(2, new JSONObject());
                                }
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//                    comm.respondVolleyRequestFinished(1, new JSONObject());
                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("Error: ", error.getMessage());
                    Log.e("asdfg", error.getMessage(), error);
                }
            });

            req.setRetryPolicy(new DefaultRetryPolicy(60000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext/*.getApplicationContext()*/);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Get the Last Purchase of Selected Item from Current Customer
    void getLastPurchase(Context context, final ListViewReturnsItemsAdapter adapter, final InvoiceLineSimple line) {
        try {
            mContext = context;


            HashMap<String, String> params = new HashMap<>();
//            params.put("CusID", "17-03-11"/*gVar.getMyInvoice().getMyCustomer().getCode()*/);
//            params.put("ItemID", String.valueOf(1001)/*lines.get(position).getMyItem().getCode()*/);
//            String url = gVar.getOnlineIP() + "GetLastPurchase/?CusID=17-03-11&ItemID=1001";
            String url = gVar.getOnlineIP() + "SenService/GetSenLastPurchase?CusCode=" + gVar.getMyInvoice().getMyCustomer().getCode() + "&ItemCode=" + URLEncoder.encode(line.getMyItem().getCode(), "utf-8");
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                            try {
                                VolleyLog.v("asdfg", response.toString(4));
                                Log.d("asdfg", String.valueOf(response));
                                try {
                                    if (response.isNull("GetLastPurchasesResult")) {
                                            line.setLastDate("ΔΕΝ ΒΡΕΘΗΚΕ");
                                    } else {
                                        final JSONObject json = response.getJSONObject("GetLastPurchasesResult");
                                        line.setEY(json.getBoolean("isEY"));
                                        line.setOverdue(json.getInt("Overdue"));
                                        line.setLastDate(json.getString("LastDate"));
                                        line.setLastCompany(realm.where(Company.class).equalTo("InAppID", json.getString("LastCompany")).findFirst());
                                        line.setLastQuantity(json.getDouble("LastQuantity"));
                                        if (line.getLastQuantity() < line.getQuantity()) {
                                            line.setQuantity(line.getLastQuantity());
                                            Toast.makeText(mContext, "Η ποσότητα που είχατε βάλει ήταν μεγαλυτερη από την ποσότητα της τελευταίας αγοράς.", Toast.LENGTH_LONG).show();
                                        }
                                        line.setTypeCode(json.getString("TypeCode"));
                                        line.setDosCode(json.getString("DosCode"));
                                        line.setDocNumber(json.getString("DocNumber"));
                                        line.setWPrice(json.getDouble("WPrice"));
                                        line.setPrice(json.getDouble("Price"));
                                        line.setWrhID(json.getString("WrhID"));
                                        line.setBraID(json.getString("BraID"));
                                        line.setTRNID(json.getString("TRNID"));
										line.setDocID(json.getString("DocID"));
										line.setDocValue(Double.valueOf(json.getDouble("DocValue")));
										line.setChargePapi(Double.valueOf(json.getDouble("ChargePapi")));
										line.setExtraCharge(json.getBoolean("IsExtraCharge"));
										line.setExtraChargeValue(Double.valueOf(json.getDouble("ExtraChargeValue")));
										line.setExtraChargeLimit(Double.valueOf(json.getDouble("ExtraChargeLimit")));
										line.setDiscountReturnPercent(Double.valueOf(json.getDouble("DiscountReturnPercent")));
										String allowed=json.getString("ReturnAllowed");
                                        String[] separated = allowed.split("#");
										if(separated[0].equals("0")){
                                            line.setAllowed(false);
                                        }
										else{
										    line.setAllowed(true);
                                        }
                                        line.setMessageAllowed(separated[1]);
                                    }
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }

                                Log.d("asdfg", String.valueOf(line.getLastCompany()));
//                                sortAdapter();
                                adapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("asdfg", error.getMessage());
                    Log.e("asdfg", error.getMessage(), error);
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Get the Recent Purchases of Selected Item from Current Customer
    void getRecentPurchases(Context context, final InvoiceLineSimple line) {
        try {
            mContext = context;
            comm = (Communicator) context;
            HashMap<String, String> params = new HashMap<>();
            String url = gVar.getOnlineIP() + "SenService/GetSenRecentPurchases?CusCode=" + gVar.getMyInvoice().getMyCustomer().getCode() + "&ItemCode=" + URLEncoder.encode(line.getMyItem().getCode(), "utf-8");;
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                            try {
                                VolleyLog.v("asdfg", response.toString(4));
                                Log.d("asdfg", String.valueOf(response));
                                try {
                                    comm.respondVolleyRequestFinished(0, response);
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("asdfg", error.getMessage());
                    Log.e("asdfg", error.getMessage(), error);
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Get Balance of Current Customer
    void getCustomerBalance(Context context, Customer customer) {
        try {
            mContext = context;
            comm = (Communicator) context;
            HashMap<String, String> params = new HashMap<>();
            String url = gVar.getOnlineIP() + "SenService/GetSenBalance?CusID=" + customer.getID();
            Log.d("asdfg", url);
            final JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                VolleyLog.d("asdfg", response.toString(4));
                                Log.d("asdfg", String.valueOf(response));
                                comm.respondVolleyRequestFinished(3, response);
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    comm.respondVolleyRequestFinished(3, new JSONObject());
                    VolleyLog.e("Error: ", error.getMessage());
                }
            });

            req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion


  public void setCustomerService(Context context, Customer customer, int ServiceLevel) {
        try {
            this.mContext = context;
            this.comm = (Communicator) context;
            HashMap<String, String> params = new HashMap<>();
            String url = this.gVar.getOnlineIP() + "SenService/SetSenCustomerServiceLevel?CustomerID=" + customer.getID() + "&ServiceLevel=" + ServiceLevel;
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(0, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    try {
                        VolleyLog.d("asdfg", response.toString(4));
                        Log.d("asdfg", String.valueOf(response));
                        VolleyRequests.this.comm.respondVolleyRequestFinished(4, response);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    VolleyRequests.this.comm.respondVolleyRequestFinished(4, new JSONObject());
                    VolleyLog.e("Error: ", error.getMessage());
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(15000, 1, 1.0f));
            if (this.requestQueue == null) {
                this.requestQueue = Volley.newRequestQueue(this.mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            this.requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

	public void getReturnBalance(Context context, final Customer customer) {
        try {
            this.mContext = context;
            this.comm = (Communicator) context;
            HashMap<String, String> params = new HashMap<>();
            String url = this.gVar.getOnlineIP() + "SenService/GetSENReturnBalance?CustomerCode=" + customer.getCode();
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(0, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    try {
                        VolleyLog.d("asdfg", response.toString(4));
                        Log.d("asdfg", String.valueOf(response));
                        if (!response.isNull("GetReturnBalanceResult")) {
                            final JSONObject json = response.getJSONObject("GetReturnBalanceResult");
                            VolleyRequests.this.realm.executeTransaction(new Realm.Transaction() {
                                public void execute(Realm realm) {
                                    try {
                                        realm.copyToRealmOrUpdate(new ReturnBalance(json.getString("CusCode"), customer, Double.valueOf(json.getDouble("ReturnsValue")), Double.valueOf(json.getDouble("ReturnsPercent"))));
                                    } catch (JSONException error) {
                                        VolleyRequests.this.comm.respondVolleyRequestFinished(3, new JSONObject());
                                        VolleyLog.e("Error: ", error.getMessage());
                                    }
                                }
                            });
                        }
                        VolleyRequests.this.comm.respondVolleyRequestFinished(3, response);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    VolleyRequests.this.comm.respondVolleyRequestFinished(3, new JSONObject());
                    VolleyLog.e("Error: ", error.getMessage());
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(15000, 1, 1.0f));
            if (this.requestQueue == null) {
                this.requestQueue = Volley.newRequestQueue(this.mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            this.requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    //region Set Customer Message
    void setCustomerMessage(Context context, Customer customer) {
        try {
            mContext = context;
            comm = (Communicator) context;
            JSONObject params = new JSONObject();
            params.put("ID", customer.getID());
            params.put("Code", customer.getCode());
            params.put("Message", customer.getMessage());

            HashMap<String, JSONObject> invoiceFinal = new HashMap<>();
            invoiceFinal.put("customer", params);

            String url = gVar.getOnlineIP() + "SenService/PostSenCustomerMessage";
            Log.d("asdfg", url+ String.valueOf(invoiceFinal));
            final JsonObjectRequest req = new JsonObjectRequest( url, new JSONObject(invoiceFinal),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                VolleyLog.d("asdfg", response.toString(4));
                                Log.d("asdfg", String.valueOf(response));
                                comm.respondVolleyRequestFinished(0, response);
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    comm.respondVolleyRequestFinished(0, new JSONObject());
                    VolleyLog.e("Error: ", error.getMessage());
                }
            });

            req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region get B2B Credentials
    void getCustomerB2BCredentials(Context context, Customer customer) {
        try {
            mContext = context;
            comm = (Communicator) context;
            HashMap<String,String> params = new HashMap<>();
            params.put("Code", customer.getCode());

            String url = gVar.getOnlineIP() + "SenService/PostSenCustomerB2B";
            Log.d("asdfg", url+ String.valueOf(new JSONObject(params)));
            final JsonObjectRequest req = new JsonObjectRequest( url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                VolleyLog.d("asdfg", response.toString(4));
                                Log.d("asdfg", String.valueOf(response));
                                comm.respondVolleyRequestFinished(1, response);
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    comm.respondVolleyRequestFinished(1, new JSONObject());
                    VolleyLog.e("Error: ", error.getMessage());
                }
            });

            req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

	public void SentEmailWithImage(Context context, Bitmap Image, ArrayList<CustomerDetailRow> Details, String Title, String Email1, String Email2) {
        String encodedImage = null;
        Context context2 = context;
        ArrayList<CustomerDetailRow> arrayList = Details;
        try {
            this.mContext = context2;
            this.comm = (Communicator) context2;
            HashMap<String, Object> params = new HashMap<>();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                Image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Image.recycle();
                encodedImage = Base64.encodeToString(byteArray, 0);
                new String(byteArray, "UTF-8");
                JSONArray jaInvoiceDetails = new JSONArray();
                if (arrayList != null) {
                    int ilLength = Details.size();
                    for (int i = 0; i < ilLength; i++) {
                        JSONArray jaInvoiceDetailsRow = new JSONArray();
                        try {
                            Iterator<String> it = arrayList.get(i).getData().iterator();
                            while (it.hasNext()) {
                                jaInvoiceDetailsRow.put(it.next());
                            }
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                        jaInvoiceDetails.put(jaInvoiceDetailsRow);
                    }
                }
                params.put("Details", jaInvoiceDetails);
                try {
                    params.put("Title", Title);
                } catch (Exception e) {

                    String str = Email1;
                    String str2 = Email2;
                    Log.e("asdfg", e.getMessage(), e);
                }
            } catch (Exception e) {

                String str3 = Title;
                String str4 = Email1;
                String str22 = Email2;
                Log.e("asdfg", e.getMessage(), e);
            }
            try {
                params.put("Email1", Email1);
                try {
                    params.put("Email2", Email2);
                    params.put("Image", encodedImage);
                    String url = this.gVar.getOnlineIP() + "SenService/PostSentEmailWithImage";
                    Log.d("asdfg", url + new JSONObject(params));
                    HashMap<String, Object> hashMap = params;
                    JsonObjectRequest req = new JsonObjectRequest(url, new JSONObject(params), new Response.Listener<JSONObject>() {
                        public void onResponse(JSONObject response) {
                            try {
                                VolleyLog.d("asdfg", response.toString(4));
                                Log.d("asdfg", String.valueOf(response));
                                VolleyRequests.this.comm.respondVolleyRequestFinished(2, response);
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            VolleyRequests.this.comm.respondVolleyRequestFinished(2, new JSONObject());
                            VolleyLog.e("Error: ", error.getMessage());
                        }
                    });
                    String str5 = url;
                    req.setRetryPolicy(new DefaultRetryPolicy(15000, 1, 1.0f));
                    if (this.requestQueue == null) {
                        this.requestQueue = Volley.newRequestQueue(this.mContext);
                        Log.d("asdfg", "Requesting new queue");
                    }
                    this.requestQueue.add(req);
                } catch (Exception e) {

                    Log.e("asdfg", e.getMessage(), e);
                }
            } catch (Exception e) {

                String str222 = Email2;
                Log.e("asdfg", e.getMessage(), e);
            }
        } catch (Exception e) {
            Bitmap bitmap = Image;
            String str32 = Title;
            String str42 = Email1;
            String str2222 = Email2;
            Log.e("asdfg", e.getMessage(), e);
        }
    }


    public void UpdateCustomerAddressCoordinates(Context context, String Latitude,String Longitude,String AdrID) {
        try {
            this.mContext = context;
            this.comm = (Communicator) context;
            HashMap<String, String> params = new HashMap<>();
            String url = this.gVar.getOnlineIP() + "SenService/UpdateCustomerAddressCoordinates?Latitude=" + Latitude+"&Longitude="+Longitude+"&AdrID="+AdrID;
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(0, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    try {
                        VolleyLog.d("asdfg", response.toString(4));
                        Log.d("asdfg", String.valueOf(response));
                        if (!response.isNull("UpdateCustomerAddressCoordinatesResult")) {
                            //final JSONObject json = response.getJSONObject("UpdateCustomerAddressCoordinatesResult");
                            VolleyRequests.this.comm.respondVolleyRequestFinished(5,response);
                        }
                        VolleyRequests.this.comm.respondVolleyRequestFinished(5, response);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    VolleyRequests.this.comm.respondVolleyRequestFinished(5, new JSONObject());
                    VolleyLog.e("Error: ", error.getMessage());
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(15000, 1, 1.0f));
            if (this.requestQueue == null) {
                this.requestQueue = Volley.newRequestQueue(this.mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            this.requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void syncEvents(final Context context, final String UserName, final String n) {
        try {

            comm = (Communicator) context;
            mContext = context;
            HashMap<String, String> params = new HashMap<>();
            String url = gVar.getOnlineIP() + "SenService/GetMobEvents?UserName=" + UserName  + "&Row=" +n;
            Log.d("asdfg", params.toString());
            Log.d("asdfg", url);


            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                final JSONArray jsonArray = response.getJSONArray("GetEventsFromMobileResult");
                                if (jsonArray.length() > 0) {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            try {
                                                realm.createOrUpdateAllFromJson(UserEvents.class, jsonArray);
                                            } catch (Exception e) {
                                                Log.e("asdfg", e.getMessage(), e);
                                            }
                                            Log.d("asdfg", "number of Events: " + String.valueOf(realm.where(UserEvents.class).count()));
                                        }
                                    });
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("Message", "Περάστηκαν τα πρώτα " + String.valueOf(Integer.parseInt(n) + jsonSize) + " ραντεβού.");
                                    comm.respondVolleyRequestFinished(0, jsonObject);
                                    syncEvents(context,UserName, String.valueOf(Integer.parseInt(n) + jsonSize));
                                } else {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("Message", "Ο συγχρονισμός των ραντεβού ολοκληρώθηκε.\n O συγχρονισμός ολοκληρώθηκε, μπορείτε να κλείσετε το παράθυρο.");
                                    comm.respondVolleyRequestFinished(1, jsonObject);
                                }
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    JSONObject object = new JSONObject();
                    try {
                        object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    comm.respondVolleyRequestFinished(1, object);
                    VolleyLog.e("asdfg", error.getMessage());
                    Log.e("asdfg", error.getMessage(), error);
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void SendCoordinates(Context context){
        try{
            mContext = context;
            comm = (Communicator) context;
            RealmResults<GpsCoordinates> gpsCoordinates = realm.where(GpsCoordinates.class).findAll();
            JSONArray lines = new JSONArray();
            int ilLength = gpsCoordinates.size();
            if (ilLength > 0) {
                for (int j = 0; j < ilLength; j++) {
                    JSONObject il = new JSONObject();
                    try {
                        il.put("ID", gpsCoordinates.get(j).getID());
                        il.put("UserName", gpsCoordinates.get(j).getUserName());
                        il.put("Date", gpsCoordinates.get(j).getDate());
                        il.put("Time", gpsCoordinates.get(j).getTime());
                        il.put("Latitude", gpsCoordinates.get(j).getLatitude());
                        il.put("Longitude", gpsCoordinates.get(j).getLongitude());

                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    lines.put(il);
                }


                JSONObject joIn = new JSONObject();

                try {
                    joIn.put("Lines", lines);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                HashMap<String, JSONObject> params = new HashMap<>();
                params.put("gpsCoordinates", joIn);
                String url =  gVar.getOnlineIP() + "SenService/PostGpsCoordinates";

                Log.d("asdfg", url + new JSONObject(params));
                JsonObjectRequest req = new JsonObjectRequest(url, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(final JSONObject response) {
                                try {
                                    VolleyLog.v("asdfg", response.toString());
                                    Log.d("asdfg", response.toString());
                                    /*if (mContext instanceof CheckOut) {
                                        comm.respondVolleyRequestFinished(2, response);
                                    } else {*/
                                        boolean isSaved = response.getInt("InsertGpsCoordinatesResult") == 1;
                                        if (!isSaved) {
                                            JSONObject jsonObject = new JSONObject();
                                            jsonObject.put("Message", "Δεν στάλθηκαν οι συντεταγμένες");
                                            comm.respondVolleyRequestFinished(0, jsonObject);
                                        } else {
                                            realm.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    realm.where(GpsCoordinates.class).findAll().deleteAllFromRealm();
                                                }
                                            });
                                            JSONObject jsonObject = new JSONObject();
                                            jsonObject.put("Message", "Στάλθηκαν οι συντεταγμένες επιτυχώς!");
                                            comm.respondVolleyRequestFinished(1, jsonObject);
                                        }
                                    //}
                                } catch (Exception e) {
                                    try {
                                        JSONObject object = new JSONObject();
                                        object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                                        comm.respondVolleyRequestFinished(0, object);
                                        Log.e("asdfg", e.getMessage(), e);
                                    } catch (Exception ex) {
                                        Log.e("asdfg", ex.getMessage(), ex);
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        try {
                            VolleyLog.e("Error: ", error.getMessage());
                            Log.e("asdfg", error.getMessage(), error);
                            JSONObject object = new JSONObject();
                            object.put("Message","Ο Διακομιστής δεν ανταποκρίθηκε.");
                            comm.respondVolleyRequestFinished(0, object);
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }

                    }
                });
                req.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                if (requestQueue == null) {
                    requestQueue = Volley.newRequestQueue(mContext);
                    Log.d("asdfg", "Requesting new queue");
                }
                requestQueue.add(req);
            }else {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Message", "Δεν υπάρχουν συντεταγμένες προς αποθήκευση!");
                    comm.respondVolleyRequestFinished(2, jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void GetDepositAccounts(Context context) {
        try {
            this.mContext = context;
            this.comm = (Communicator) this.mContext;
            HashMap<String, String> params = new HashMap<>();
            String url = this.gVar.getOnlineIP() + "SenService/GetDepositAccounts";
            Log.d("asdfg", url + String.valueOf(params));
            JsonObjectRequest req = new JsonObjectRequest(0, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    try {
                        VolleyLog.v("asdfg", response.toString());
                        Log.d("asdfg", response.toString());
                        comm.respondVolleyRequestFinished(4, response);
                    } catch (Exception e) {
                        Toast.makeText( mContext,  "Ο Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_SHORT).show();
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText( mContext,  "Ο Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_SHORT).show();
                    VolleyLog.e("Error: ", error.getMessage());
                    Log.e("asdfg", error.getMessage(), error);
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(15000, 0, 2.0f));
            if (this.requestQueue == null) {
                this.requestQueue = Volley.newRequestQueue(this.mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            this.requestQueue.add(req);
            this.requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    //region Get Customers of Current User or manufacturer
//    void getCustomerBalance(Context context, Customer customer) {
//        try {
//            mContext = context;
//            comm = (Communicator) context;
//            HashMap<String, String> params = new HashMap<>();
//            String url = gVar.getOnlineIP() + "SenService/GetSenBalance?CusID=" + customer.getID();
//            Log.d("asdfg", url);
//            final JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
//                    new Response.Listener<JSONObject>() {
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            try {
//                                VolleyLog.d("asdfg", response.toString(4));
//                                Log.d("asdfg", String.valueOf(response));
//                                comm.respondVolleyRequestFinished(3, response);
//                            } catch (Exception e) {
//                                Log.e("asdfg", e.getMessage(), e);
//                            }
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    comm.respondVolleyRequestFinished(3, new JSONObject());
//                    VolleyLog.e("Error: ", error.getMessage());
//                }
//            });
//
//            req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//            if (requestQueue == null) {
//                requestQueue = Volley.newRequestQueue(mContext);
//                Log.d("asdfg", "Requesting new queue");
//            }
//            requestQueue.add(req);
//        } catch (Exception e) {
//            Log.e("asdfg", e.getMessage(), e);
//        }
//    }
    //endregion

    //region Get Item Images
    /*private void getImages() {
        try {
            items = new ArrayList<>();
            items.addAll(realm.where(Item.class).findAll());
//            int c=items.size();
//            for (int i=0;i<c;i++) {
//                final String cusCode= items.get(i).getCode();
//                Log.d("asdfg", cusCode);
//                imageDownload(i);
//            }
            imageDownload(0);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void imageDownload(final int i) {
        try {
            final int c = items.size() - 1;
//            Log.d("asdfg", "c = "+String.valueOf(c));
            final String cusCode = items.get(i).getCode();
            Log.d("asdfg", cusCode);
            BasicImageDownloader downloader = new BasicImageDownloader(new BasicImageDownloader.OnImageLoaderListener() {
                @Override
                public void onError(BasicImageDownloader.ImageError error) {
                    Log.e("asdfg", error.getMessage(), error);
                    if (i < c) {
                        Log.d("asdfg", String.valueOf(i));
                        imageDownload(i + 1);
                    } else {
                        Toast.makeText(mContext, "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                        getCustomers("0");
                    }
                }

                @Override
                public void onProgressChange(int percent) {

                }

                @Override
                public void onComplete(Bitmap result) {
                    *//* save the image - I'm gonna use JPEG *//*
                    final Bitmap.CompressFormat mFormat = Bitmap.CompressFormat.JPEG;
                    *//* don't forget to include the extension into the file cusName *//*
                    final File myImageFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures/" + cusCode + ".jpg");
//                    if (!myImageFile.exists()){
                    BasicImageDownloader.writeToDisk(myImageFile, result, new BasicImageDownloader.OnBitmapSaveListener() {
                        @Override
                        public void onBitmapSaved() {
                            //Toast.makeText(Sync.this, "Image saved as: " + myImageFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                            Log.d("asdfg", "Image saved as: " + myImageFile.getAbsolutePath());
                            if (i < c) {
                                Log.d("asdfg", String.valueOf(i));
                                imageDownload(i + 1);
                            } else {
                                Toast.makeText(mContext, "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                getCustomers("0");
                            }
                        }

                        @Override
                        public void onBitmapSaveError(BasicImageDownloader.ImageError error) {
                            Log.e("asdfg", error.getMessage(), error);
                            if (i < c) {
                                Log.d("asdfg", String.valueOf(i));
                                imageDownload(i + 1);
                            } else {
                                Toast.makeText(mContext, "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                getCustomers("0");
                            }
                        }
                    }, mFormat, true);
                }
            });
            downloader.download(gVar.getOnlineIP() + "GetImage/" + items.get(i).getCode(), true);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }*/
    //endregion
}