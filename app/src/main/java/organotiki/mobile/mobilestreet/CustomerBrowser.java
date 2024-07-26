package organotiki.mobile.mobilestreet;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.github.mikephil.charting.utils.Utils;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.ArrayList;
import java.util.UUID;
import org.json.JSONObject;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.BrowserCityFilter;
import organotiki.mobile.mobilestreet.objects.BrowserCustomer;
import organotiki.mobile.mobilestreet.objects.BrowserTotalFilter;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;

public class CustomerBrowser extends AppCompatActivity implements Communicator {
    public int NUM_ITEMS_PAGE = 100;
    Button back;
    private Button btnNext;
    private Button btnPrev;
    CityFilterFragment cityFilterFragment;
    Communicator comm;
    ArrayList<BrowserCustomer> customers;
    Button filters;
    GlobalVar gVar;
    /* access modifiers changed from: private */
    public int increment = 0;
    boolean isSearched;
    ListView listView;
    AlertDialog mAlertDialog;
    private long mLastClickTime;
    MyCusListAdapter myCusListAdapter;
    Button newRouting;
    private int pageCount;
    ArrayList<BrowserCustomer> pageCustomers;
    private ProgressBar progressBar;
    Realm realm;
    VolleyRequests request;
    AppCompatTextView txvPages;

    static /* synthetic */ int access$008(CustomerBrowser x0) {
        int i = x0.increment;
        x0.increment = i + 1;
        return i;
    }

    static /* synthetic */ int access$010(CustomerBrowser x0) {
        int i = x0.increment;
        x0.increment = i - 1;
        return i;
    }

    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView((int) R.layout.activity_customer_browser);
            getWindow().setSoftInputMode(2);
            this.realm = Realm.getDefaultInstance();
            this.gVar = (GlobalVar) this.realm.where(GlobalVar.class).findFirst();
            this.request = new VolleyRequests();
            setSupportActionBar((Toolbar) findViewById(R.id.customerBrowserBar));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            this.txvPages = (AppCompatTextView) findViewById(R.id.textView_pages);
            this.btnPrev = (Button) findViewById(R.id.button_previous);
            this.btnNext = (Button) findViewById(R.id.button_next);
            this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
            this.progressBar.setVisibility(View.VISIBLE);
            this.btnNext.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CustomerBrowser.access$008(CustomerBrowser.this);
                    CustomerBrowser customerBrowser = CustomerBrowser.this;
                    customerBrowser.loadList(customerBrowser.increment);
                }
            });
            this.btnPrev.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CustomerBrowser.access$010(CustomerBrowser.this);
                    CustomerBrowser customerBrowser = CustomerBrowser.this;
                    customerBrowser.loadList(customerBrowser.increment);
                }
            });
            if (((BrowserTotalFilter) this.realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", this.gVar.getMyUser().getID()).findFirst()) == null) {
                this.realm.executeTransaction(new Realm.Transaction() {
                    public void execute(Realm realm) {
                        realm.copyToRealm(new BrowserTotalFilter(UUID.randomUUID().toString(), CustomerBrowser.this.gVar.getMyUser(), false));
                    }
                });
            }
            this.listView = (ListView) findViewById(R.id.listView_customers);
            this.mLastClickTime = 0;
            this.comm = this;
            this.request.getUserCustomers(this);
            respondVolley();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customer_browser_menu, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case 16908332:
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.back.getWindowToken(), 0);
                    finish();
                    return true;
                case R.id.menuItem_filters /*2131231034*/:
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setPositiveButton(getString(R.string.total), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CustomerBrowser.this);
                            alertDialog.setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    CustomerBrowser.this.realm.executeTransaction(new Realm.Transaction() {
                                        public void execute(Realm realm) {
                                            ((BrowserTotalFilter) realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", CustomerBrowser.this.gVar.getMyUser().getID()).findFirst()).setPositive(true);
                                        }
                                    });
                                    CustomerBrowser.this.respondVolley();
                                }
                            });
                            alertDialog.setNegativeButton("Όχι", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    CustomerBrowser.this.realm.executeTransaction(new Realm.Transaction() {
                                        public void execute(Realm realm) {
                                            ((BrowserTotalFilter) realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", CustomerBrowser.this.gVar.getMyUser().getID()).findFirst()).setPositive(false);
                                        }
                                    });
                                    CustomerBrowser.this.respondVolley();
                                }
                            });
                            alertDialog.setMessage(CustomerBrowser.this.getString(R.string.totalFilterQuestion));
                            alertDialog.setTitle(R.string.app_name);
                            CustomerBrowser.this.mAlertDialog = alertDialog.create();
                            CustomerBrowser.this.mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                public void onShow(DialogInterface dialog) {
                                    ((AlertDialog) dialog).getButton(-1).setTransformationMethod((TransformationMethod) null);
                                    ((AlertDialog) dialog).getButton(-2).setTransformationMethod((TransformationMethod) null);
                                }
                            });
                            CustomerBrowser.this.mAlertDialog.show();
                        }
                    });
                    alertDialog.setNegativeButton(getString(R.string.city), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentManager loadManager = CustomerBrowser.this.getFragmentManager();
                            CustomerBrowser.this.cityFilterFragment = new CityFilterFragment();
                            CustomerBrowser.this.cityFilterFragment.show(loadManager, "City Filter Fragment");
                        }
                    });
                    alertDialog.setNeutralButton(getString(R.string.clearFilters), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            CustomerBrowser.this.realm.executeTransaction(new Realm.Transaction() {
                                public void execute(Realm realm) {
                                    realm.where(BrowserCityFilter.class).equalTo("myUser.ID", CustomerBrowser.this.gVar.getMyUser().getID()).findAll().deleteAllFromRealm();
                                    ((BrowserTotalFilter) realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", CustomerBrowser.this.gVar.getMyUser().getID()).findFirst()).setPositive(false);
                                }
                            });
                            CustomerBrowser.this.respondVolley();
                        }
                    });
                    alertDialog.setMessage(getString(R.string.chooseFilters));
                    alertDialog.setTitle(R.string.app_name);
                    this.mAlertDialog = alertDialog.create();
                    this.mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        public void onShow(DialogInterface dialog) {
                            ((AlertDialog) dialog).getButton(-1).setTransformationMethod((TransformationMethod) null);
                            ((AlertDialog) dialog).getButton(-2).setTransformationMethod((TransformationMethod) null);
                            ((AlertDialog) dialog).getButton(-3).setTransformationMethod((TransformationMethod) null);
                        }
                    });
                    this.mAlertDialog.show();
                    return super.onOptionsItemSelected(item);
                case R.id.menuItem_newRouting /*2131231035*/:
                    if (SystemClock.elapsedRealtime() - this.mLastClickTime < 1500) {
                        break;
                    } else {
                        this.mLastClickTime = SystemClock.elapsedRealtime();
                        try {
                            this.progressBar.setVisibility(View.VISIBLE);
                            this.realm.executeTransaction(new Realm.Transaction() {
                                public void execute(Realm realm) {
                                    Log.d("asdfg", "Customers=" + CustomerBrowser.this.customers.size());
                                    realm.where(BrowserCustomer.class).equalTo("myUser.ID", CustomerBrowser.this.gVar.getMyUser().getID()).findAll().deleteAllFromRealm();
                                    realm.where(BrowserCityFilter.class).equalTo("myUser.ID", CustomerBrowser.this.gVar.getMyUser().getID()).findAll().deleteAllFromRealm();
                                    ((BrowserTotalFilter) realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", CustomerBrowser.this.gVar.getMyUser().getID()).findFirst()).setPositive(false);
                                }
                            });
                            this.customers = new ArrayList<>();
                            Log.d("asdfg", "Customers=" + this.customers.size());
                            this.myCusListAdapter = new MyCusListAdapter();
                            this.myCusListAdapter.notifyDataSetChanged();
                            this.request.getUserCustomers(this);
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                        return super.onOptionsItemSelected(item);
                    }
            }
            return false;
        } catch (Exception e2) {
            Log.e("asdfg", e2.getMessage(), e2);
            return false;
        }
    }

    private void CheckEnable() {
        if (this.increment + 1 >= this.pageCount) {
            this.btnNext.setEnabled(false);
        } else {
            this.btnNext.setEnabled(true);
        }
        if (this.increment == 0) {
            this.btnPrev.setEnabled(false);
        } else {
            this.btnPrev.setEnabled(true);
        }
    }

    /* access modifiers changed from: private */
    public void loadList(int number) {
        this.pageCustomers = new ArrayList<>();
        int start = this.NUM_ITEMS_PAGE * number;
        int i = start;
        while (i < this.NUM_ITEMS_PAGE + start && i < this.customers.size()) {
            this.pageCustomers.add(this.customers.get(i));
            i++;
        }
        AppCompatTextView appCompatTextView = this.txvPages;
        appCompatTextView.setText("Page " + (number + 1) + " of " + this.pageCount);
        CheckEnable();
        this.myCusListAdapter = new MyCusListAdapter();
        this.listView.setAdapter(this.myCusListAdapter);
    }

    public void respondVolley() {
        this.customers = new ArrayList<>();
        RealmResults<BrowserCityFilter> findAll = this.realm.where(BrowserCityFilter.class).equalTo("myUser.ID", this.gVar.getMyUser().getID()).findAll();
        if (findAll.size() > 0) {
            for (int i = 0; i < findAll.size(); i++) {
                if (((BrowserTotalFilter) this.realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", this.gVar.getMyUser().getID()).findFirst()).getPositive().booleanValue()) {
                    this.customers.addAll(this.realm.where(BrowserCustomer.class).equalTo("myUser.ID", this.gVar.getMyUser().getID()).equalTo("City", ((BrowserCityFilter) findAll.get(i)).getCity()).greaterThan("TotalBalance", 0.0).findAll());
                } else {
                    this.customers.addAll(this.realm.where(BrowserCustomer.class).equalTo("myUser.ID", this.gVar.getMyUser().getID()).equalTo("City", ((BrowserCityFilter) findAll.get(i)).getCity()).findAll());
                }
            }
        } else if (((BrowserTotalFilter) this.realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", this.gVar.getMyUser().getID()).findFirst()).getPositive().booleanValue()) {
            this.customers.addAll(this.realm.where(BrowserCustomer.class).equalTo("myUser.ID", this.gVar.getMyUser().getID()).greaterThan("TotalBalance", 0.0).findAll());
        } else {
            this.customers.addAll(this.realm.where(BrowserCustomer.class).equalTo("myUser.ID", this.gVar.getMyUser().getID()).findAll());
        }
        Log.d("asdfg", "number of browser customers: " + this.customers.size());
        this.pageCount = (this.customers.size() / this.NUM_ITEMS_PAGE) + (this.customers.size() % this.NUM_ITEMS_PAGE == 0 ? 0 : 1);
        this.increment = 0;
        loadList(this.increment);
        this.progressBar.setVisibility(View.INVISIBLE);
    }

    public void respondCustomerSearch(Customer customer, Address address) {
    }

    public void respondInvoiceType() {
    }

    public void respondPaymentTerm() {
    }

    public void respondCustomerCreate() {
    }

    public void respondVolleyRequestFinished(Integer position, JSONObject jsonObject) {
        respondVolley();
    }

    public void respondDate(Integer position, int year, int month, int day) {
    }

    public void respondCompanySite() {
    }

    public void respondRecentPurchases(ArrayList<InvoiceLineSimple> arrayList) {
    }

    private class MyCusListAdapter extends BaseAdapter {
        private MyCusListAdapter() {
        }

        public int getCount() {
            if (CustomerBrowser.this.pageCustomers == null || CustomerBrowser.this.pageCustomers.size() == 0) {
                return 0;
            }
            return CustomerBrowser.this.pageCustomers.size();
        }

        public Object getItem(int position) {
            return CustomerBrowser.this.pageCustomers.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {

                    holder = new ViewHolder();
                    convertView = CustomerBrowser.this.getLayoutInflater().inflate(R.layout.listview_customer_browser, (ViewGroup) null);
                    convertView.setMinimumHeight(45);
                    holder.Code = (AppCompatTextView) convertView.findViewById(R.id.textView_code);
                    holder.Name = (AppCompatTextView) convertView.findViewById(R.id.textView_customer_name);
                    holder.Total = (AppCompatTextView) convertView.findViewById(R.id.textView_total);
                    holder.TotalAEY = (AppCompatTextView) convertView.findViewById(R.id.textView_total_AEY);
                    holder.TotalASY = (AppCompatTextView) convertView.findViewById(R.id.textView_total_ASY);
                    holder.TotalFEY = (AppCompatTextView) convertView.findViewById(R.id.textView_total_FEY);
                    holder.TotalFSY = (AppCompatTextView) convertView.findViewById(R.id.textView_total_FSY);
                    holder.City = (AppCompatTextView) convertView.findViewById(R.id.textView_city);
                    holder.Coupon = (AppCompatTextView) convertView.findViewById(R.id.textView_coupon);
                    holder.ToVisit = (AppCompatCheckBox) convertView.findViewById(R.id.checkBox_to_visit);
                    holder.ToVisit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            CustomerBrowser.this.realm.executeTransaction(new Realm.Transaction() {
                                public void execute(Realm realm) {
                                    CustomerBrowser.this.pageCustomers.get(holder.ref).setWillVisit(holder.ToVisit.isChecked());
                                    CustomerBrowser.this.myCusListAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    holder.Visited = (AppCompatCheckBox) convertView.findViewById(R.id.checkBox_visited);
                    holder.Visited.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            CustomerBrowser.this.realm.executeTransaction(new Realm.Transaction() {
                                public void execute(Realm realm) {
                                    CustomerBrowser.this.pageCustomers.get(holder.ref).setVisited(holder.Visited.isChecked());
                                    CustomerBrowser.this.myCusListAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    convertView.setTag(holder);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CustomerBrowser.this);
                            alertDialog.setPositiveButton(CustomerBrowser.this.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(CustomerBrowser.this, CustomerReport.class);
                                    Bundle b = new Bundle();
                                    b.putString("CustomerID", ((Customer) CustomerBrowser.this.realm.where(Customer.class).equalTo("Code", CustomerBrowser.this.pageCustomers.get(holder.ref).getCustomerCode()).findFirst()).getID());
                                    b.putString("AddressID", ((Address) CustomerBrowser.this.realm.where(Address.class).equalTo("myCustomer.Code", CustomerBrowser.this.pageCustomers.get(holder.ref).getCustomerCode()).findFirst()).getID());
                                    intent.putExtras(b);
                                    CustomerBrowser.this.startActivity(intent);
                                }
                            });
                            alertDialog.setNegativeButton(CustomerBrowser.this.getString(R.string.no), (DialogInterface.OnClickListener) null);
                            alertDialog.setMessage("Θέλετε να μεταβείτε στην καρτέλα του πελάτη;");
                            alertDialog.setTitle(R.string.app_name);
                            CustomerBrowser.this.mAlertDialog = alertDialog.create();
                            CustomerBrowser.this.mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                public void onShow(DialogInterface dialog) {
                                    ((AlertDialog) dialog).getButton(-1).setTransformationMethod((TransformationMethod) null);
                                    ((AlertDialog) dialog).getButton(-2).setTransformationMethod((TransformationMethod) null);
                                }
                            });
                            CustomerBrowser.this.mAlertDialog.show();
                        }
                    });

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.ref = position;
            holder.Code.setText(CustomerBrowser.this.pageCustomers.get(holder.ref).getCustomerCode());
            holder.Name.setText(CustomerBrowser.this.pageCustomers.get(holder.ref).getCustomerName());
            holder.Total.setText(CustomerBrowser.this.pageCustomers.get(holder.ref).getTotalBalanceText());
            holder.TotalAEY.setText(CustomerBrowser.this.pageCustomers.get(holder.ref).getBalanceAEYText());
            holder.TotalASY.setText(CustomerBrowser.this.pageCustomers.get(holder.ref).getBalanceASYText());
            holder.TotalFEY.setText(CustomerBrowser.this.pageCustomers.get(holder.ref).getBalanceFEYText());
            holder.TotalFSY.setText(CustomerBrowser.this.pageCustomers.get(holder.ref).getBalanceFSYText());
            holder.City.setText(CustomerBrowser.this.pageCustomers.get(holder.ref).getCity());
            holder.Coupon.setText(CustomerBrowser.this.pageCustomers.get(holder.ref).getCouponText());
            holder.ToVisit.setChecked(CustomerBrowser.this.pageCustomers.get(holder.ref).isWillVisit());
            holder.Visited.setChecked(CustomerBrowser.this.pageCustomers.get(position).isVisited());
            return convertView;
        }

        private class ViewHolder {
            AppCompatTextView City;
            AppCompatTextView Code;
            AppCompatTextView Coupon;
            String ID;
            AppCompatTextView Name;
            AppCompatCheckBox ToVisit;
            AppCompatTextView Total;
            AppCompatTextView TotalAEY;
            AppCompatTextView TotalASY;
            AppCompatTextView TotalFEY;
            AppCompatTextView TotalFSY;
            AppCompatCheckBox Visited;
            int ref;

            private ViewHolder() {
            }
        }
    }
}
