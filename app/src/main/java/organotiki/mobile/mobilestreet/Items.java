package organotiki.mobile.mobilestreet;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.AlternateCode;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLine;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;
import organotiki.mobile.mobilestreet.objects.Item;
import organotiki.mobile.mobilestreet.objects.ItemCategory;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class Items extends AppCompatActivity implements View.OnClickListener, Communicator {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    ListView listView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    EditText search;
    Button searchItem;
    Realm realm;
    GlobalVar gVar;
    RealmResults<ItemCategory> categories;
    ArrayList<InvoiceLine> lines;
    ArrayList<InvoiceLineSimple> lineSimple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_items);

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
                    searchItems();
                }
            });
            search = (EditText) findViewById(R.id.search);
            search.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            listView = (ListView) findViewById(R.id.listViewItems);

            if (toolbar != null) {
                Log.d("asdfg", "hi");
                toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        try {
                            saveItemList();
                            Intent i = new Intent(Items.this, NewOrder.class);
                            startActivity(i);
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                        return false;
                    }
                });
            }

            lineSimple = (ArrayList<InvoiceLineSimple>) getLastCustomNonConfigurationInstance();
            if (lineSimple!=null){
                ListViewItemsAdapter adapter = new ListViewItemsAdapter(this,lineSimple);
                listView.setAdapter(adapter);
            }

            getCategories();

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

    @Override
    public void onClick(View v) {

    }

    private void getCategories() {
        try {
            /*expListView = (ExpandableListView) findViewById(R.id.categories);
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


            listAdapter = new ExpandableListAdapter(Items.this, listDataHeader, listDataChild);

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
            });*/
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private void saveItemList() {
        /*try {
            if (lineSimple!=null) {
                for (final InvoiceLineSimple line:lineSimple) {
                    if (line.getQuantity()>0){
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealmOrUpdate(new InvoiceLine(line.getID(),line.getMyInvoice(),line.getMyItem(),line.getPrice(),line.getQuantity(),line.getDiscount(),line.getValue(),line.getComments(),line.getCompanyItem()));
                            }
                        });
                    }else{
                        if (checkIfExists(line.getMyItem().getID(),gVar.getMyInvoice().getID())) {
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
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    gVar.getMyInvoice().setTotal((Double) realm.where(InvoiceLine.class).equalTo("myInvoice.ID", gVar.getMyInvoice().getID()).sum("Value"));
                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }*/
    }

    private void getCatItems(String catDesc) {
        try {
            ArrayList<Item> items = new ArrayList<>();
            items.addAll(realm.where(Item.class).equalTo("myCategory.City",catDesc).findAll());
            Log.d("asdfg", String.valueOf(items));
            fillLines(items);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private void searchItems(){
        try {
            String str = String.valueOf(search.getText());
            final ArrayList<Item> items = new ArrayList<>();
            items.addAll(realm.where(Item.class)
            .beginGroup()
            .contains("Description", str, Case.INSENSITIVE).or()
            .contains("Code", str, Case.INSENSITIVE)
            .endGroup()
            .findAll());
            for (AlternateCode code:realm.where(AlternateCode.class).contains("AltCode", str, Case.INSENSITIVE).findAll()){
                if (!items.contains(code.getMyItem())) {
                    items.add(code.getMyItem());
                }
            }
            Log.d("asdfg", String.valueOf(items));
            fillLines(items);
            search.setText("");
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private void fillLines(ArrayList<Item> items){
        /*try {
            lineSimple = new ArrayList<>();
            lines = new ArrayList<>();
            for (final Item i:items) {
                if (checkIfExists(i.getID(),gVar.getMyInvoice().getID())) {
                    lines.add(realm.where(InvoiceLine.class).equalTo("myItem.ID",i.getID()).equalTo("myInvoice.ID",gVar.getMyInvoice().getID()).findFirst());
                } else {
                    InvoiceLine myLine = new InvoiceLine(UUID.randomUUID().toString(),gVar.getMyInvoice(),i ,i.getPrice(),0.0, 0.0, 0.0, "", "");
                    lines.add(myLine);
                }
            }

            for (InvoiceLine line:lines) {
                lineSimple.add(new InvoiceLineSimple(line.getID(),line.getMyInvoice(),line.getMyItem(), line.getPrice(),line.getQuantity(),line.getDiscount(),line.getValue(),line.getNotes(),line.getCompanyItem()));
            }

            ListViewItemsAdapter mAdapter = new ListViewItemsAdapter(this,lineSimple);
            listView.setAdapter(mAdapter);

            newRouting.setText("");
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }*/
    }

    public boolean checkIfExists(String itemID,String invoiceID){

        try {
            RealmQuery<InvoiceLine> query = realm.where(InvoiceLine.class)
                    .equalTo("myItem.ID", itemID).equalTo("myInvoice.ID", invoiceID);

            return query.count() != 0;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return false;
        }
    }
}