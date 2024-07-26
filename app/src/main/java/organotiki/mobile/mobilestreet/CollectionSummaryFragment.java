package organotiki.mobile.mobilestreet;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import androidx.core.app.NavUtils;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.FInvoiceLine;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

/**
 * Created by Thanasis on 23/9/2016.
 */

public class CollectionSummaryFragment  extends DialogFragment {

    TextView txvTotalAEY, txvTotalFEY, txvTotalEY, txvTotalASY, txvTotalFSY, txvTotalSY, txvTotalA, txvTotalF, txvTotal;
    Button submit;
    ListView listView;
    TextView title;
    Realm realm;
    GlobalVar gVar;
    ArrayList<String> messages;
    MyListAdapter myListAdapter;
    AlertDialog mAlertDialog;
    VolleyRequests request;
    DecimalFormat decim = new DecimalFormat("0.00");
    private long mLastClickTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_collections_summary, null);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            messages = new ArrayList<>();

            RealmResults<FInvoiceLine> lines = realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", gVar.getMyFInvoice().getID()).equalTo("myCompany.InAppID", "1").findAll();
            if (gVar.getMyFInvoice().getCash1()+(Double)lines.sum("Value")>0.0){
                messages.add(getString(R.string.karampasisAEY));
                if (gVar.getMyFInvoice().getCash1()>0){
                    messages.add(getString(R.string.cashAnalysis_, gVar.getMyFInvoice().getCash1Text()));
                }
                for (FInvoiceLine line : lines){
                    messages.add(line.getType()==1?getString(R.string.checkAnalysis_,String.valueOf(line.getMyBank().getDescription()),String.valueOf(line.getExDate()),String.valueOf(line.getNumber()),line.getValueText()):getString(R.string.promissoryNoteAnalysis_, String.valueOf(line.getExDate()), line.getValueText()));
                }
            }
            if (gVar.getMyFInvoice().getCash2()>0.0){
                messages.add(getString(R.string.karampasisASY));
                messages.add(getString(R.string.cashAnalysis_, gVar.getMyFInvoice().getCash2Text()));
            }
            lines = realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", gVar.getMyFInvoice().getID()).equalTo("myCompany.InAppID", "2").findAll();
            if (gVar.getMyFInvoice().getCash3()+(Double)lines.sum("Value")>0.0){
                messages.add(getString(R.string.karampasisFEY));
                if (gVar.getMyFInvoice().getCash3()>0){
                    messages.add(getString(R.string.cashAnalysis_, gVar.getMyFInvoice().getCash3Text()));
                }
                for (FInvoiceLine line : lines){
                    messages.add(line.getType()==1?getString(R.string.checkAnalysis_,String.valueOf(line.getMyBank().getDescription()),String.valueOf(line.getExDate()),String.valueOf(line.getNumber()),line.getValueText()):getString(R.string.promissoryNoteAnalysis_, String.valueOf(line.getExDate()), line.getValueText()));
                }
            }
            if (gVar.getMyFInvoice().getCash4()>0.0){
                messages.add(getString(R.string.karampasisFSY));
                messages.add(getString(R.string.cashAnalysis_, gVar.getMyFInvoice().getCash4Text()));
            }
            messages.add(getString(R.string.cashTotal_, decim.format(gVar.getMyFInvoice().getCash1()+gVar.getMyFInvoice().getCash2()+gVar.getMyFInvoice().getCash3()+gVar.getMyFInvoice().getCash4())).replace(",", "."));
            messages.add(getString(R.string.securitiesTotal_, decim.format(realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", gVar.getMyFInvoice().getID()).sum("Value"))).replace(",", "."));

            title = (TextView) view.findViewById(R.id.textView_title);
            title.setText(R.string.app_name);
            txvTotalAEY = (TextView) view.findViewById(R.id.textView_total_AEY);
            txvTotalAEY.setText(decim.format(gVar.getMyFInvoice().getCash1()+(Double)realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", gVar.getMyFInvoice().getID()).equalTo("myCompany.InAppID", "1").sum("Value")).replace(",", "."));
            txvTotalFEY = (TextView) view.findViewById(R.id.textView_total_FEY);
            txvTotalFEY.setText(decim.format(gVar.getMyFInvoice().getCash3()+(Double)realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", gVar.getMyFInvoice().getID()).equalTo("myCompany.InAppID", "2").sum("Value")).replace(",", "."));
            txvTotalEY = (TextView) view.findViewById(R.id.textView_total_EY);
            txvTotalEY.setText(decim.format(gVar.getMyFInvoice().getCash1()+gVar.getMyFInvoice().getCash3()+(Double)realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", gVar.getMyFInvoice().getID()).sum("Value")).replace(",", "."));
            txvTotalASY = (TextView) view.findViewById(R.id.textView_total_ASY);
            txvTotalASY.setText(gVar.getMyFInvoice().getCash2Text());
            txvTotalFSY = (TextView) view.findViewById(R.id.textView_total_FSY);
            txvTotalFSY.setText(gVar.getMyFInvoice().getCash4Text());
            txvTotalSY = (TextView) view.findViewById(R.id.textView_total_SY);
            txvTotalSY.setText(decim.format(gVar.getMyFInvoice().getCash2()+gVar.getMyFInvoice().getCash4()).replace(",", "."));
            txvTotalA = (TextView) view.findViewById(R.id.textView_total_A);
            txvTotalA.setText(decim.format(gVar.getMyFInvoice().getCash1()+gVar.getMyFInvoice().getCash2()+(Double)realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", gVar.getMyFInvoice().getID()).equalTo("myCompany.InAppID", "1").sum("Value")).replace(",", "."));
            txvTotalF = (TextView) view.findViewById(R.id.textView_total_F);
            txvTotalF.setText(decim.format(gVar.getMyFInvoice().getCash3()+gVar.getMyFInvoice().getCash4()+(Double)realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", gVar.getMyFInvoice().getID()).equalTo("myCompany.InAppID", "2").sum("Value")).replace(",", "."));
            txvTotal = (TextView) view.findViewById(R.id.textView_total);
            txvTotal.setText(gVar.getMyFInvoice().getTotalText());
            listView = (ListView) view.findViewById(R.id.listView_sync_messages);
            myListAdapter = new MyListAdapter(getActivity(), messages);
            listView.setAdapter(myListAdapter);
            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            submit = (Button) view.findViewById(R.id.button_close);
            submit.setTransformationMethod(null);
            submit.setText(getString(R.string.submit));
            request = new VolleyRequests();
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    request.getCustomerEmails(getActivity(), gVar.getMyFInvoice().getMyCustomer());
                }
            });

            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    public class  MyListAdapter extends ArrayAdapter<String> {

        private ArrayList<String> messages;

        private class ViewHolder{
            TextView Message;
            int ref;
        }

        public MyListAdapter(Context context, ArrayList<String> messages) {
            super(context, 0, messages);
            this.messages = messages;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            try {
                // Get the data item for this position
                final String message = messages.get(position);

                final ViewHolder viewHolder;
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(R.layout.listview_sync_messages, parent, false);
                    viewHolder.Message = (TextView) convertView.findViewById(R.id.textView_message);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                viewHolder.ref = position;
                viewHolder.Message.setText(message);

            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
            return convertView;
        }
    }
}