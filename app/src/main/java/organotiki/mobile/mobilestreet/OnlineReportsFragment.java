package organotiki.mobile.mobilestreet;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.OnlineReportType;

/**
 * Created by Thanasis on 13/6/2016.
 */
public class OnlineReportsFragment extends DialogFragment {

    TextView title;
    Realm realm;
    GlobalVar gVar;
    ListView listView;
    VolleyRequests request;
    ArrayList<OnlineReportType> list;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_simple_dialog, null);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            title = (TextView) view.findViewById(R.id.textView_title);
            title.setText(R.string.onlineReports);
            listView = (ListView) view.findViewById(R.id.listView_invoice_payment);

            request  = new VolleyRequests();
            request.sendRequest(getActivity(),"SenService/GetSenOnlineReportType", "");
            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    private class MyListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (list != null && list.size() != 0) {
                return list.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            try {
                final ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    convertView = inflater.inflate(R.layout.list_view_simple, null);
                    convertView.setClickable(true);
                    convertView.setFocusable(true);

                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (holder.FromDate || holder.ToDate || holder.Customer){
                                    Intent intent = new Intent(getActivity(), OnlineReports.class);
                                    intent.putExtra("ReportID", holder.ID);
                                    startActivity(intent);
                                }else{
                                    Intent intent = new Intent(getActivity(), MyWebView.class);
                                    intent.putExtra("ReportID", holder.ID);
                                    startActivity(intent);
                                }

                                dismiss();
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    });
                    holder.Description = (TextView) convertView.findViewById(R.id.textView_invoice_payment);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.ref = position;
                holder.ID = list.get(position).getID();
                holder.Description.setText(list.get(position).getDescription());
                holder.FromDate = list.get(position).getFromDate();
                holder.ToDate = list.get(position).getToDate();
                holder.Customer = list.get(position).getCustomer();
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }

            return convertView;
        }

        private class ViewHolder {
            String ID;
            TextView Description;
            Boolean FromDate;
            Boolean ToDate;
            Boolean Customer;
            int ref;
        }
    }

    public void VolleyRequestCompleted(){
        try {
            RealmResults<OnlineReportType> rr = realm.where(OnlineReportType.class).findAll();
            list = new ArrayList<>();
            for (OnlineReportType type : rr){
                list.add(type);
            }
            MyListAdapter myListAdapter = new MyListAdapter();
            listView.setAdapter(myListAdapter);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
}