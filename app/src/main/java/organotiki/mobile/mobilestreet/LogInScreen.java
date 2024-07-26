package organotiki.mobile.mobilestreet;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ProgressBar;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Bank;
import organotiki.mobile.mobilestreet.objects.Company;
import organotiki.mobile.mobilestreet.objects.CompanySite;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;
import organotiki.mobile.mobilestreet.objects.User;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class LogInScreen extends AppCompatActivity implements View.OnClickListener, Communicator {

    EditText username, password;
    AppCompatSpinner company, companySite;
    ArrayList<String> companyList, companySiteList;
    Button login, exit;
    Intent intent = null;
    Realm realm;
    GlobalVar gVar;
    VolleyRequests request;
    CompanySite lastSite;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_screen);
        // Get a Realm instance for this thread
        realm = Realm.getDefaultInstance();
        gVar = realm.where(GlobalVar.class).findFirst();

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.getIndeterminateDrawable().setColorFilter(-1, PorterDuff.Mode.MULTIPLY);

        username = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);

        if (realm.where(Customer.class).count()==0){
            Toast.makeText(LogInScreen.this, "Δεν έχετε συγχονίσει τα είδη και τους πελάτες.", Toast.LENGTH_LONG).show();
        }

        RealmResults<Company> companies = realm.where(Company.class).findAll();
        lastSite = gVar.getMyCompanySite();
        Integer bankCount = companies.size();
        companyList = new ArrayList<>();
//            banklist.add("ΠΕΙΡΑΙΩΣ");
//            banklist.add("ΕΘΝΙΚΗ");
        for (int i = 0; i < bankCount; i++) {
            companyList.add(companies.get(i).getDescription());
        }
        company = (AppCompatSpinner) findViewById(R.id.spinner_company);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(LogInScreen.this,
                R.layout.spinner_collections_item, companyList);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the mAdapter to the spinner
        company.setAdapter(adapter);
        if (lastSite != null) {
            for (int i = 0; i < companyList.size(); i++) {
                if (companyList.get(i).equals(lastSite.getMyCompany().getDescription())) {
                    company.setSelection(i);
                    break;
                }
            }
        }
//        company.setSelection(0);
        company.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> adapterView, View view, final int i, long l) {
                try {
                    SelectCompanySite();
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        companySite = (AppCompatSpinner) findViewById(R.id.spinner_companySite);

        SelectCompanySite();

        login = (Button) findViewById(R.id.logIn);
        login.setTransformationMethod(null);
        login.setOnClickListener(LogInScreen.this);
        exit = (Button) findViewById(R.id.exit);
        exit.setTransformationMethod(null);
        exit.setOnClickListener(LogInScreen.this);

        request = new VolleyRequests();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logIn:
                LogIn();
                break;
            case R.id.exit:
                //this.finish();
                /*Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);*/
                this.finishAffinity();
                break;
        }
    }

    private void SelectCompanySite() {
        try {

            Log.d("asdfg", String.valueOf(company.getSelectedItem()));
            if (company.getSelectedItem() != null) {
                RealmResults<CompanySite> companySites = realm.where(CompanySite.class).equalTo("myCompany.Description", String.valueOf(company.getSelectedItem())).findAll();
                Integer companySiteCount = companySites.size();
                Log.d("asdfg", String.valueOf(companySites));
                companySiteList = new ArrayList<>();
//            banklist.add("ΠΕΙΡΑΙΩΣ");
//            banklist.add("ΕΘΝΙΚΗ");
                for (int i = 0; i < companySiteCount; i++) {
                    if (companySites.get(i).isMain()) {
                        companySiteList.add(0, companySites.get(i).getDescription());
                    } else {
                        companySiteList.add(companySites.get(i).getDescription());
                    }
                }
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<String> adapterSite = new ArrayAdapter<>(LogInScreen.this,
                        R.layout.spinner_collections_item, companySiteList);
                // Specify the layout to use when the list of choices appears
                adapterSite.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the mAdapter to the spinner
                companySite.setAdapter(adapterSite);

                if (lastSite != null) {
                    for (int i = 0; i < companySiteList.size(); i++) {
                        if (companySiteList.get(i).equals(lastSite.getDescription())) {
                            companySite.setSelection(i);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private void LogIn() {
        if (companySite.getSelectedItem() != null || ((String.valueOf(username.getText()).equals("user")))) {
            if (isNetworkAvailable() && (!(String.valueOf(username.getText()).equals("user")))) {
                request.sendAuthenticationRequest(LogInScreen.this);
				progressBar.setVisibility(View.VISIBLE);
            } else {
                try {
                    if (checkIfExists(String.valueOf(username.getText()))) {
                        final User user = realm.where(User.class).equalTo("Username", String.valueOf(username.getText())).findFirst();
                        if (user.getPassword().equals(password.getText().toString())) {//.equals(sha1(password.getText() + LogIn.getString("Salt")))) {//
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    gVar.setMyUser(user);
                                    gVar.setMyCompanySite(realm.where(CompanySite.class).equalTo("myCompany.Description", String.valueOf(company.getSelectedItem())).equalTo("Description", String.valueOf(companySite.getSelectedItem())).findFirst());
                                }
                            });
                            intent = new Intent(LogInScreen.this, MainScreen.class);
                            intent.putExtra("ParentActivity", "LogInScreen");
                            startActivity(intent);
                        } else {
                            Toast.makeText(LogInScreen.this, "Λανθασμένος κωδικός.\nΠαρακαλώ προσπαθήστε ξανά.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(LogInScreen.this, "Λανθασμένο όνομα χρήστη.\nΠαρακαλώ προσπαθήστε ξανά.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    Log.e("asdfg", ex.getMessage(), ex);
                }
            }
        } else {
            Toast.makeText(LogInScreen.this, "Δεν έχετε επιλέξει Κατάστημα.", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkIfExists(String username) {
//        Log.d("asdfg", username);
        RealmQuery<User> query = realm.where(User.class)
                .equalTo("Username", username);
//        Log.d("asdfg", String.valueOf(query.count()));
        return query.count() != 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
        switch (position) {
            case 0:
                try {
                    String message = jsonArray.getString("Message");
                    Toast.makeText(LogInScreen.this, message,Toast.LENGTH_LONG).show();
                    request.LogIn(LogInScreen.this, username.getText().toString(), password.getText().toString());
                }catch (Exception e){
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 1:
                try {
					progressBar.setVisibility(View.INVISIBLE);
                    String message = jsonArray.getString("Message");
                    Toast.makeText(LogInScreen.this, message,Toast.LENGTH_LONG).show();
                    request.LogIn(LogInScreen.this, username.getText().toString(), password.getText().toString());
                }catch (Exception e){
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 2:
                try {
					progressBar.setVisibility(View.INVISIBLE);
                    if (jsonArray.isNull("LogInResult")) {
                        Toast.makeText(LogInScreen.this, "Λανθασμένος συνδιασμός στοιχείων.\nΠαρακαλώ προσπαθήστε ξανά.", Toast.LENGTH_LONG).show();
                    } else {
                        final JSONObject jo = jsonArray.getJSONObject("LogInResult");

                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                try {
                                    realm.createOrUpdateObjectFromJson(User.class,jo);
                                    final User usr = realm.where(User.class).equalTo("Username", jo.getString("Username")).count() > 0 ? realm.where(User.class).equalTo("Username", jo.getString("Username")).findFirst() : new User(jo.getString("ID"), jo.getString("Username"), password.getText().toString(), jo.getString("FullName"));
                                    //realm.where(User.class).notEqualTo("ID", "user").notEqualTo("Username", usr.getUsername()).findAll().deleteAllFromRealm();
                                    usr.setPassword(password.getText().toString());
                                    //usr.setMyCustomers(jo.get);
                                    User user = realm.copyToRealmOrUpdate(usr);
                                    gVar.setMyUser(user);
                                    gVar.setMyCompanySite(realm.where(CompanySite.class).equalTo("myCompany.Description", String.valueOf(company.getSelectedItem())).equalTo("Description", String.valueOf(companySite.getSelectedItem())).findFirst());
                                    Log.d("asdfg", String.valueOf(gVar));
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                            }
                        });
                        intent = new Intent(LogInScreen.this, MainScreen.class);
                        startActivity(intent);
                    }
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
}