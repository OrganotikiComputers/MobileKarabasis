package organotiki.mobile.mobilestreet;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import io.realm.Realm;
import java.util.ArrayList;
import organotiki.mobile.mobilestreet.objects.CustomerDetail;
import organotiki.mobile.mobilestreet.objects.CustomerDetailTab;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

public class AdditionalCusDetailsVerticalFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private static final String ARG_TITLE = "title";
    ArrayList<CustomerDetail> details;
    GlobalVar gVar;
    ListView listView;
    private int position;
    Realm realm;
    private String title;
    View view;


    public static AdditionalCusDetailsVerticalFragment newInstance(int position2, String Title) {
        AdditionalCusDetailsVerticalFragment f = new AdditionalCusDetailsVerticalFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position2);
        b.putString(ARG_TITLE, Title);
        Log.e("TEST9999",String.valueOf(position2)+" "+Title);
        f.setArguments(b);
        return f;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.position = getArguments().getInt(ARG_POSITION);
        this.title = getArguments().getString(ARG_TITLE);
        Log.d("asdfg", "Position: " + this.position);
        Log.d("asdfg", "Title: " + this.title);
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            this.view = inflater.inflate(R.layout.fragment_additional_cus_details_vertical_tab, (ViewGroup) null);
            setRetainInstance(true);
            Log.d("asdfg", "Position: " + this.position);
            Log.d("asdfg", "Title: " + this.title);
            this.realm = MyApplication.getRealm();
            this.gVar = (GlobalVar) this.realm.where(GlobalVar.class).findFirst();
            this.details = new ArrayList<>();
            this.details.addAll(((CustomerDetailTab) this.realm.where(CustomerDetailTab.class).equalTo("Title", this.title).findFirst()).getDetails());
            this.listView = (ListView) this.view.findViewById(R.id.listView_additional_customer_details);
            this.listView.setAdapter(new MyListAdapter());
            return this.view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    private class MyListAdapter extends BaseAdapter {
        private MyListAdapter() {
        }

        public int getCount() {
            if (AdditionalCusDetailsVerticalFragment.this.details == null || AdditionalCusDetailsVerticalFragment.this.details.size() == 0) {
                return 0;
            }
            return AdditionalCusDetailsVerticalFragment.this.details.size();
        }

        public Object getItem(int position) {
            return AdditionalCusDetailsVerticalFragment.this.details.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            try{
                ViewHolder holder=new ViewHolder();
                if (convertView == null) {
                    try {
                        holder = new ViewHolder();
                        convertView = AdditionalCusDetailsVerticalFragment.this.getLayoutInflater().inflate(R.layout.listview_additional_customer_details, (ViewGroup) null);
                        holder.Header = (TextView) convertView.findViewById(R.id.description);
                        holder.Value = (TextView) convertView.findViewById(R.id.value);
                        convertView.setTag(holder);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.ref = position;
                holder.Header.setText(AdditionalCusDetailsVerticalFragment.this.details.get(position).getHeader());
                holder.Value.setText(AdditionalCusDetailsVerticalFragment.this.details.get(position).getValue());
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
            return convertView;
        }

        private class ViewHolder {
            TextView Header;
            String ID;
            TextView Value;
            int ref;

            private ViewHolder() {
            }
        }
    }
}
