package organotiki.mobile.mobilestreet;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmQuery;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.Invoice;
import organotiki.mobile.mobilestreet.objects.InvoiceLine;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;
import organotiki.mobile.mobilestreet.objects.ReturnBalance;
import organotiki.mobile.mobilestreet.objects.Setting;

/**
 * Created by Thanasis on 15/7/2016.
 */
public class Returns extends AppCompatActivity implements Communicator {

    TextView txvCode, txvName, txvTIN, txvAddress, txvCity, txvPostalCode, txvPhone1, txvPhone2, txvMobile, txvEmail, txvMessage,txvManagementCost;;
    EditText generalNotes;
	DecimalFormat formatter = new DecimalFormat("#,###,##0.00");
    Realm realm;
    GlobalVar gVar;
    Button invoiceType = null, paymentTerm = null;
    ListView listView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ArrayList<InvoiceLine> lines;
    ArrayList<InvoiceLineSimple> lineSimple;
    MenuItem createCustomer;
    ListViewReturnsItemsAdapter mAdapter;
    AlertDialog mAlertDialog;
    RecentPurchasesFragment recentPurchasesFragment;
	VolleyRequests request;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_returns);

        try {
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            Toolbar toolbar = (Toolbar) findViewById(R.id.returnsBar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

			request = new VolleyRequests();
			
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
            txvMessage = (TextView) findViewById(R.id.textView_message);
			txvManagementCost = (TextView) findViewById(R.id.textView_extraChargeManagementCost);
            listView = (ListView) findViewById(R.id.listViewReturnsItems);
            generalNotes = (EditText) findViewById(R.id.editText_generalNotes);
            mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_items);

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshContent();
                }
            });

            invoiceType = (Button) findViewById(R.id.invoiceType);
            invoiceType.setTransformationMethod(null);

            paymentTerm = (Button) findViewById(R.id.button_payment_term);
            paymentTerm.setTransformationMethod(null);

            if (gVar.getMyInvoice().getMyCustomer() != null) {
                selectCustomer();
            }

            fillLines();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
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
        createCustomer = menu.findItem(R.id.createCustomer);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        createCustomer.setVisible(false);
            /*createCustomer.setEnabled(false);
            createCustomer.getIcon().setAlpha(130);*/
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            Intent intent;

            switch (item.getItemId()) {
                case R.id.newItem:
                    saveItemList();

                    try {
                        if (gVar.getMyInvoice().getMyCustomer() != null) {
                            intent = new Intent(Returns.this, ItemsReturns.class);
                            startActivity(intent);
                        } else {
                            String message = "Δεν επιλέξατε πελάτη.";
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
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

                        String message = "";
                        lines = new ArrayList<>();
                        lines.addAll(realm.where(InvoiceLine.class).equalTo("myInvoice.ID", gVar.getMyInvoice().getID()).findAll());
                        int l = lines.size();

//                        message+=gVar.getMyInvoice().getMyType()== null?"Δεν επιλέξατε τύπο παραστατικού.":"";
//                        message+=gVar.getMyInvoice().getMyPayment()== null?message.equals("")?"Δεν επιλέξατε τρόπο πληρωμής.":"\nΔεν επιλέξατε τρόπο πληρωμής.":"";
                        message += gVar.getMyInvoice().getMyCustomer() == null ? message.equals("") ? "Δεν επιλέξατε πελάτη." : "\nΔεν επιλέξατε πελάτη." : "";
                        message += l == 0 ? message.equals("") ? "Δεν επιλέξατε κανένα είδος." : "\nΔεν επιλέξατε κανένα είδος." : "";


                        //message += realm.where(InvoiceLine.class).equalTo("myInvoice.ID", gVar.getMyInvoice().getID()).beginGroup().equalTo("LastDate", "ΔΕΝ ΒΡΕΘΗΚΕ").or().equalTo("LastDate", "").or().equalTo("Overdue",1).endGroup().count() > 0 ? message.equals("") ? "Για ένα ή παραπάνω αντικείμενα δεν βρέθηκαν πρόσφατες πωλήσεις." : "\nΓια ένα ή παραπάνω αντικείμενα δεν βρέθηκαν πρόσφατες πωλήσεις." : "";
                        Setting GuaranteeLimit = (Setting) this.realm.where(Setting.class).equalTo("Code", "Garantylimit").equalTo("System", "6").findFirst();
                        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                        Calendar c = Calendar.getInstance();

                        for(int i=0;i<lines.size();i++){
                            if(!lines.get(i).isAllowed()){
                                message+="\n"+lines.get(i).getMessageAllowed();
                            }
                            if(lines.get(i).getLastDate()==""||lines.get(i).getLastDate()=="ΔΕΝ ΒΡΕΘΗΚΕ"||lines.get(i).getOverdue()==1){
                                if(lines.get(i).getOverdue()==1){
                                    if(!lines.get(i).isGuarantee()){
                                        message+="\n Δεν βρέθηκαν πρόσφατες πωλήσεις για το αντικείμενο:"+lines.get(i).getMyItem().getCode();
                                    }else{
                                        Date myDate;
                                        try {
                                            myDate = df.parse(lines.get(i).getLastDate());
                                            c.add(Calendar.MONTH, -Integer.parseInt(GuaranteeLimit.getValue()));
                                            if(myDate.before(c.getTime())){
                                                message+="\n Δεν επιτρέπεται η εγγύηση για το αντικείμενο:"+lines.get(i).getMyItem().getCode();
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                                else{
                                    message+="\n Δεν βρέθηκαν πρόσφατες πωλήσεις για το αντικείμενο:"+lines.get(i).getMyItem().getCode();
                                }


                            }
                        }


                        if (message.equals("")) {
                            intent = new Intent(Returns.this, CheckOutReturns.class);
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
        }
        return false;
    }

    private void refreshContent() {
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void doExit() {
        if (gVar.getMyInvoice().getMyCustomer() == null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    gVar.getMyInvoice().deleteFromRealm();
                }
            });
            NavUtils.navigateUpFromSameTask(Returns.this);
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                    Returns.this);

            alertDialog.setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveItemList();
                    NavUtils.navigateUpFromSameTask(Returns.this);
                }
            });

            alertDialog.setNegativeButton("Όχι", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.where(InvoiceLine.class).equalTo("myInvoice.ID", gVar.getMyInvoice().getID()).findAll().deleteAllFromRealm();
                            gVar.getMyInvoice().deleteFromRealm();
                        }
                    });
                    NavUtils.navigateUpFromSameTask(Returns.this);
                }
            });
            alertDialog.setNeutralButton("Άκυρο", null);
            alertDialog.setMessage("Η επιστροφή ειδών δεν έχει ολοκληρωθεί.\nΘες να την αποθηκεύσεις;");
            alertDialog.setTitle(R.string.app_name);
            mAlertDialog = alertDialog.create();
            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTransformationMethod(null);

                    Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    negativeButton.setTransformationMethod(null);

                    Button neutralButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                    neutralButton.setTransformationMethod(null);
                }
            });

            mAlertDialog.show();
        }
    }

    @Override
    public void respondCustomerSearch(final Customer customer, final Address address) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                gVar.getMyInvoice().setMyAddress(address);
                gVar.getMyInvoice().setMyCustomer(customer);
            }
        });
        selectCustomer();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            DrawerLayout mDrowerLayout = (DrawerLayout) findViewById(R.id.drower_customer);
            mDrowerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        doExit();
    }

    @Override
    public void respondInvoiceType() {
        /*selectInvoiceType()*/
        ;
    }

    @Override
    public void respondPaymentTerm() {
        /*selectPaymentTerm();*/
    }


    @Override
    public void respondCustomerCreate() {
        selectCustomer();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            DrawerLayout mDrowerLayout = (DrawerLayout) findViewById(R.id.drower_customer);
            mDrowerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void respondVolleyRequestFinished(Integer position, JSONObject jsonObject) {
        switch (position) {
            case 0:
                recentPurchasesFragment.respondRecentPurchases(jsonObject);
                FragmentManager manager = getFragmentManager();
                recentPurchasesFragment.show(manager, "Recent Purchases Fragment");
                break;
			case 3:
				ReturnBalance returnBalance = (ReturnBalance) this.realm.where(ReturnBalance.class).equalTo("MyCustomer.ID", this.gVar.getMyInvoice().getMyCustomer().getID()).findFirst();
				if (returnBalance != null) {
					Setting ReturnPercentSetting = (Setting) this.realm.where(Setting.class).equalTo("Code", "ReturnPercent").equalTo("System", "2").findFirst();
					Setting ReturnValueSetting = (Setting) this.realm.where(Setting.class).equalTo("Code", "ValueLimit").equalTo("System", "2").findFirst();
					final Setting ReturnManageCostSetting = (Setting) this.realm.where(Setting.class).equalTo("Code", "ManagementCost").equalTo("System", "2").findFirst();
					if (ReturnPercentSetting == null || ReturnValueSetting == null || ReturnManageCostSetting == null || returnBalance.getReturnsPercent().doubleValue() <= Double.parseDouble(ReturnPercentSetting.getValue()) || returnBalance.getReturnsValue().doubleValue() <= Double.parseDouble(ReturnValueSetting.getValue())) {
						this.realm.executeTransaction(new Realm.Transaction() {
							public void execute(Realm realm) {
								Returns.this.gVar.getMyInvoice().setManagementCostPercent(0.0);
							}
						});
					} else {
						this.realm.executeTransaction(new Realm.Transaction() {
							public void execute(Realm realm) {
								Returns.this.gVar.getMyInvoice().setManagementCostPercent(Double.valueOf(Double.parseDouble(ReturnManageCostSetting.getValue())));
							}
						});
					}
					this.txvManagementCost.setText(getString(R.string.managementCost_s, new Object[]{formatter.format(this.gVar.getMyInvoice().getManagementCostPercent())}));
				}
				this.mAdapter.notifyDataSetChanged();
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
        fillLines();
    }

    public void selectCustomer() {
        try {
            Customer c = gVar.getMyInvoice().getMyCustomer();
            Address a = gVar.getMyInvoice().getMyAddress();
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
			txvMessage.setText(c.getMessage() != null && !c.getMessage().isEmpty() ? c.getMessage().replaceFirst("\\s+$", "") : "");
        	txvManagementCost.setText(c.getMessage() != null && !c.getMessage().isEmpty() ? c.getMessage().replaceFirst("\\s+$", "") : "");

			if (gVar.getMyInvoice().getManagementCostPercent() == null) {
				request.getReturnBalance(this, gVar.getMyInvoice().getMyCustomer());
			} else {
				txvManagementCost.setText(getString(R.string.managementCost_s, new Object[] { formatter.format(gVar.getMyInvoice().getManagementCostPercent()) }));
			}
		} catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private void saveItemList() {
        try {
            if (lineSimple != null) {
                for (final InvoiceLineSimple line : lineSimple) {
                    if (line.getQuantity() > 0) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealmOrUpdate(new InvoiceLine(line.getID(), line.getMyInvoice(), line.getMyItem(), line.getWPrice(), line.getPrice(), line.getQuantity(), line.getNotes(), line.getLastDate(), line.getLastCompany(), line.getLastQuantity(), line.getWrhID(), line.getBraID(), line.getTypeCode(), line.getDosCode(), line.getDocNumber(), line.getOverdue(), line.isEY(), line.isGuarantee(), line.isFromCustomer(), line.getTRNID(), line.getManufacturer(), line.getModel(), line.getYear1(), line.getEngineCode(), line.getYear2(),line.getKMTraveled(), line.getReturnCause(), line.getObservations(),line.getDocID(), line.getDocValue(), line.getChargePapi(), line.isExtraCharge(), line.getExtraChargeValue(), line.getExtraChargeLimit(),line.getDiscountReturnPercent(),line.isAllowed(),line.getMessageAllowed()));
                            }
                        });
                    } else {
                        if (line.getLastDate()!=null || checkIfExists(line, gVar.getMyInvoice().getID())) {
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
                    gVar.getMyInvoice().setTotalAEY((Double) realm.where(InvoiceLine.class).equalTo("myInvoice.ID", gVar.getMyInvoice().getID()).equalTo("isGuarantee", false).equalTo("LastCompany.InAppID", "1").equalTo("isEY", true).sum("Value"));
                    gVar.getMyInvoice().setTotalASY((Double) realm.where(InvoiceLine.class).equalTo("myInvoice.ID", gVar.getMyInvoice().getID()).equalTo("isGuarantee", false).equalTo("LastCompany.InAppID", "1").equalTo("isEY", false).sum("Value"));
                    gVar.getMyInvoice().setTotalFEY((Double) realm.where(InvoiceLine.class).equalTo("myInvoice.ID", gVar.getMyInvoice().getID()).equalTo("isGuarantee", false).equalTo("LastCompany.InAppID", "2").equalTo("isEY", true).sum("Value"));
                    gVar.getMyInvoice().setTotalFSY((Double) realm.where(InvoiceLine.class).equalTo("myInvoice.ID", gVar.getMyInvoice().getID()).equalTo("isGuarantee", false).equalTo("LastCompany.InAppID", "2").equalTo("isEY", false).sum("Value"));
                    gVar.getMyInvoice().setTotal();
                    gVar.getMyInvoice().setNotes(String.valueOf(generalNotes.getText()));
                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    boolean isRecentPurchasesActive(InvoiceLineSimple line) {
        if (recentPurchasesFragment != null && recentPurchasesFragment.isVisible()) {
            return true;
        }else{
            ArrayList<InvoiceLineSimple> oldLines = new ArrayList<>();
            for (InvoiceLineSimple l: lineSimple) {
                if (l.getMyItem().getID().equals(line.getMyItem().getID())){
                    oldLines.add(l);
                    Log.d("asdfg", "isRecentPurchasesActive: oldLines="+l.getDosCode()+l.getDocNumber()+l.getPrice());
                }
            }
            recentPurchasesFragment = new RecentPurchasesFragment();
            recentPurchasesFragment.setOldLines(oldLines);
            return false;
        }
    }

    private void fillLines() {
        try {
            generalNotes.setText(gVar.getMyInvoice().getNotes());

            lineSimple = new ArrayList<>();
            lines = new ArrayList<>();

            lines.addAll(realm.where(InvoiceLine.class).equalTo("myInvoice.ID", gVar.getMyInvoice().getID()).findAll());
            Log.w("asdfg", "number of lines: " + String.valueOf(lines.size()));
            for (InvoiceLine line : lines) {
                lineSimple.add(new InvoiceLineSimple(line.getID(), line.getMyInvoice(), line.getMyItem(), line.getWPrice(), line.getPrice(), line.getQuantity(), line.getNotes(), line.getLastDate(), line.getLastCompany(), line.getLastQuantity(), line.getWrhID(), line.getBraID(), line.getTypeCode(), line.getDosCode(), line.getDocNumber(), line.getOverdue(), line.isEY(), line.isGuarantee(), line.isFromCustomer(), line.getTRNID(), line.getManufacturer(), line.getModel(), line.getYear1(), line.getEngineCode(), line.getYear2(),line.getKMTraveled(), line.getReturnCause(), line.getObservations(), line.getDocID(), line.getDocValue(), line.getChargePapi(), line.isExtraCharge(), line.getExtraChargeValue(), line.getExtraChargeLimit(),line.getDiscountReturnPercent(),line.isAllowed(),line.getMessageAllowed()));
            }

            mAdapter = new ListViewReturnsItemsAdapter(this, lineSimple);
            mAdapter.setmSwipeRefreshLayout(mSwipeRefreshLayout);
            listView.setAdapter(mAdapter);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public boolean checkIfExists(InvoiceLineSimple line, String invoiceID) {

        RealmQuery<InvoiceLine> query = realm.where(InvoiceLine.class)
                .equalTo("myItem.ID", line.getMyItem().getID())
                .equalTo("myInvoice.ID", invoiceID)
                .equalTo("myCompany.InAppID", line.getLastCompany().getInAppID())
                .equalTo("DosCode", line.getDosCode())
                .equalTo("DocNumber", line.getDocNumber())
                .equalTo("Price", line.getPrice());

        return query.count() != 0;
    }
}
