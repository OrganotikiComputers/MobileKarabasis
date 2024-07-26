package organotiki.mobile.mobilestreet;

import android.content.DialogInterface;
import android.app.DialogFragment;
//import android.app.Fragment;
//import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import android.text.InputFilter;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.realm.Case;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class SearchCustomerFragment extends DialogFragment implements View.OnClickListener {
    Button search, cancel;
    TextView titleTextView;
    Communicator comm;
    EditText code, name, tin, address, city, postalcode, phone, email;
    ListView listView;
    Realm realm;
    GlobalVar gVar;
    ArrayList<Customer> customers;
    RealmResults<Customer> rCustomers;
    RealmResults<Address> addresses;
    boolean isSearched;
    private View view;
    private long mLastClickTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            if (view != null) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null)
                    parent.removeView(view);
            }
            try {
                view = inflater.inflate(R.layout.fragment_search_customer_dialog, container, false);
            } catch (InflateException e) {
        /* map is already there, just return view as it is */
            }
//            view = inflater.inflate(R.layout.fragment_search_customer_dialog, null);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            listView = view.findViewById(R.id.listViewCustomers);

            titleTextView = view.findViewById(R.id.textView_Title);
            titleTextView.setText(R.string.searchCustomer);
            //codeTextView = (TextView) view.findViewById(R.id.textView_code);
            code = view.findViewById(R.id.editText_code);
            code.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            name = view.findViewById(R.id.editText_name);
            name.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            tin = view.findViewById(R.id.editText_TIN);
            tin.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            address = view.findViewById(R.id.editText_street);
            address.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            city = view.findViewById(R.id.editText_city);
            city.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            postalcode = view.findViewById(R.id.editText_postalCode);
            postalcode.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            phone = view.findViewById(R.id.editText_phone);
            email = view.findViewById(R.id.editText_email);
            search = view.findViewById(R.id.button_search);
            search.setTransformationMethod(null);
            search.setOnClickListener(this);
            cancel = view.findViewById(R.id.button_back);
            cancel.setTransformationMethod(null);
            cancel.setOnClickListener(this);
            mLastClickTime = 0;

            comm = (Communicator) getActivity();

            Log.d("asdfg", "create cusName: " + String.valueOf(name.getText()));

            setCancelable(false);
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(android.content.DialogInterface dialog, int keyCode, android.view.KeyEvent event) {

                    if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(cancel.getWindowToken(), 0);
                        dismiss();
                        return true; // pretend we've processed it
                    } else
                        return false; // pass on to be processed as normal
                }
            });
            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void onStop () {
        super.onStop();
        /*if (addresses!=null) {
            addresses.removeAllChangeListeners(); // remove all registered listeners
        }*/
        if (rCustomers!=null) {
            rCustomers.removeAllChangeListeners(); // remove all registered listeners
        }
        realm.close();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            try {
                if (savedInstanceState.getBoolean("isSearched")) {
                    code.setText(savedInstanceState.getString("cusCode"));
                    name.setText(savedInstanceState.getString("cusName"));
                    tin.setText(savedInstanceState.getString("itemCode"));
                    address.setText(savedInstanceState.getString("address"));
                    city.setText(savedInstanceState.getString("manufacturer"));
                    postalcode.setText(savedInstanceState.getString("model"));
                    phone.setText(savedInstanceState.getString("phone"));
                    email.setText(savedInstanceState.getString("engineCode"));
                    Log.d("asdfg", "activity created+ cusName: " + String.valueOf(name.getText()));
                    searchCustomer();
                }
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
            outState.putBoolean("isSearched", isSearched);
            outState.putString("cusCode", String.valueOf(code.getText()));
            outState.putString("cusName", String.valueOf(name.getText()));
            outState.putString("itemCode", String.valueOf(tin.getText()));
            outState.putString("address", String.valueOf(address.getText()));
            outState.putString("manufacturer", String.valueOf(city.getText()));
            outState.putString("model", String.valueOf(postalcode.getText()));
            outState.putString("phone", String.valueOf(phone.getText()));
            outState.putString("engineCode", String.valueOf(email.getText()));
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_search:

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500) {
                    break;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                try {
                    searchCustomer();
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_back:
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(cancel.getWindowToken(), 0);
                dismiss();
                break;
        }
    }
//    private RealmChangeListener<RealmResults<Address>> callback = new RealmChangeListener<RealmResults<Address>>() {
//        @Override
//        public void onChange(RealmResults<Address> addresses) {
//            try {
//                Log.d("asdfg", "number of addresses: " + addresses.size()+"not null changeSet");
//                for (Address adr : addresses) {
//                    if (customers.size() < 300) {
//                        Customer cus = realm.where(Customer.class).equalTo("Code", adr.getMyCustomer().getCode()).findFirst();
//                        if (!customers.contains(cus)) {
//                            customers.add(cus);
//                        }
//                    } else {
//                        Toast.makeText(getActivity(), "Τα αποτελέσματα ήταν πάρα πολλά, εμφανίστηκαν τα 300 πρώτα", Toast.LENGTH_SHORT).show();
//                        break;
//                    }
//                }
//                addresses.removeAllChangeListeners(); // remove all registered listeners
//                Log.d("asdfg", "number of customers: " + customers.size());
//                MyCusListAdapter myCusListAdapter = new MyCusListAdapter();
//                listView.setAdapter(myCusListAdapter);
//            } catch (Exception e) {
//                Log.e("asdfg", e.getMessage(), e);
//            }
//    }
//    };

    private void searchCustomer() {
        try {
            if (addresses!=null) {
                if (addresses.isLoaded()) {
                    Log.d("asdfg", "addresses isLoaded: " + addresses.size());
                } else {
                    Log.d("asdfg", "addresses isNotLoaded");
                }
            }
            isSearched = true;
            Log.d("asdfg", "newRouting cusName: " + String.valueOf(name.getText()));
            RealmList<Customer> userCustomers = gVar.getMyUser().getMyCustomers();
            if (userCustomers.size() > 0) {
                customers = new ArrayList<>();
                rCustomers= userCustomers.where()
                        .beginsWith("Code", String.valueOf(code.getText()), Case.INSENSITIVE)
                        .contains("Name", String.valueOf(name.getText()), Case.INSENSITIVE)
                        .beginsWith("TIN", String.valueOf(tin.getText()), Case.INSENSITIVE)
                        .sort("Name")
                        .findAllAsync();
                rCustomers.addChangeListener(new RealmChangeListener<RealmResults<Customer>>() {
                    @Override
                    public void onChange(@NonNull RealmResults<Customer> result) {
                        try {
                            Log.d("asdfg", "number of rCustomers: " + rCustomers.size());
                            customers = new ArrayList<>();
                            if (rCustomers.size() > 300) {
                                customers.addAll(rCustomers.subList(0, 300));
                                Toast.makeText(getActivity(), "Τα αποτελέσματα ήταν πάρα πολλά, εμφανίστηκαν τα 300 πρώτα", Toast.LENGTH_SHORT).show();
                            } else {
                                customers.addAll(rCustomers);
                            }

                            rCustomers.removeAllChangeListeners(); // remove all registered listeners
                            Log.d("asdfg", "number of customers: " + customers.size());
                            MyCusListAdapter myCusListAdapter = new MyCusListAdapter();
                            listView.setAdapter(myCusListAdapter);

                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });
                customers.addAll(gVar.getMyUser().getMyCustomers().subList(0,300));
                /*String[] customerIDs = new String[userCustomers.size()];
                for (int i = 0; i < userCustomers.size(); i++) {
                    customerIDs[i] = userCustomers.get(i).getID();
                }
                addresses = realm.where(Address.class)
                        .in("myCustomer.ID", customerIDs)
                        .beginsWith("myCustomer.Code", String.valueOf(code.getText()), Case.INSENSITIVE)
                        .contains("myCustomer.Name", String.valueOf(name.getText()), Case.INSENSITIVE)
                        .beginsWith("myCustomer.TIN", String.valueOf(tin.getText()), Case.INSENSITIVE)
                        .contains("Street", String.valueOf(address.getText()), Case.INSENSITIVE)
                        .contains("City", String.valueOf(city.getText()), Case.INSENSITIVE)
                        .contains("PostalCode", String.valueOf(postalcode.getText()), Case.INSENSITIVE)
                        .contains("Email", String.valueOf(email.getText()), Case.INSENSITIVE)
                        .beginGroup()
                        .beginsWith("Phone1", String.valueOf(phone.getText())).or()
                        .beginsWith("Phone2", String.valueOf(phone.getText())).or()
                        .beginsWith("Mobile", String.valueOf(phone.getText()))
                        .endGroup()
                        .sort("CustomerName")
                        .findAllAsync();
                //addresses.addChangeListener(callback);

                addresses.addChangeListener( new RealmChangeListener<RealmResults<Address>>() {
                    @Override
                    public void onChange(@NonNull RealmResults<Address> result) {
                        Log.d("asdfg", "number of addresses: " + addresses.size());
                        for (Address adr : addresses) {
                            if (customers.size() < 300) {
                                Customer cus = realm.where(Customer.class).equalTo("Code", adr.getMyCustomer().getCode()).findFirst();
                                if (!customers.contains(cus)) {
                                    customers.add(cus);
                                }
                            } else {
                                Toast.makeText(getActivity(), "Τα αποτελέσματα ήταν πάρα πολλά, εμφανίστηκαν τα 300 πρώτα", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                        addresses.removeAllChangeListeners(); // remove all registered listeners
                        Log.d("asdfg", "number of customers: " + customers.size());
                        MyCusListAdapter myCusListAdapter = new MyCusListAdapter();
                        listView.setAdapter(myCusListAdapter);
                    }
                });*/
            } else {
                Toast.makeText(getActivity(), "Δεν έχουν δηλωθεί πελάτες για το χρήστη " + gVar.getMyUser().getUsername(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
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
                final ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    convertView = inflater.inflate(R.layout.list_view_search_customer, null);
                    convertView.setMinimumHeight(45);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Log.d("asdfg", "customer: "+customers.get(holder.ref)+", address: "+realm.where(Address.class).equalTo("myCustomer.Code",customers.get(holder.ref).getCode()).findFirst());
                                String cusID= customers.get(holder.ref).getID();
                                comm.respondCustomerSearch(realm.where(Customer.class).equalTo("ID",cusID).findFirst(), realm.where(Address.class).equalTo("myCustomer.ID",cusID).findFirst());
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                                dismiss();
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    });
                    holder.Code = convertView.findViewById(R.id.textView_Code);
                    holder.Name = convertView.findViewById(R.id.textView_Name);
                    holder.Tin = convertView.findViewById(R.id.textView_TIN);
                    holder.Address = convertView.findViewById(R.id.textView_Address);
                    holder.City = convertView.findViewById(R.id.textView_City);
                    holder.PostalCode = convertView.findViewById(R.id.textView_PostalCode);
                    holder.Phone = convertView.findViewById(R.id.textView_Phone);
                    holder.Email = convertView.findViewById(R.id.textView_Email);
                    holder.B2BCode = convertView.findViewById(R.id.textView_B2bCode);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                Address adr = realm.where(Address.class).equalTo("myCustomer.Code", customers.get(position).getCode()).findFirst();

                holder.ref = position;
                holder.ID = customers.get(position).getID();
                holder.Code.setText(customers.get(position).getCode());
                holder.Name.setText(customers.get(position).getName());
                holder.Tin.setText(customers.get(position).getTIN());
                holder.Address.setText(adr.getStreet());
                holder.City.setText(adr.getCity());
                holder.PostalCode.setText(adr.getPostalCode());
                holder.Phone.setText(adr.getPhone1() == null ? adr.getPhone2() == null ? adr.getMobile() : adr.getPhone2() : adr.getPhone1());
                holder.Email.setText(adr.getEmail());
                holder.B2BCode.setText(customers.get(position).getB2bCode());

            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }

            return convertView;
        }

        private class ViewHolder {
            String ID;
            TextView Code;
            TextView Name;
            TextView Tin;
            TextView Address;
            TextView City;
            TextView PostalCode;
            TextView Phone;
            TextView Email;
            TextView B2BCode;
            int ref;
        }
    }
}