package organotiki.mobile.mobilestreet;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.BrowserCityFilter;
import organotiki.mobile.mobilestreet.objects.BrowserCustomer;
import organotiki.mobile.mobilestreet.objects.BrowserTotalFilter;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

/**
 * Created by Thanasis on 6/3/2017.
 */

public class CityFilterFragment extends DialogFragment {

    private View view;
    ListView listView;
    Button cancel, ok;
    Realm realm;
    GlobalVar gVar;
    Communicator comm;
    ArrayList<FilterCity> cities;
    MyCityListAdapter myCityListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            if (view != null) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null)
                    parent.removeView(view);
            }
            try {
                view = inflater.inflate(R.layout.fragment_city_filter, container, false);
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(),e);
            }
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            comm = (Communicator) getActivity();

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

            listView = (ListView) view.findViewById(R.id.listView_cities);

            ok = (Button) view.findViewById(R.id.button_ok);
            ok.setTransformationMethod(null);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        for (int i = 0; i < myCityListAdapter.getCount(); i++){
                            if (cities.get(i).getShow()) {
                                if (realm.where(BrowserCityFilter.class).equalTo("City", cities.get(i).getName()).equalTo("myUser.ID", gVar.getMyUser().getID()).findFirst() == null) {
                                    final int finalI = i;
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            try {
                                                realm.copyToRealmOrUpdate(new BrowserCityFilter(UUID.randomUUID().toString(), gVar.getMyUser(), cities.get(finalI).Name));
                                            } catch (Exception e) {
                                                Log.e("asdfg", e.getMessage(), e);
                                            }
                                        }
                                    });
                                }
                            } else {
                                if (realm.where(BrowserCityFilter.class).equalTo("City", cities.get(i).getName()).equalTo("myUser.ID", gVar.getMyUser().getID()).findFirst() != null) {
                                    final int finalI1 = i;
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            try {
                                                realm.where(BrowserCityFilter.class).equalTo("City", cities.get(finalI1).getName()).equalTo("myUser.ID", gVar.getMyUser().getID()).findFirst().deleteFromRealm();
                                            } catch (Exception e) {
                                                Log.e("asdfg", e.getMessage(), e);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(ok.getWindowToken(), 0);
                        comm.respondVolleyRequestFinished(3, new JSONObject());
                        dismiss();
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
            cancel = (Button) view.findViewById(R.id.button_cancel);
            cancel.setTransformationMethod(null);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(cancel.getWindowToken(), 0);
                    dismiss();
                }
            });



            setCancelable(false);
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(android.content.DialogInterface dialog, int keyCode, android.view.KeyEvent event) {

                    if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(cancel.getWindowToken(), 0);
                        dismiss();
                        return true;
                    } else
                        return false;
                }
            });
            cities = new ArrayList<>();
            /*RealmResults<BrowserCustomer> customersRR = realm.where(BrowserCustomer.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findAll().sort("City").where().distinct("City");*/
            RealmResults<BrowserCustomer> customersRR = realm.where(BrowserCustomer.class).equalTo("myUser.ID", gVar.getMyUser().getID()).findAll().sort("City");
            for (int i=0; i<customersRR.size();i++) {

                cities.add(new FilterCity(customersRR.get(i).getCity(), realm.where(BrowserCityFilter.class).equalTo("City", customersRR.get(i).getCity()).equalTo("myUser.ID", gVar.getMyUser().getID()).findFirst()!=null));
            }
            Log.d("asdfg", "number of cities : " + cities.size());
            if (cities.size()>0){
                myCityListAdapter = new MyCityListAdapter();
                listView.setAdapter(myCityListAdapter);
            }

            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    private class MyCityListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (cities != null && cities.size() != 0) {
                return cities.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return cities.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            try {
                final MyCityListAdapter.ViewHolder holder;
                if (convertView == null) {
                    holder = new MyCityListAdapter.ViewHolder();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    convertView = inflater.inflate(R.layout.listview_city_filter, null);
                    convertView.setMinimumHeight(45);
                    holder.City = (AppCompatTextView) convertView.findViewById(R.id.textView_city);
                    holder.Show = (AppCompatCheckBox) convertView.findViewById(R.id.checkbox_show);
                    holder.Show.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            cities.get(holder.ref).setShow(isChecked);
                        }
                    });
                    convertView.setTag(holder);
                } else {
                    holder = (MyCityListAdapter.ViewHolder) convertView.getTag();
                }

                //Address adr = realm.where(Address.class).equalTo("myCustomer.Code", customers.get(position).getCode()).findFirst();

                holder.ref = position;
                //holder.ID = customers.get(position).getID();
                holder.City.setText(cities.get(holder.ref).getName());
                holder.Show.setChecked(cities.get(holder.ref).getShow());
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }

            return convertView;
        }

        class ViewHolder {
            String ID;
            AppCompatTextView City;
            AppCompatCheckBox Show;
            int ref;
        }
    }

    class FilterCity{
        private String Name;
        private Boolean Show;

        public FilterCity() {
        }

        FilterCity(String name, Boolean show) {
            Name = name;
            Show = show;
        }

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }

        public Boolean getShow() {
            return Show;
        }

        public void setShow(Boolean show) {
            Show = show;
        }
    }
}
