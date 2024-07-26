package organotiki.mobile.mobilestreet;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmQuery;
import organotiki.mobile.mobilestreet.objects.Company;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLine;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;

import static java.lang.Boolean.getBoolean;

/**
 * Created by Thanasis on 30/11/2016.
 */

public class RecentPurchasesFragment extends DialogFragment {

    TextView title;
    ListView listView;
    Button btnSave, btnCancel;
    Realm realm = MyApplication.getRealm();
    GlobalVar gVar= realm.where(GlobalVar.class).findFirst();;
    DecimalFormat decim = new DecimalFormat("0.00");
    ListViewReturnsItemsAdapter mAdapter;
    ArrayList<InvoiceLineSimple> lines;
    ArrayList<InvoiceLineSimple> oldLines;
    private Communicator comm;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_recent_purchases, null);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            title = (TextView) view.findViewById(R.id.textView_Title);
            title.setText(R.string.recentPurchases);
            listView = (ListView) view.findViewById(R.id.listView_recent_purchases);
            btnCancel = (Button) view.findViewById(R.id.button_back);
            btnCancel.setTransformationMethod(null);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            btnSave = (Button) view.findViewById(R.id.button_save);
            btnSave.setTransformationMethod(null);
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (lines != null) {
                            for (final InvoiceLineSimple line : lines) {
                                if (line.getQuantity() > 0) {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realm.copyToRealmOrUpdate(new InvoiceLine(line.getID(), line.getMyInvoice(), line.getMyItem(), line.getWPrice(), line.getPrice(), line.getQuantity(), line.getNotes(), line.getLastDate(), line.getLastCompany(), line.getLastQuantity(), line.getWrhID(), line.getBraID(), line.getTypeCode(), line.getDosCode(), line.getDocNumber(), line.getOverdue(), line.isEY(), line.isGuarantee(), line.isFromCustomer(), line.getTRNID(), line.getManufacturer(), line.getModel(), line.getYear1(), line.getEngineCode(), line.getYear2(),line.getKMTraveled(), line.getReturnCause(), line.getObservations(),line.getDocID(),line.getDocValue(),line.getChargePapi(), line.isExtraCharge(),line.getExtraChargeValue(), line.getExtraChargeLimit(),line.getDiscountReturnPercent(),line.isAllowed(),line.getMessageAllowed()));
                                        }
                                    });
                                } else {
                                    if (checkIfExists(line, gVar.getMyInvoice().getID())) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                InvoiceLine l = realm.where(InvoiceLine.class).equalTo("myItem.ID", line.getMyItem().getID()).equalTo("myInvoice.ID", gVar.getMyInvoice().getID()).equalTo("LastCompany.InAppID", line.getLastCompany().getInAppID()).equalTo("DosCode", line.getDosCode()).equalTo("DocNumber", line.getDocNumber()).equalTo("Price", line.getPrice()).findFirst();
                                                l.deleteFromRealm();
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    comm.respondRecentPurchases(lines);
                    dismiss();
                }
            });
            comm = (Communicator) getActivity();

            mAdapter = new ListViewReturnsItemsAdapter(getActivity(),lines);
            listView.setAdapter(mAdapter);


            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    public void setOldLines(ArrayList<InvoiceLineSimple> oldLines) {
        this.oldLines = oldLines;
        Log.d("asdfg", "setOldLines: oldLines.size="+String.valueOf(oldLines.size()));
    }

    void respondRecentPurchases(JSONObject response){
        if (!response.isNull("GetRecentPurchasesResult")) {
            try {
                lines = new ArrayList<>();
                lines.addAll(oldLines);
                final JSONArray array = response.getJSONArray("GetRecentPurchasesResult");
                int length = array.length();
                for (int i=0;i<length;i++){
                    JSONObject o = (JSONObject) array.get(i);
                    Log.d("asdfg", String.valueOf(o));
                    if (!checkIfExists(o.getString("LastCompany"),o.getString("DosCode"), o.getString("DocNumber"), o.getDouble("Price"),o.getString("TRNID"))) {
                        InvoiceLineSimple lineSimple = new InvoiceLineSimple(String.valueOf(UUID.randomUUID()), gVar.getMyInvoice(), oldLines.get(0).getMyItem(), o.getDouble("WPrice"), o.getDouble("Price"), 0.0, "", o.getString("LastDate"), realm.where(Company.class).equalTo("InAppID", o.getString("LastCompany")).findFirst(), o.getDouble("LastQuantity"), o.getString("WrhID"), o.getString("BraID"), o.getString("TypeCode"), o.getString("DosCode"), o.getString("DocNumber"), o.getInt("Overdue"), o.getBoolean("isEY"), false, false, o.getString("TRNID"), "", "", null, "", null, null , "", "",o.getString("DocID"), o.getDouble("DocValue"),o.getDouble("ChargePapi"),o.getBoolean("IsExtraCharge"),o.getDouble("ExtraChargeValue"),o.getDouble("ExtraChargeLimit"),o.getDouble("DiscountReturnPercent"),false,o.getString("ReturnAllowed"));
                        String allowed=lineSimple.getMessageAllowed();
                        String[] separated = allowed.split("#");
                        if(separated[0].equals("0")){
                            lineSimple.setAllowed(false);
                        }
                        else{
                            lineSimple.setAllowed(true);
                        }
                        lineSimple.setMessageAllowed(separated[1]);
                        lines.add(lineSimple);
                    }
                }
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
        }
    }

    public boolean checkIfExists(String CompanyInAppID, String DosCode, String DocNumber, Double Price, String TRNID) {

        Log.d("asdfg", DosCode+DocNumber+" linesCount = "+lines.size());
        for ( InvoiceLineSimple line: lines) {
            if (line.getLastCompany().getInAppID().equals(CompanyInAppID) && line.getDosCode().equals(DosCode) && line.getDocNumber().equals(DocNumber) && line.getPrice().equals(Price) && line.getTRNID().equals(TRNID)) {
                Log.d("asdfg", "true");
                return true;
            }
        }
        Log.d("asdfg", "false");
        return false;
    }

    public boolean checkIfExists(String CompanyInAppID, String TRNID) {

        Log.d("asdfg", "TRNID"+TRNID+" linesCount = "+lines.size());
        for ( InvoiceLineSimple line: lines) {
            if (line.getLastCompany().getInAppID().equals(CompanyInAppID) && line.getTRNID().equals(TRNID)) {
                Log.d("asdfg", "true");
                return true;
            }
        }
        Log.d("asdfg", "false");
        return false;
    }

    public boolean checkIfExists(InvoiceLineSimple line, String invoiceID) {

        RealmQuery<InvoiceLine> query = realm.where(InvoiceLine.class)
                .equalTo("myItem.ID", line.getMyItem().getID())
                .equalTo("myInvoice.ID", invoiceID)
                .equalTo("LastCompany.InAppID", line.getLastCompany().getInAppID())
                .equalTo("DosCode", line.getDosCode())
                .equalTo("DocNumber", line.getDocNumber())
                .equalTo("Price", line.getPrice())
                .equalTo("TRNID", line.getTRNID());

        return query.count() != 0;
    }
}