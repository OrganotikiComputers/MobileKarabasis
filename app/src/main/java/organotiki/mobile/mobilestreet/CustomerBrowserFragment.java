package organotiki.mobile.mobilestreet;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.core.app.NavUtils;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.BrowserCityFilter;
import organotiki.mobile.mobilestreet.objects.BrowserCustomer;
import organotiki.mobile.mobilestreet.objects.BrowserTotalFilter;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLine;

/**
 * Created by Thanasis on 16/12/2016.
 */

public class CustomerBrowserFragment extends DialogFragment implements View.OnClickListener {
    Button newRouting, back, filters;
    TextView titleTextView;
    Communicator comm;
    //EditText code, name, tin, address, city, postalcode, phone, email;
    ListView listView;
    Realm realm;
    GlobalVar gVar;
    ArrayList<BrowserCustomer> customers;
    boolean isSearched;
    private View view;
    private long mLastClickTime;
    VolleyRequests request;
    MyCusListAdapter myCusListAdapter;
    AlertDialog mAlertDialog;
    CityFilterFragment cityFilterFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            if (view != null) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null)
                    parent.removeView(view);
            }
            try {
                view = inflater.inflate(R.layout.fragment_customer_browser, container, false);
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(),e);
            }
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            request = new VolleyRequests();

            BrowserTotalFilter totalFilter = realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findFirst();
            if (totalFilter==null){
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        BrowserTotalFilter totalFilter = new BrowserTotalFilter(UUID.randomUUID().toString(), gVar.getMyUser(), false);
                        realm.copyToRealm(totalFilter);
                    }
                });
            }

            listView = (ListView) view.findViewById(R.id.listView_customers);

            titleTextView = (TextView) view.findViewById(R.id.textView_title);
            titleTextView.setText(R.string.customerBrowser);
            filters = (Button) view.findViewById(R.id.button_filters);
            filters.setTransformationMethod(null);
            filters.setOnClickListener(this);
            newRouting = (Button) view.findViewById(R.id.button_new_routing);
            newRouting.setTransformationMethod(null);
            newRouting.setOnClickListener(this);
            back = (Button) view.findViewById(R.id.button_back);
            back.setTransformationMethod(null);
            back.setOnClickListener(this);
            mLastClickTime = 0;

            comm = (Communicator) getActivity();

            request.getUserCustomers(getActivity());

            setCancelable(false);
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(android.content.DialogInterface dialog, int keyCode, android.view.KeyEvent event) {

                    if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(back.getWindowToken(), 0);
                        dismiss();
                        return true;
                    } else
                        return false;
                }
            });
            respondVolley();
//            customers = new ArrayList<>();
//            customers.addAll(realm.where(BrowserCustomer.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findAll());
//            Log.d("asdfg", "number of browser customers: " + customers.size());
//            if (customers.size()>0){
//                myCusListAdapter = new MyCusListAdapter();
//                listView.setAdapter(myCusListAdapter);
//            }

            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.button_filters:
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                            getActivity());

                    alertDialog.setPositiveButton(getString(R.string.total), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                                    getActivity());

                            alertDialog.setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findFirst().setPositive(true);
                                        }
                                    });
                                    respondVolley();
                                }
                            });

                            alertDialog.setNegativeButton("Όχι", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findFirst().setPositive(false);
                                        }
                                    });
                                    respondVolley();
                                }
                            });
                            alertDialog.setMessage(getString(R.string.totalFilterQuestion));
                            alertDialog.setTitle(R.string.app_name);
                            mAlertDialog = alertDialog.create();
                            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                    positiveButton.setTransformationMethod(null);

                                    Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                                    negativeButton.setTransformationMethod(null);
                                }
                            });

                            mAlertDialog.show();
                        }
                    });

                    alertDialog.setNegativeButton(getString(R.string.city), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentManager loadManager = getFragmentManager();
                            cityFilterFragment = new CityFilterFragment();
                            cityFilterFragment.show(loadManager, "City Filter Fragment");
                        }
                    });

                    alertDialog.setNeutralButton(getString(R.string.clearFilters), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.where(BrowserCityFilter.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findAll().deleteAllFromRealm();
                                    realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findFirst().setPositive(false);
                                }
                            });
                            respondVolley();
//                            customers.addAll(realm.where(BrowserCustomer.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findAll());
//                            Log.d("asdfg", "number of browser customers: " + customers.size());
//                            if (customers.size()>0){
//                                myCusListAdapter = new MyCusListAdapter();
//                                listView.setAdapter(myCusListAdapter);
//                            }
                        }
                    });
                    alertDialog.setMessage(getString(R.string.chooseFilters));
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
                    break;
                case R.id.button_new_routing:
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1500) {
                        break;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    try {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                Log.d("asdfg", "Customers="+customers.size());
                                realm.where(BrowserCustomer.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findAll().deleteAllFromRealm();
                                realm.where(BrowserCityFilter.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findAll().deleteAllFromRealm();
                                realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findFirst().setPositive(false);

                            }
                        });
                        customers = new ArrayList<>();
                        Log.d("asdfg", "Customers="+customers.size());
                        myCusListAdapter = new MyCusListAdapter();
                        myCusListAdapter.notifyDataSetChanged();
                        request.getUserCustomers(getActivity());
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    break;
                case R.id.button_back:
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(back.getWindowToken(), 0);
                    dismiss();
                    break;
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void respondVolley(){
        customers = new ArrayList<>();
        RealmResults<BrowserCityFilter> cities = realm.where(BrowserCityFilter.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findAll();
        if (cities.size()>0) {
            for (int i =0;i<cities.size();i++){
                if (realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findFirst().getPositive()){
                    customers.addAll(realm.where(BrowserCustomer.class).equalTo("myUser.ID", gVar.getMyUser().getID()).equalTo("City", cities.get(i).getCity()).greaterThan("TotalBalance", 0.0).findAll());
                }else{
                    customers.addAll(realm.where(BrowserCustomer.class).equalTo("myUser.ID", gVar.getMyUser().getID()).equalTo("City", cities.get(i).getCity()).findAll());
                }
            }
        } else {
            if (realm.where(BrowserTotalFilter.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findFirst().getPositive()){
                customers.addAll(realm.where(BrowserCustomer.class).equalTo("myUser.ID", gVar.getMyUser().getID()).greaterThan("TotalBalance", 0.0).findAll());
            }else{
                customers.addAll(realm.where(BrowserCustomer.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findAll());
            }
        }
        Log.d("asdfg", "number of browser customers: " + customers.size());

            myCusListAdapter = new MyCusListAdapter();
            listView.setAdapter(myCusListAdapter);

    }

    private class MyCusListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (customers != null && customers.size() != 0) {
                return customers.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return customers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            try {
                final MyCusListAdapter.ViewHolder holder;
                if (convertView == null) {
                    holder = new MyCusListAdapter.ViewHolder();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    convertView = inflater.inflate(R.layout.listview_customer_browser, null);
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
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    customers.get(holder.ref).setWillVisit(holder.ToVisit.isChecked());
                                    //Log.d("asdfg", "Customer City:"+customers.get(holder.ref).getCustomerName()+", To Visit: "+customers.get(holder.ref).isWillVisit());
                                    myCusListAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    holder.Visited = (AppCompatCheckBox) convertView.findViewById(R.id.checkBox_visited);
                    holder.Visited.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    customers.get(holder.ref).setVisited(holder.Visited.isChecked());
                                    myCusListAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    convertView.setTag(holder);
                } else {
                    holder = (MyCusListAdapter.ViewHolder) convertView.getTag();
                }

                //Address adr = realm.where(Address.class).equalTo("myCustomer.Code", customers.get(position).getCode()).findFirst();

                holder.ref = position;
                //holder.ID = customers.get(position).getID();
                holder.Code.setText(customers.get(holder.ref).getCustomerCode());
                holder.Name.setText(customers.get(holder.ref).getCustomerName());
                holder.Total.setText(customers.get(holder.ref).getTotalBalanceText());
                holder.TotalAEY.setText(customers.get(holder.ref).getBalanceAEYText());
                holder.TotalASY.setText(customers.get(holder.ref).getBalanceASYText());
                holder.TotalFEY.setText(customers.get(holder.ref).getBalanceFEYText());
                holder.TotalFSY.setText(customers.get(holder.ref).getBalanceFSYText());
                //Log.d("asdfg", "City:"+customers.get(holder.ref).getCity() );
                holder.City.setText(customers.get(holder.ref).getCity());
                holder.Coupon.setText(customers.get(holder.ref).getCouponText());
                holder.ToVisit.setChecked(customers.get(holder.ref).isWillVisit());
                holder.Visited.setChecked(customers.get(position).isVisited());
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }

            return convertView;
        }

        private class ViewHolder {
            String ID;
            AppCompatTextView Code;
            AppCompatTextView Name;
            AppCompatTextView Total;
            AppCompatTextView TotalAEY;
            AppCompatTextView TotalASY;
            AppCompatTextView TotalFEY;
            AppCompatTextView TotalFSY;
            AppCompatTextView City;
            AppCompatTextView Coupon;
            AppCompatCheckBox ToVisit;
            AppCompatCheckBox Visited;
            int ref;
        }
    }
}