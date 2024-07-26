package organotiki.mobile.mobilestreet;

import android.app.DialogFragment;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.Invoice;
import organotiki.mobile.mobilestreet.objects.PaymentTerm;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class PaymentTermFragment extends DialogFragment /*implements View.OnClickListener, View.OnTouchListener*/ {
    Communicator comm;
    final Rect r = new Rect();
    ListView listView;
    TextView title;
    Realm realm;
    GlobalVar gVar;
    PaymentTerm[] terms;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_simple_dialog, null);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            RealmResults<PaymentTerm> t = realm.where(PaymentTerm.class).findAll();
            Log.d("asdfg", String.valueOf(t.size()));
            terms = t.toArray(new PaymentTerm[t.size()]);
            title = (TextView) view.findViewById(R.id.textView_title);
            title.setText(R.string.paymentTerm);
            listView = (ListView) view.findViewById(R.id.listView_invoice_payment);
            MyListAdapter myListAdapter = new MyListAdapter();
            listView.setAdapter(myListAdapter);
            comm = (Communicator) getActivity();
            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }


    private class MyListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (terms != null && terms.length != 0) {
                return terms.length;
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return terms[position];
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
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    /*Invoice invoice = gVar.getMyInvoice();
                                    invoice.setMyPayment(terms[holder.ref]);*/
                                }
                            });
                            comm.respondPaymentTerm();
                            dismiss();
                        }
                    });
                    holder.Description = (TextView) convertView.findViewById(R.id.textView_invoice_payment);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.ref = position;
                holder.ID = terms[position].getID();
                holder.Description.setText(terms[position].getDescription());
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }

            return convertView;
        }

        private class ViewHolder {
            String ID;
            TextView Description;
            int ref;
        }
    }
}