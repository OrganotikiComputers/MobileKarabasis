package organotiki.mobile.mobilestreet;

import android.net.http.SslError;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import io.realm.Realm;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

/**
 * Created by Thanasis on 2/2/2017.
 */

public class B2B extends AppCompatActivity {

    Realm realm;
    GlobalVar gVar;
    WebView mWebView;
    String username, password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activy_b2b);

            final Toolbar toolbar = (Toolbar) findViewById(R.id.b2b_Bar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            username = getIntent().getExtras().getString("Username");
            password = getIntent().getExtras().getString("Password");
            Log.d("asdfg", "username :"+username+", password:"+password);

            mWebView = (WebView) findViewById(R.id.webView_b2b);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.getSettings().setDisplayZoomControls(false);
            mWebView.getSettings().setDomStorageEnabled(true);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.canGoBack();
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return false;
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed();
                }

                @Override
                public void onReceivedError(WebView view, int errorCod, String description, String failingUrl) {
                    try {
                        Toast.makeText(B2B.this, "Your Internet Connection May not be active Or " + description, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {

                    try {
                        super.onPageFinished(view, url);
                        mWebView.loadUrl("javascript: " +
                                "document.getElementById('username').value = '"+username +"';" +
                                "document.getElementById('password').value = '"+password+"';" +
                                "var frms = document.getElementsByID('login').click();");// +
                        //"frms[0].submit(); };");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
            mWebView.loadUrl("http://web2.carparts-cat.com/default.aspx?10=756086B6C5EE4CC6A2A4B2BC5D4486DB388020&14=20&12=1003&204=false");

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
}
