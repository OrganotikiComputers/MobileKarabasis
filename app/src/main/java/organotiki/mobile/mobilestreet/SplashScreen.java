package organotiki.mobile.mobilestreet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Company;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.FInvoice;
import organotiki.mobile.mobilestreet.objects.FInvoiceLine;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.Invoice;
import organotiki.mobile.mobilestreet.objects.InvoiceLine;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;
import organotiki.mobile.mobilestreet.objects.User;
import organotiki.mobile.mobilestreet.objects.UserEvents;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class SplashScreen extends AppCompatActivity implements Communicator {
    Intent intent = null;
    Realm realm;
    VolleyRequests request;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_splash_screen);

            final String str = readFromFile("/documents/Server.txt");
            final String parts[] = str.split("\\*");
            final int l = parts.length;
            Log.d("asdfg", str+" parts: "+l);
                /*for (int i = 0; i < l; i++) {
                    Log.d("asdfg", parts[i]);
                }*/



            // Create a RealmConfiguration that saves the Realm file in the app's "files" directory.
           /* RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
            Realm.setDefaultConfiguration(realmConfig);*/

            // Get a Realm instance for this thread
            realm = Realm.getDefaultInstance();

//            Log.d("asdfg", "number of customers: "+String.valueOf(realm.where(Customer.class).count()));

            final String versionNumber = "1.1.1.38";
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
//                    realm.deleteAll();
                    if (realm.where(GlobalVar.class).count()==0) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(2016, 0, 1, 0, 0, 0);
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        String date0 = df.format(cal.getTime());
                        Log.d("asdfg", "number of parts: "+l);
                        GlobalVar gVar = new GlobalVar(UUID.randomUUID().toString(), l>0?parts[0]:"" , l>1?parts[1]:"", date0, versionNumber,"0","0");
                        realm.copyToRealmOrUpdate(gVar);
                    }else{
                        GlobalVar gVar = realm.where(GlobalVar.class).findFirst();
                        gVar.setLocalIP(l>0?parts[0]:"");
                        gVar.setOnlineIP(l>1?parts[1]:"");
                        gVar.setVerNum(versionNumber);

                        gVar.refresh();
                    }
                    User user = new User("user", "user", "user", "user");
                    realm.copyToRealmOrUpdate(user);
//                    User user1 = new User("1", "1", "1", "1", "1");
//                    realm.copyToRealmOrUpdate(user1);

//                    realm.where(Invoice.class).findAll().deleteAllFromRealm();
//                    realm.where(InvoiceLine.class).findAll().deleteAllFromRealm();
//
//                    realm.where(FInvoice.class).findAll().deleteAllFromRealm();
//                    realm.where(FInvoiceLine.class).findAll().deleteAllFromRealm();

                }
            });

            request = new VolleyRequests();
            try {
                if (realm.where(Company.class).count()==0){
                    request.sendRequest(SplashScreen.this, "SenService/GetMobCompany","");
                }
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }

            Log.d("asdfg", "number of users: " + String.valueOf(realm.where(User.class).count()));
            Log.d("asdfg", "number of customers: " + String.valueOf(realm.where(Customer.class).count()));
            Log.d("asdfg", "number of addresses: " + String.valueOf(realm.where(Address.class).count()));

            Log.d("asdfg", "local IP: "+realm.where(GlobalVar.class).findFirst().getLocalIP());
            Log.d("asdfg", "online IP: "+realm.where(GlobalVar.class).findFirst().getOnlineIP());

            intent = new Intent(SplashScreen.this, LogInScreen.class);

            final ImageView iv = (ImageView) findViewById(R.id.logo);
            final Animation an = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate);

            iv.startAnimation(an);
            an.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    finish();
                    startActivity(intent);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private String readFromFile(String filepath) {

        String ret = "";

        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filepath);
            if (file.exists()) {
                FileInputStream inputStream = new FileInputStream(file);


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("asdfg", "File not found: " + e.toString());
        } catch (Exception e) {
            Log.e("asdfg", "Can not read file: " + e.toString());
        }
        return ret;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
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
}
