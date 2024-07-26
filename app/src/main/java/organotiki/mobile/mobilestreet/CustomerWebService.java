package organotiki.mobile.mobilestreet;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Bookmark;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;

/**
 * Created by Thanasis on 11/1/2017.
 */

public class CustomerWebService extends AppCompatActivity implements Communicator{

    Realm realm;
    GlobalVar gVar;
    String CustomerCode;
    boolean isClose;
    DrawerLayout drawer;
    WebView mWebView;
    String fileLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_customer_webservice);

            final Toolbar toolbar = (Toolbar) findViewById(R.id.navigateBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);



            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            CustomerCode=getIntent().getStringExtra("customerCode");
            isClose=getIntent().getBooleanExtra("isClose",false);

            mWebView = (WebView) findViewById(R.id.webView_customer_webservice);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.getSettings().setDisplayZoomControls(false);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.canGoBack();
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.d("asdfg","when you click on any interlink on webview that time you got url :- " + url);
                    return super.shouldOverrideUrlLoading(view, url);
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed();
                }

                @Override
                public void onReceivedError(WebView view, int errorCod, String description, String failingUrl) {
                    try {
                        Toast.makeText(CustomerWebService.this, "Your Internet Connection May not be active Or " + description, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);

                    Log.d("asdfg", "your current url when webpage loading..: " + url);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    Log.d("asdfg", "your current url when webpage loading... finish: " + url);
                    super.onPageFinished(view, url);
                }
            });

            fileLink = gVar.getOnlineIP() +"SenService/GetSenBookmarkFile?ID=";
            String url="";
            if(isClose){
                url = (isLocalIPReachable("http://10.0.0.104/") ? "http://10.0.0.104/" : "http://crm.karabassis.com/") + "customs/WebService.php?user="+gVar.getMyUser().getUsername()+"&customer="+CustomerCode+"&distance=1";
            }else{
                url = (isLocalIPReachable("http://10.0.0.104/") ? "http://10.0.0.104/" : "http://crm.karabassis.com/") + "customs/WebService.php?user="+gVar.getMyUser().getUsername()+"&customer="+CustomerCode+"&distance=0";
            }
            mWebView.loadUrl(url);

        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //this method is used for adding menu items to the Activity
// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigate_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //this method is used for handling menu items' events
// Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.goBack:
                if(mWebView.canGoBack()) {
                    mWebView.goBack();
                }
                return true;

            case R.id.goForward:
                if(mWebView.canGoForward()) {
                    mWebView.goForward();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void CustomerWebServiceRespond(JSONObject response) {

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
        switch (position) {
            case -1:
                try {
                    String message = jsonObject.getString("Message");
                    Toast.makeText(CustomerWebService.this, message, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 0:
                CustomerWebServiceRespond(jsonObject);
                break;
            case 1:
                break;
            case 2:
                //BookmarkFileRespond(jsonObject);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            try {

                onBackPressed();

            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private boolean isLocalIPReachable(String localip) {
        boolean exists = false;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);
            final String[] sParts = localip.split(":");
            final int l = sParts.length;
            if (l > 1) {
                int port = Integer.parseInt(sParts[l - 1].replace("/", ""));
                //String ip = gVar.getLocalIP().replace(":"+ String.valueOf(port)+"/", "");
                String ip = sParts[l - 2].replace("/", "");
                Log.d("asdfg", "IP: " + ip);
                Log.d("asdfg", "Port: " + port);
                SocketAddress sockaddr = new InetSocketAddress(ip, port);
                // Create an unbound socket
                Socket sock = new Socket();

                // This method will block no more than timeoutMs.
                // If the timeout occurs, SocketTimeoutException is thrown.
                int timeoutMs = 500;   // 2 seconds
                sock.connect(sockaddr, timeoutMs);
                exists = true;
            }
        } catch (Exception e) {
            //Log.e("asdfg", e.getMessage(), e);
        }
        Log.d("asdfg", "LocalIP found:" + exists);
        return exists;
    }

}
