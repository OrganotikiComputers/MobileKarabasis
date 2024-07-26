package organotiki.mobile.mobilestreet;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
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

public class Bookmarks extends AppCompatActivity implements Communicator{

    Realm realm;
    GlobalVar gVar;
    VolleyRequests request;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    RealmResults<Bookmark> categories;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataFirstChild, listDataSecondChild;
    DrawerLayout drawer;
    WebView mWebView;
    String fileLink;
    String lastBookmarkID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_bookmarks);

            final Toolbar toolbar = (Toolbar) findViewById(R.id.navigateBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            drawer = (DrawerLayout) findViewById(R.id.drawer_inventory_header);
            drawer.openDrawer(GravityCompat.START);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            request = new VolleyRequests();

            mWebView = (WebView) findViewById(R.id.webView_bookmarks);
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
                        Toast.makeText(Bookmarks.this, "Your Internet Connection May not be active Or " + description, Toast.LENGTH_LONG).show();
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
                    Log.d("asdfg", "your current url when webpage loading.. finish: " + url);
                    super.onPageFinished(view, url);
                }
            });

            fileLink = gVar.getOnlineIP() +"SenService/GetSenBookmarkFile?ID=";

            request.getBookmarks(Bookmarks.this);
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

    private void BookmarksRespond(JSONObject response) {
        try {
            expListView = (ExpandableListView) findViewById(R.id.categories);

            // preparing list data
            //prepareListData(response);

            categories = realm.where(Bookmark.class).equalTo("Category", "-1").findAll();
            try {
                listDataHeader = new ArrayList<>();
                listDataFirstChild = new HashMap<>();
                listDataSecondChild = new HashMap<>();

                // Adding child data
                for (int i = 0; i < categories.size(); i++) {
                    listDataHeader.add(categories.get(i).getName());
                }

                // Adding child data
                int listDataLen = listDataHeader.size();
                for (int i = 0; i < listDataLen; i++) {
                    RealmResults<Bookmark> childResults = realm.where(Bookmark.class).equalTo("Category", categories.get(i).getID()).findAll();
                    ArrayList<String> list = new ArrayList<>(childResults.size());
                    Log.d("asdfg", String.valueOf(childResults.size()));
                    for (int j = 0; j < childResults.size(); j++) {
                        Log.d("asdfg", "-"+childResults.get(j).getName());
                        list.add(childResults.get(j).getName());
                        RealmResults<Bookmark> childResults2 = realm.where(Bookmark.class).equalTo("Category", childResults.get(j).getID()).findAll();
                        ArrayList<String> list2 = new ArrayList<>(childResults.size());
                        for (int k = 0; k < childResults2.size(); k++) {
                            Log.d("asdfg", "--"+childResults2.get(k).getName());
                            list2.add(childResults2.get(k).getName());
                        }
                        listDataSecondChild.put(childResults.get(j).getName(), list2);
                    }

                    listDataFirstChild.put(listDataHeader.get(i), list);
                }

            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }


            listAdapter = new ExpandableListAdapter(Bookmarks.this, listDataHeader, listDataFirstChild, listDataSecondChild);

            // setting list adapter
            expListView.setAdapter(listAdapter);

            expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {
                    Toast.makeText(
                            getApplicationContext(),
                            listDataHeader.get(groupPosition)
                                    + " : "
                                    + listDataFirstChild.get(
                                    listDataHeader.get(groupPosition)).get(
                                    childPosition), Toast.LENGTH_SHORT)
                            .show();
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drower_categories);
                        mDrawerLayout.closeDrawers();
//                                            Log.d("asdfg", "hi");
                    }
                    //saveItemList();
                    //request.getItemsPerCategory(Items.this, listDataFirstChild.get(listDataHeader.get(groupPosition)).get(childPosition));
                    //mWebView.loadUrl(fileLink+listDataFirstChild.get(listDataHeader.get(groupPosition)).get(childPosition));
                    return false;
                }
            });

            expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

                @Override
                public void onGroupExpand(int groupPosition) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drower_categories);
                        mDrawerLayout.closeDrawers();
//                                            Log.d("asdfg", "hi");
                    }
                    //saveItemList();
                    //request.getItemsPerCategory(Items.this, listDataHeader.get(groupPosition));
                    //mWebView.loadUrl(fileLink+listDataHeader.get(groupPosition));
                    Toast.makeText(
                            getApplicationContext(),
                            listDataHeader.get(groupPosition)
                            , Toast.LENGTH_SHORT).show();
                }
            });

            expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

                @Override
                public void onGroupCollapse(int groupPosition) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drower_categories);
                        mDrawerLayout.closeDrawers();
                    }
                    //saveItemList();
                    //request.getItemsPerCategory(Items.this, listDataHeader.get(groupPosition));
                    //mWebView.loadUrl(fileLink+listDataHeader.get(groupPosition));
                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void BookmarkContentRespond(JSONObject response){
        try {
            String url = response.getString("GetBookmarkResult");
            Log.d("asdfg", url);
            Log.d("asdfg", url.substring(0,4));
            if (url.substring(0,4).equals("file")){
                Log.d("asdfg", fileLink+lastBookmarkID);
                mWebView.loadUrl(fileLink+lastBookmarkID);
                //request.getBookmarkFile(Bookmarks.this, lastBookmarkID);
            }else {
                Log.d("asdfg", url);
                //mWebView.loadUrl(fileLink+lastBookmarkID);
                mWebView.loadUrl(url);
            }
            drawer.closeDrawer(GravityCompat.START);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void childClicked(String parentText, String text) {
        //saveItemList();
        //request.getItemsPerCategory(Items.this, text);
        Bookmark parent = realm.where(Bookmark.class).equalTo("Category", "-1").equalTo("Name", parentText).findFirst();
        Bookmark bm = realm.where(Bookmark.class).equalTo("Category", parent.getID()).equalTo("Name", text).findFirst();
        //mWebView.loadUrl(fileLink+bm.getID());
        request.getBookmark(Bookmarks.this, bm.getID());
        lastBookmarkID = bm.getID();
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
                    Toast.makeText(Bookmarks.this, message, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 0:
                BookmarksRespond(jsonObject);
                break;
            case 1:
                BookmarkContentRespond(jsonObject);
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
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    onBackPressed();
                }
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
