package organotiki.mobile.mobilestreet;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.Invoice;
import organotiki.mobile.mobilestreet.objects.InvoiceLine;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class NewOrder extends AppCompatActivity implements Communicator {

    TextView txvCode, txvName, txvTIN, txvAddress, txvCity, txvPostalCode, txvPhone1, txvPhone2, txvMobile, txvEmail;
    EditText generalNotes;
    Realm realm;
    GlobalVar gVar;
    Button invoiceType = null, paymentTerm = null;
    ListView listView;
    ArrayList<InvoiceLine> lines;
    ArrayList<InvoiceLineSimple> lineSimple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_order);

        /*try {
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            Toolbar toolbar = (Toolbar) findViewById(R.id.newOrderBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            TextView title = (TextView) findViewById(R.id.toolbar_title);
            txvCode = (TextView) findViewById(R.id.textView_code);
            txvName = (TextView) findViewById(R.id.textView_name);
            txvTIN = (TextView) findViewById(R.id.textView_TIN);
            txvAddress = (TextView) findViewById(R.id.textView_address);
            txvCity = (TextView) findViewById(R.id.textView_city);
            txvPostalCode = (TextView) findViewById(R.id.textView_postalCode);
            txvPhone1 = (TextView) findViewById(R.id.textView_phone1);
            txvPhone2 = (TextView) findViewById(R.id.textView_phone2);
            txvMobile = (TextView) findViewById(R.id.textView_mobile);
            txvEmail = (TextView) findViewById(R.id.textView_email);
            listView = (ListView) findViewById(R.id.listViewOrderItems);
            generalNotes = (EditText) findViewById(R.id.editText_generalNotes);

            invoiceType = (Button) findViewById(R.id.invoiceType);
            invoiceType.setTransformationMethod(null);
            invoiceType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        FragmentManager manager = getFragmentManager();
                        InvoiceTypeFragment frag = new InvoiceTypeFragment();
                        frag.show(manager, "Invoice Type Fragment");

                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }

                }
            });
            if(realm.where(Invoice.class).equalTo("ID", gVar.getMyInvoice().getID()).findFirst().getMyType()!=null) {
                selectInvoiceType();
            }
            paymentTerm = (Button) findViewById(R.id.button_payment_term);
            paymentTerm.setTransformationMethod(null);
            paymentTerm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        FragmentManager manager = getFragmentManager();
                        PaymentTermFragment frag = new PaymentTermFragment();
                        frag.show(manager, "Payment Term Fragment");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }

                }
            });

            fillLines();

            if(gVar.getMyInvoice().getMyPayment()!=null) {
                selectPaymentTerm();
            }

            if(gVar.getMyInvoice().getMyCustomer()!=null) {
                selectCustomer();
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(),e);
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_order_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*try {
            Intent intent;

            switch (item.getItemId()) {
                case R.id.newItem:
                    saveItemList();

                    try {
                        intent = new Intent(NewOrder.this, Items.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(),e);
                    }

                    return super.onOptionsItemSelected(item);

                case R.id.createCustomer:
                    FragmentManager createmanager = getFragmentManager();
                    CreateCustomerFragment createfrag = new CreateCustomerFragment();
                    createfrag.show(createmanager, "Create Customer Fragment");

                    return super.onOptionsItemSelected(item);

                case R.id.searchCustomer:
                    FragmentManager searchmanager = getFragmentManager();
                    SearchCustomerFragment searchfrag = new SearchCustomerFragment();
                    searchfrag.show(searchmanager, "Search Customer Fragment");

                    return super.onOptionsItemSelected(item);

                case R.id.next:
                    try {
                        saveItemList();

                        String message="";

                        message+=gVar.getMyInvoice().getMyType()== null?"Δεν επιλέξατε τύπο παραστατικού.":"";
                        message+=gVar.getMyInvoice().getMyPayment()== null?message.equals("")?"Δεν επιλέξατε τρόπο πληρωμής.":"\nΔεν επιλέξατε τρόπο πληρωμής.":"";
                        message+=gVar.getMyInvoice().getMyCustomer()== null?message.equals("")?"Δεν επιλέξατε πελάτη.":"\nΔεν επιλέξατε πελάτη.":"";
                        message+=lines.size() == 0?message.equals("")?"Δεν επιλέξατε κανένα είδος.":"\nΔεν επιλέξατε κανένα είδος.":"";

                        if (message.equals("")) {
                            intent = new Intent(NewOrder.this, CheckOut.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }

                    return super.onOptionsItemSelected(item);


                case android.R.id.home:
                    doExit();
                    return true;

            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return false;
        }*/
        return false;
    }

    private void doExit() {

        /*AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                NewOrder.this);

        alertDialog.setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveItemList();
                NavUtils.navigateUpFromSameTask(NewOrder.this);
            }
        });

        alertDialog.setNegativeButton("Όχι", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(InvoiceLine.class).equalTo("myInvoice.ID",gVar.getMyInvoice().getID()).findAll().deleteAllFromRealm();
                        gVar.getMyInvoice().deleteFromRealm();
                    }
                });
                NavUtils.navigateUpFromSameTask(NewOrder.this);
            }
        });
        alertDialog.setNeutralButton("Άκυρο",null);
        alertDialog.setMessage("Η παραγγελία δεν έχει ολοκληρωθεί.\nΘες να την αποθηκεύσεις;");
        alertDialog.setTitle(R.string.app_name);
        alertDialog.show();*/
    }
    @Override
    public void respondCustomerSearch(final Customer customer, final Address address) {
        /*realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                gVar.getMyInvoice().setMyAddress(address);
                gVar.getMyInvoice().setMyCustomer(customer);
            }
        });
        selectCustomer();
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            DrawerLayout mDrowerLayout = (DrawerLayout) findViewById(R.id.drower_customer);
            mDrowerLayout.openDrawer(Gravity.LEFT);
        }*/
    }

    @Override
    public void onBackPressed() {
        doExit();
    }

    @Override
    public void respondInvoiceType() {
        selectInvoiceType();
    }

    @Override
    public void respondPaymentTerm() {
        selectPaymentTerm();
    }


    @Override
    public void respondCustomerCreate() {
        selectCustomer();
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            DrawerLayout mDrowerLayout = (DrawerLayout) findViewById(R.id.drower_customer);
            mDrowerLayout.openDrawer(Gravity.LEFT);
        }
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

    public void selectCustomer() {
        /*try {
            Invoice invoice = gVar.getMyInvoice();
            Log.d("asdfg", String.valueOf(invoice));
            Customer c = invoice.getMyCustomer();
            Address a = invoice.getMyAddress();
            txvCode.setText(c.getCode() != null && !c.getCode().isEmpty() ? c.getCode().replaceFirst("\\s+$", "") : "");
            txvName.setText(c.getCode() != null && !c.getName().isEmpty() ? c.getName().replaceFirst("\\s+$", "") : "");
            txvTIN.setText(c.getTIN() != null && !c.getTIN().isEmpty() ? c.getTIN().replaceFirst("\\s+$", "") : "");
            txvAddress.setText(a.getStreet() != null && !a.getStreet().isEmpty() ? a.getStreet().replaceFirst("\\s+$", "") : "");
            txvCity.setText(a.getCity() != null && !a.getCity().isEmpty() ? a.getCity().replaceFirst("\\s+$", "") : "");
            txvPostalCode.setText(a.getPostalCode() != null && !a.getPostalCode().isEmpty() ? a.getPostalCode().replaceFirst("\\s+$", "") : "");
            txvPhone1.setText(a.getPhone1() != null && !a.getPhone1().isEmpty() ? a.getPhone1().replaceFirst("\\s+$", "") : "");
            txvPhone2.setText(a.getPhone2() != null && !a.getPhone2().isEmpty() ? a.getPhone2().replaceFirst("\\s+$", "") : "");
            txvMobile.setText(a.getMobile() != null && !a.getMobile().isEmpty() ? a.getMobile().replaceFirst("\\s+$", "") : "");
            txvEmail.setText(a.getEmail() != null && !a.getEmail().isEmpty() ? a.getEmail().replaceFirst("\\s+$", "") : "");
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }*/
    }

    public void selectPaymentTerm(){
        /*try {
            paymentTerm.setText(gVar.getMyInvoice().getMyPayment().getDescription());
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }*/
    }

    public void selectInvoiceType(){
        /*try {
            invoiceType.setText(gVar.getMyInvoice().getMyType().getDescription());
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }*/
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
                    gVar.getMyInvoice().setGeneralNotes(String.valueOf(generalNotes.getText()));
                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }*/
    }

    private void fillLines(){
        /*try {
            generalNotes.setText(gVar.getMyInvoice().getGeneralNotes());

            lineSimple = new ArrayList<>();
            lines = new ArrayList<>();

            lines.addAll(realm.where(InvoiceLine.class).equalTo("myInvoice.ID",gVar.getMyInvoice().getID()).findAll());
            Log.w("asdfg", String.valueOf(lines.size()));
            for (InvoiceLine line:lines) {
                lineSimple.add(new InvoiceLineSimple(line.getID(),line.getMyInvoice(),line.getMyItem(), line.getPrice(),line.getQuantity(),line.getDiscount(),line.getValue(),line.getNotes(),line.getCompanyItem()));
            }

            ListViewItemsAdapter mAdapter = new ListViewItemsAdapter(this,lineSimple);
            listView.setAdapter(mAdapter);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }*/
    }

    public boolean checkIfExists(String itemID,String invoiceID){

        RealmQuery<InvoiceLine> query = realm.where(InvoiceLine.class)
                .equalTo("myItem.ID", itemID).equalTo("myInvoice.ID", invoiceID);

        return query.count() != 0;
    }
}
