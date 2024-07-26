package organotiki.mobile.mobilestreet;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.util.*;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.AlternateCode;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLine;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;
import organotiki.mobile.mobilestreet.objects.Item;

/**
 * Created by Thanasis on 15/7/2016.
 */
public class ItemsReturns extends AppCompatActivity implements View.OnClickListener, Communicator {

    ListView listView;
    EditText search;
    Button searchItem;
    Realm realm;
    GlobalVar gVar;
    ArrayList<InvoiceLine> lines;
    ArrayList<InvoiceLineSimple> lineSimple;
    RecentPurchasesFragment recentPurchasesFragment;
    ListViewReturnsItemsAdapter mAdapter;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_items_returns);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            Toolbar toolbar = (Toolbar) findViewById(R.id.itemsBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            //TextView documentHeader = (TextView) findViewById(R.id.column_header7);
            //documentHeader.setVisibility(View.GONE);

            searchItem = (Button) findViewById(R.id.button_search);
            searchItem.setTransformationMethod(null);
            searchItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveItemList();
                    searchItems();
                }
            });
            search = (EditText) findViewById(R.id.search);
            search.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            listView = (ListView) findViewById(R.id.listViewItems);


            if (toolbar != null) {
                toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        try {
                            saveItemList();
                            Intent i = new Intent(ItemsReturns.this, Returns.class);
                            startActivity(i);
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                        return false;
                    }
                });
            }

            lineSimple = (ArrayList<InvoiceLineSimple>) getLastCustomNonConfigurationInstance();
            if (lineSimple != null) {
                mAdapter = new ListViewReturnsItemsAdapter(this, lineSimple);
                listView.setAdapter(mAdapter);
            }

            /*getCategories();*/

        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        try {
            getMenuInflater().inflate(R.menu.items_menu, menu);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
        return true;
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return lineSimple;
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
            case 0:
                recentPurchasesFragment.respondRecentPurchases(jsonObject);
                FragmentManager manager = getFragmentManager();
                recentPurchasesFragment.show(manager, "Recent Purchases Fragment");
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
        for (InvoiceLineSimple sLine : sLines) {
            int index = lineSimple.indexOf(sLine);
            if (index == -1) {
                if (sLine.getQuantity() > 0) {
                    lineSimple.add(sLine);
                }
            } else {
                lineSimple.set(index, sLine);
            }
        }
        mAdapter.notifyDataSetChanged();
        saveItemList();
    }

    @Override
    public void onClick(View v) {

    }

    /*private void getCategories() {
        try {
            expListView = (ExpandableListView) findViewById(R.id.categories);
            categories = realm.where(ItemCategory.class).findAll();
            try {
                listDataHeader = new ArrayList<>();
                listDataChild = new HashMap<>();

                // Adding child data
                for (int i = 0; i < categories.size(); i++) {
                    listDataHeader.add(categories.get(i).getName());
                }

                // Adding child data
                int listDataLen = listDataHeader.size();
                for (int i = 0; i < listDataLen; i++) {
                    listDataChild.put(listDataHeader.get(i), new ArrayList<String>());
                }

            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
            // preparing list data


            listAdapter = new ExpandableListAdapter(ItemsReturns.this, listDataHeader, listDataChild);

            // setting list mAdapter
            expListView.setAdapter(listAdapter);

            expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {
                    Toast.makeText(
                            getApplicationContext(),
                            listDataHeader.get(groupPosition)
                                    + " : "
                                    + listDataChild.get(
                                    listDataHeader.get(groupPosition)).get(
                                    childPosition), Toast.LENGTH_SHORT)
                            .show();
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        DrawerLayout mDrowerLayout = (DrawerLayout) findViewById(R.id.drower_categories);
                        mDrowerLayout.closeDrawers();
                        Log.d("asdfg", "hi");
                    }
                    saveItemList();
                    getCatItems(listDataChild.get(
                            listDataHeader.get(groupPosition)).get(
                            childPosition));
                    return false;
                }
            });

            expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

                @Override
                public void onGroupExpand(int groupPosition) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        DrawerLayout mDrowerLayout = (DrawerLayout) findViewById(R.id.drower_categories);
                        mDrowerLayout.closeDrawers();
                        Log.d("asdfg", "hi");
                    }
                    saveItemList();
                    getCatItems(listDataHeader.get(groupPosition));
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
                        DrawerLayout mDrowerLayout = (DrawerLayout) findViewById(R.id.drower_categories);
                        mDrowerLayout.closeDrawers();
                        Log.d("asdfg", "hi");
                    }
                    saveItemList();
                    getCatItems(listDataHeader.get(groupPosition));
                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }*/

    private void saveItemList() {
        try {
            if (lineSimple != null) {
                for (final InvoiceLineSimple line : lineSimple) {
                    if (line.getQuantity() > 0) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
//                                InvoiceLine il = new InvoiceLine(line.getID(), line.getMyInvoice(),  line.getMyItem(), line.getMyInvoiceType(), line.getPrice(), line.getQuantity(),  line.getNotes(), line.getLastUpdate(),line.getLastCompany(), line.getWrhID(), line.getBraID(),line.getTypeCode(), line.getOverdue());
                                InvoiceLine il = new InvoiceLine(line.getID(), line.getMyInvoice(), line.getMyItem(), line.getWPrice(), line.getPrice(), line.getQuantity(), line.getNotes(), line.getLastDate(), line.getLastCompany(), line.getLastQuantity(), line.getWrhID(), line.getBraID(), line.getTypeCode(), line.getDosCode(), line.getDocNumber(), line.getOverdue(), line.isEY(), line.isGuarantee(), line.isFromCustomer(), line.getTRNID(), line.getManufacturer(), line.getModel(), line.getYear1(), line.getEngineCode(), line.getYear2(),line.getKMTraveled(), line.getReturnCause(), line.getObservations(),line.getDocID(), line.getDocValue(),line.getChargePapi(), line.isExtraCharge(),line.getExtraChargeValue(),line.getExtraChargeLimit(),line.getDiscountReturnPercent(),line.isAllowed(),line.getMessageAllowed());
                                Log.d("asdfg", String.valueOf(il));
                                realm.copyToRealmOrUpdate(il);
                            }
                        });
                    } else {
                        if (line.getLastCompany() != null && checkIfExists(line, gVar.getMyInvoice().getID())) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    lines.get(lineSimple.indexOf(line)).deleteFromRealm();
                                }
                            });
                        }
                    }
                }
            }
            /*realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    invoiceAEY.setTotal((Double) realm.where(InvoiceLine.class).equalTo("myInvoice.ID", invoiceAEY.getID()).sum("Value"));
                    invoiceASY.setTotal((Double) realm.where(InvoiceLine.class).equalTo("myInvoice.ID", invoiceASY.getID()).sum("Value"));
                    invoiceFEY.setTotal((Double) realm.where(InvoiceLine.class).equalTo("myInvoice.ID", invoiceFEY.getID()).sum("Value"));
                    invoiceFSY.setTotal((Double) realm.where(InvoiceLine.class).equalTo("myInvoice.ID", invoiceFSY.getID()).sum("Value"));
                    gVar.getMyTransaction().setTotal(invoiceAEY.getTotal()+invoiceASY.getTotal()+invoiceFEY.getTotal()+invoiceFSY.getTotal());
                }
            });*/
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    /*private void getCatItems(String catDesc) {
        try {
            ArrayList<Item> items = new ArrayList<>();
            items.addAll(realm.where(Item.class).equalTo("myCategory.City", catDesc).findAllSorted("Code"));
            Log.d("asdfg", String.valueOf(items));
            fillLines(items);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }*/

    private void searchItems() {
        try {
            if (!String.valueOf(search.getText()).equals("")) {
                String str = String.valueOf(search.getText());
                String parts[] = str.split("\\*");
                int l = parts.length;
                Log.d("asdfg", str + " parts: " + l);
                final ArrayList<Item> items = new ArrayList<>();
                if (l == 1) {
                    items.addAll(realm.where(Item.class)
                            .beginGroup()
                            .beginsWith("Description", str, Case.INSENSITIVE).or()
                            .beginsWith("Code", str, Case.INSENSITIVE)
                            .endGroup()
                            .findAll().sort("Code"));
                    for (AlternateCode code : realm.where(AlternateCode.class).beginsWith("AltCode", str, Case.INSENSITIVE).findAll()) {
                        Log.d("asdfg", "hi");
                        if (items.size() > 300) {
                            break;
                        }
                        Log.d("asdfg", "items size" + String.valueOf(items.size()));
                        if (!items.contains(code.getMyItem())) {
                            items.add(code.getMyItem());
                        }
                    }
                } else if (l == 2) {
                    items.addAll(realm.where(Item.class)
                            .beginGroup()
                            .beginsWith("Description", str, Case.INSENSITIVE).or()
                            .beginsWith("Code", parts[0], Case.INSENSITIVE).contains("Code", parts[1], Case.INSENSITIVE)
                            .endGroup()
                            .findAll().sort("Code"));
                    for (AlternateCode code : realm.where(AlternateCode.class).beginsWith("AltCode", parts[0], Case.INSENSITIVE).contains("AltCode", parts[1], Case.INSENSITIVE).findAll()) {
                        Log.d("asdfg", "items size" + String.valueOf(items.size()));
                        if (items.size() > 300) {
                            break;
                        }
                        if (!items.contains(code.getMyItem())) {
                            items.add(code.getMyItem());
                        }
                    }
                } else if (l == 3) {
                    items.addAll(realm.where(Item.class)
                            .beginGroup()
                            .beginsWith("Description", str, Case.INSENSITIVE).or()
                            .beginsWith("Code", parts[0], Case.INSENSITIVE).contains("Code", parts[1], Case.INSENSITIVE).contains("Code", parts[2], Case.INSENSITIVE)
                            .endGroup()
                            .findAll().sort("Code"));
                    for (AlternateCode code : realm.where(AlternateCode.class).beginsWith("AltCode", parts[0], Case.INSENSITIVE).contains("AltCode", parts[1], Case.INSENSITIVE).contains("AltCode", parts[2], Case.INSENSITIVE).findAll()) {
                        Log.d("asdfg", "items size" + String.valueOf(items.size()));
                        if (items.size() > 300) {
                            break;
                        }
                        if (!items.contains(code.getMyItem())) {
                            items.add(code.getMyItem());
                        }
                    }
                } else /*if (l == 4)*/ {
                    items.addAll(realm.where(Item.class)
                            .beginGroup()
                            .beginsWith("Description", str, Case.INSENSITIVE).or()
                            .beginsWith("Code", parts[0], Case.INSENSITIVE).contains("Code", parts[1], Case.INSENSITIVE).contains("Code", parts[2], Case.INSENSITIVE).contains("Code", parts[3], Case.INSENSITIVE)
                            .endGroup()
                            .findAll().sort("Code"));
                    for (AlternateCode code : realm.where(AlternateCode.class).beginsWith("AltCode", parts[0], Case.INSENSITIVE).contains("AltCode", parts[1], Case.INSENSITIVE).contains("AltCode", parts[2], Case.INSENSITIVE).contains("AltCode", parts[3], Case.INSENSITIVE).findAll()) {
                        if (!items.contains(code.getMyItem())) {
                            items.add(code.getMyItem());
                        }
                        Log.d("asdfg", "items size" + String.valueOf(items.size()));
                        if (items.size() > 300) {
                            Toast.makeText(ItemsReturns.this, "Τα αποτελέσματα ήταν πάρα πολλά, εμφανίστηκαν τα 300 πρώτα", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
                java.util.Collections.sort(items, new Comparator<Item>() {
                    @Override
                    public int compare(Item s1, Item s2) {
                        return s1.getCode().compareToIgnoreCase(s2.getCode());
                    }
                });
                if (realm.where(AlternateCode.class).equalTo("AltCode", str, Case.INSENSITIVE).count() > 0) {
                    AlternateCode code = realm.where(AlternateCode.class).equalTo("AltCode", str, Case.INSENSITIVE).findFirst();
                    Log.d("asdfg", "alternate cusCode found:" + String.valueOf(code.getMyItem()));
                    items.remove(code.getMyItem());
                    items.add(0, code.getMyItem());
                }
                Item item = null;
                for (Item i : items) {
                    if (i.getCode().equals(str) || i.getDescription().equals(str)) {
                        Log.d("asdfg", "item found: " + i);
                        item = i;
                    }
                }
                if (item != null) {
                    Log.d("asdfg", "removing item");
                    items.remove(item);
                    items.add(0, item);
                }
                Log.d("asdfg", String.valueOf(items));
                if (items.size() > 300) {
                    ArrayList<Item> items300 = new ArrayList<>();
                    items300.addAll(items.subList(0, 299));
                    fillLines(items300);
                    Toast.makeText(ItemsReturns.this, "Τα αποτελέσματα ήταν πάρα πολλά, εμφανίστηκαν τα 300 πρώτα", Toast.LENGTH_SHORT).show();
                } else {
                    fillLines(items);
                }
                search.setText("");
            } else {
                Toast.makeText(ItemsReturns.this, "Δεν έχετε συμπληρώσει το κείμενο αναζήτησης.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private void fillLines(ArrayList<Item> items) {
        try {
            lineSimple = new ArrayList<>();
            lines = new ArrayList<>();
            for (final Item i : items) {
                if (checkIfExists(i.getID(), gVar.getMyInvoice().getID())) {
                    lines.addAll(realm.where(InvoiceLine.class).equalTo("myItem.ID", i.getID()).equalTo("myInvoice.ID", gVar.getMyInvoice().getID()).findAll());
                } else {
                    InvoiceLine myLine = new InvoiceLine(UUID.randomUUID().toString(), gVar.getMyInvoice(), i, 0.0, i.getPrice() == null ? 0.0 : i.getPrice(), 0.0, "", "", null, 0.0, null, null, null, null, null, 0, true, false, false, "", "", "", null, "", null, null, "", "",null,0.0, 0.0,false,0.0, 0.0,0.0,false,"");
                    lines.add(myLine);
                }
            }

            for (InvoiceLine line : lines) {

                lineSimple.add(new InvoiceLineSimple(line.getID(), line.getMyInvoice(), line.getMyItem(), line.getWPrice(), line.getPrice(), line.getQuantity(), line.getNotes(), line.getLastDate(), line.getLastCompany(), line.getLastQuantity(), line.getWrhID(), line.getBraID(), line.getTypeCode(), line.getDosCode(), line.getDocNumber(), line.getOverdue(), line.isEY(), line.isGuarantee(), line.isFromCustomer(), line.getTRNID(), line.getManufacturer(), line.getModel(), line.getYear1(), line.getEngineCode(), line.getYear2(),line.getKMTraveled(), line.getReturnCause(), line.getObservations(),line.getDocID(), line.getDocValue(),line.getChargePapi(), line.isExtraCharge(),line.getExtraChargeValue(),line.getExtraChargeLimit(),line.getDiscountReturnPercent(),line.isAllowed(),line.getMessageAllowed()));
            }
            mAdapter = new ListViewReturnsItemsAdapter(this, lineSimple);
            listView.setAdapter(mAdapter);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    boolean isRecentPurchasesActive(InvoiceLineSimple line) {
        if (recentPurchasesFragment != null && recentPurchasesFragment.isVisible()) {
            return true;
        } else {
            ArrayList<InvoiceLineSimple> oldLines = new ArrayList<>();
            for (InvoiceLineSimple l : lineSimple) {
                if (l.getMyItem() == line.getMyItem()) {
                    oldLines.add(l);
                }
                Log.d("asdfg", "isRecentPurchasesActive: oldLines=" + l.getDosCode() + l.getDocNumber() + l.getPrice());
            }
            recentPurchasesFragment = new RecentPurchasesFragment();
            recentPurchasesFragment.setOldLines(oldLines);
            return false;
        }
    }

    public boolean checkIfExists(InvoiceLineSimple line, String invoiceID) {

        try {
            RealmQuery<InvoiceLine> query = realm.where(InvoiceLine.class)
                    .equalTo("myItem.ID", line.getMyItem().getID())
                    .equalTo("myInvoice.ID", invoiceID)
                    .equalTo("LastCompany.InAppID", line.getLastCompany().getInAppID())
                    .equalTo("DosCode", line.getDosCode())
                    .equalTo("DocNumber", line.getDocNumber())
                    .equalTo("Price", line.getPrice());

            return query.count() != 0;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return false;
        }
    }

    public boolean checkIfExists(String itemID, String invoiceID) {

        try {
            RealmQuery<InvoiceLine> query = realm.where(InvoiceLine.class)
                    .equalTo("myItem.ID", itemID).equalTo("myInvoice.ID", invoiceID);

            return query.count() != 0;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return false;
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("ItemsReturns Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}