package organotiki.mobile.mobilestreet;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import com.astuetz.PagerSlidingTabStrip;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import java.util.ArrayList;
import java.util.Iterator;

import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.CustomerDetailTab;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;

public class AdditionalCustomerDetails extends AppCompatActivity implements Communicator {
    ArrayList<String> Titles;
    Address address;
    Customer customer;
    boolean customerIsUpdated = true;
    GlobalVar gVar;
    ArrayList<CustomerDetailTab> lines;
    ListView listView;
    ViewPager pager;
    MyPagerAdapter pagerAdapter;
    Realm realm;
    VolleyRequests request;
    Fragment requestingFragment;
    ArrayList<Fragment> tabFragments = new ArrayList<>();
    PagerSlidingTabStrip tabs;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView((int) R.layout.activity_additional_customer_details);
            setSupportActionBar((Toolbar) findViewById(R.id.additionalCustomerDetailsBar));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            String cid = getIntent().getStringExtra("CustomerID");
            String aid = getIntent().getStringExtra("AddressID");


            this.realm = MyApplication.getRealm();
            this.gVar = (GlobalVar) this.realm.where(GlobalVar.class).findFirst();
            this.customer = (Customer) this.realm.where(Customer.class).equalTo("ID", cid).findFirst();
            this.address = (Address) this.realm.where(Address.class).equalTo("ID", aid).findFirst();
            this.pager = (ViewPager) findViewById(R.id.pager);
            this.tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
            this.lines = new ArrayList<>();
            this.Titles = new ArrayList<>();
            this.tabFragments = new ArrayList<>();
            this.pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
            this.pager.setAdapter(this.pagerAdapter);
            this.tabs.setViewPager(this.pager);
            this.tabs.setTextColor(Color.parseColor("#FFFFFFFF"));
            this.tabs.setTextSize(18);
            this.tabs.setAllCaps(false);
            if (this.realm.where(CustomerDetailTab.class).findAll().size() == 0) {
                this.request = new VolleyRequests();
                this.request.getCustomerDetails(this, "SenService/GetSenCustomerDetail", this.customer.getID());
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public Fragment getRequestingFragment() {
        return this.requestingFragment;
    }

    public void setRequestingFragment(Fragment requestingFragment2) {
        this.requestingFragment = requestingFragment2;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(Customer customer2) {
        this.customer = customer2;
    }

    public Address getAddress() {
        return this.address;
    }

    public void setAddress(Address address2) {
        this.address = address2;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            if (item.getItemId() != 16908332) {
                return true;
            }
            finish();
            return true;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        this.realm.close();
    }

    public void respondCustomerSearch(Customer customer2, Address address2) {
    }

    public void respondInvoiceType() {
    }

    public void respondPaymentTerm() {
    }

    public void respondCustomerCreate() {
    }

    

    public void respondVolleyRequestFinished(Integer position, JSONObject jsonObject)
    {

        switch (position) {
            case 0:
                Log.e("Test","0");
                if ((this.requestingFragment instanceof AdditionalCusDetailsBarChartFragment)) {
                    ((AdditionalCusDetailsBarChartFragment)this.requestingFragment).setEmails(jsonObject);
                } else if ((this.requestingFragment instanceof AdditionalCusDetailsBarChartPlusHorizontalFragment)) {
                    ((AdditionalCusDetailsBarChartPlusHorizontalFragment)this.requestingFragment).setEmails(jsonObject);
                }
            case 1:
                Log.e("Test","1");
                RealmResults<CustomerDetailTab> rr = this.realm.where(CustomerDetailTab.class).findAll();
                this.lines = new ArrayList<>();
                this.lines.addAll(rr);
                this.Titles = new ArrayList<>();
                Iterator it = this.lines.iterator();
                int pos=0;
                try{
                    while (it.hasNext()) {
                        CustomerDetailTab detailTab = (CustomerDetailTab) it.next();
                        StringBuilder sb = new StringBuilder();
                        sb.append("Tab:");
                        sb.append(this.lines.indexOf(detailTab));
                        sb.append(", Title:");
                        sb.append(detailTab.getTitle());
                        sb.append(", Orientation:");
                        sb.append(detailTab.getOrientation());

                        this.Titles.add(detailTab.getTitle());
                        String orientation = detailTab.getOrientation();
                        char c = 65535;
                        int hashCode = orientation.hashCode();

                        if (hashCode != 72) {
                            if (hashCode != 86) {
                                if (hashCode != 2113) {
                                    if (hashCode == 2032012 && orientation.equals("BC+V")) {
                                        c = 3;
                                    }
                                } else if (orientation.equals("BC")) {
                                    c = 2;
                                }
                            } else if (orientation.equals("V")) {
                                c = 0;
                            }
                        } else if (orientation.equals("H")) {
                            c = 1;
                        }

                        if (c == 0) {
                            AdditionalCustomerDetails.this.tabFragments.add(AdditionalCusDetailsVerticalFragment.newInstance(c, AdditionalCustomerDetails.this.lines.get(pos).getTitle()));
                        } else if (c == 1) {
                            AdditionalCustomerDetails.this.tabFragments.add(AdditionalCusDetailsHorizontalFragment.newInstance(c, AdditionalCustomerDetails.this.lines.get(pos).getTitle()));
                        } else if (c == 2) {
                            AdditionalCustomerDetails.this.tabFragments.add(AdditionalCusDetailsBarChartFragment.newInstance(c, AdditionalCustomerDetails.this.lines.get(pos).getTitle()));
                        } else if (c == 3) {
                            AdditionalCustomerDetails.this.tabFragments.add(AdditionalCusDetailsBarChartPlusHorizontalFragment.newInstance(c, AdditionalCustomerDetails.this.lines.get(pos).getTitle()));
                        }
                        pos++;
                    }
                    this.pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
                    this.pager.setAdapter(this.pagerAdapter);
                    this.tabs.setViewPager(this.pager);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage(), e);
                }
            case 2:
                Log.e("TEST9999","2");
                Log.d("asdfg", jsonObject.toString());
                boolean bool = this.requestingFragment instanceof AdditionalCusDetailsBarChartFragment;
                if (bool)
                {
                    try {
                        if ((jsonObject.length() != 0) || (jsonObject.getString("SentEmailWithImageResult").equals("null")) || (jsonObject.getString("SentEmailWithImageResult") == null) || (jsonObject.isNull("SentEmailWithImageResult"))) {
                            if (jsonObject.getBoolean("SentEmailWithImageResult")) {
                                ((AdditionalCusDetailsBarChartFragment)this.requestingFragment).mAlertDialog.dismiss();
                            } else {
                                ((AdditionalCusDetailsBarChartFragment)this.requestingFragment).positiveButton.setEnabled(true);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        if (((this.requestingFragment instanceof AdditionalCusDetailsBarChartPlusHorizontalFragment)) && ((jsonObject.length() != 0) || (jsonObject.getString("SentEmailWithImageResult").equals("null")) || (jsonObject.getString("SentEmailWithImageResult") == null) || (jsonObject.isNull("SentEmailWithImageResult")))) {
                            try {
                                if (jsonObject.getBoolean("SentEmailWithImageResult")) {
                                    ((AdditionalCusDetailsBarChartPlusHorizontalFragment)this.requestingFragment).mAlertDialog.dismiss();
                                } else {
                                    ((AdditionalCusDetailsBarChartPlusHorizontalFragment)this.requestingFragment).positiveButton.setEnabled(true);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        }


    }



    public void respondDate(Integer position, int year, int month, int day) {
    }

    public void respondCompanySite() {
    }

    public void respondRecentPurchases(ArrayList<InvoiceLineSimple> arrayList) {
    }

public class MyPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<String> TITLES = AdditionalCustomerDetails.this.Titles;

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public CharSequence getPageTitle(int position) {
        return this.TITLES.get(position);
    }

    public int getCount() {
        return this.TITLES.size();
    }

    public Fragment getItem(int position) {
        if (AdditionalCustomerDetails.this.tabFragments.get(position) == null) {
            String orientation = AdditionalCustomerDetails.this.lines.get(position).getOrientation();
            char c = 65535;
            int hashCode = orientation.hashCode();
            Log.e("hashcode",String.valueOf(hashCode));
            if (hashCode != 72) {
                if (hashCode != 86) {
                    if (hashCode != 2113) {
                        if (hashCode == 2032012 && orientation.equals("BC+V")) {
                            c = 3;
                        }
                    } else if (orientation.equals("BC")) {
                        c = 2;
                    }
                } else if (orientation.equals("V")) {
                    c = 0;
                }
            } else if (orientation.equals("H")) {
                c = 1;
            }
            if (c == 0) {
                AdditionalCustomerDetails.this.tabFragments.add(AdditionalCusDetailsVerticalFragment.newInstance(position, AdditionalCustomerDetails.this.lines.get(position).getTitle()));
            } else if (c == 1) {
                AdditionalCustomerDetails.this.tabFragments.add(AdditionalCusDetailsHorizontalFragment.newInstance(position, AdditionalCustomerDetails.this.lines.get(position).getTitle()));
            } else if (c == 2) {
                AdditionalCustomerDetails.this.tabFragments.add(AdditionalCusDetailsBarChartFragment.newInstance(position, AdditionalCustomerDetails.this.lines.get(position).getTitle()));
            } else if (c == 3) {
                AdditionalCustomerDetails.this.tabFragments.add(AdditionalCusDetailsBarChartPlusHorizontalFragment.newInstance(position, AdditionalCustomerDetails.this.lines.get(position).getTitle()));
            }
        }
        return AdditionalCustomerDetails.this.tabFragments.get(position);
    }
}
}
