package organotiki.mobile.mobilestreet;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.InputFilter;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import io.realm.Realm;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;

/**
 * Created by Thanasis on 20/12/2016.
 */

public class GuaranteeFormFragment extends DialogFragment {
    Button save, cancel;
    TextView titleTextView;
    Communicator comm;
    AppCompatEditText cusCode, cusName, itemCode, userName, manufacturer, model, year1, engineCode, year2, traveledKm, returnCause, observations;
    Realm realm;
    GlobalVar gVar;
    InvoiceLineSimple line;
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
                view = inflater.inflate(R.layout.fragment_guarantee_form, container, false);
            } catch (InflateException e) {
        /* map is already there, just return view as it is */
            }
//            view = inflater.inflate(R.layout.fragment_search_customer_dialog, null);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            titleTextView = (TextView) view.findViewById(R.id.textView_title);
            titleTextView.setText(R.string.guaranteeForm);
            //codeTextView = (TextView) view.findViewById(R.id.textView_code);
            cusName = (AppCompatEditText) view.findViewById(R.id.editText_trade_name);
            cusName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            cusName.setText(gVar.getMyInvoice().getMyCustomer().getName());
            cusName.setFocusable(false);
            cusName.setFocusableInTouchMode(false);
            cusName.setLongClickable(false);
            cusCode = (AppCompatEditText) view.findViewById(R.id.editText_customer_code);
            cusCode.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            cusCode.setText(gVar.getMyInvoice().getMyCustomer().getCode());
            cusCode.setFocusable(false);
            cusCode.setFocusableInTouchMode(false);
            cusCode.setLongClickable(false);
            itemCode = (AppCompatEditText) view.findViewById(R.id.editText_item_code);
            itemCode.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            itemCode.setText(line.getMyItem().getCode());
            itemCode.setFocusable(false);
            itemCode.setFocusableInTouchMode(false);
            itemCode.setLongClickable(false);
            userName = (AppCompatEditText) view.findViewById(R.id.editText_salesman_name);
            userName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            userName.setText(gVar.getMyUser().getFullName());
            userName.setFocusable(false);
            userName.setFocusableInTouchMode(false);
            userName.setLongClickable(false);
            manufacturer = (AppCompatEditText) view.findViewById(R.id.editText_manufacturer);
            manufacturer.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            manufacturer.setText(line.getManufacturer());
            model = (AppCompatEditText) view.findViewById(R.id.editText_model);
            model.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            model.setText(line.getModel());
            year1 = (AppCompatEditText) view.findViewById(R.id.editText_year1);
            year1.setText(String.valueOf(line.getYear1()==null?"":line.getYear1()));
            engineCode = (AppCompatEditText) view.findViewById(R.id.editText_engine_code);
            engineCode.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            engineCode.setText(line.getEngineCode());
            year2 = (AppCompatEditText) view.findViewById(R.id.editText_year2);
            year2.setText(String.valueOf(line.getYear2()==null?"":line.getYear2()));
            traveledKm = (AppCompatEditText) view.findViewById(R.id.editText_traveled_km);
            traveledKm.setText(String.valueOf(line.getKMTraveled()==null?"":line.getKMTraveled()));
            returnCause = (AppCompatEditText) view.findViewById(R.id.editText_return_cause);
            returnCause.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            returnCause.setText(line.getReturnCause());
            observations = (AppCompatEditText) view.findViewById(R.id.editText_observations);
            observations.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            observations.setText(line.getObservations());
            save = (Button) view.findViewById(R.id.button_save);
            save.setTransformationMethod(null);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                line.setManufacturer(String.valueOf(manufacturer.getText()));
                                line.setModel(String.valueOf(model.getText()));
                                line.setYear1(Integer.parseInt(String.valueOf(year1.getText()).equals("")?"0":String.valueOf(year1.getText())));
                                line.setEngineCode(String.valueOf(engineCode.getText()));
                                line.setYear2(Integer.parseInt(String.valueOf(year2.getText()).equals("")?"0":String.valueOf(year2.getText())));
                                line.setKMTraveled(Long.parseLong(String.valueOf(traveledKm.getText()).equals("")?"0":String.valueOf(traveledKm.getText())));
                                line.setReturnCause(String.valueOf(returnCause.getText()));
                                line.setObservations(String.valueOf(observations.getText()));
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    });
                    dismiss();
                }
            });
            cancel = (Button) view.findViewById(R.id.button_back);
            cancel.setTransformationMethod(null);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            mLastClickTime = 0;

            comm = (Communicator) getActivity();

            Log.d("asdfg", "create cusName: " + String.valueOf(cusName.getText()));

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            try {
                if (savedInstanceState.getBoolean("isSearched")) {
                    cusCode.setText(savedInstanceState.getString("cusCode"));
                    cusName.setText(savedInstanceState.getString("cusName"));
                    itemCode.setText(savedInstanceState.getString("itemCode"));
                    userName.setText(savedInstanceState.getString("userName"));
                    manufacturer.setText(savedInstanceState.getString("manufacturer"));
                    model.setText(savedInstanceState.getString("model"));
                    year1.setText(savedInstanceState.getString("year1"));
                    engineCode.setText(savedInstanceState.getString("engineCode"));
                    year2.setText(savedInstanceState.getString("year2"));
                    traveledKm.setText(savedInstanceState.getString("traveledKM"));
                    returnCause.setText(savedInstanceState.getString("returnCause"));
                    observations.setText(savedInstanceState.getString("observations"));
                    Log.d("asdfg", "activity created+ cusName: " + String.valueOf(cusName.getText()));
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
            outState.putString("line", String.valueOf(cusCode.getText()));
            outState.putString("cusCode", String.valueOf(cusCode.getText()));
            outState.putString("cusName", String.valueOf(cusName.getText()));
            outState.putString("itemCode", String.valueOf(itemCode.getText()));
            outState.putString("userName", String.valueOf(userName.getText()));
            outState.putString("manufacturer", String.valueOf(manufacturer.getText()));
            outState.putString("model", String.valueOf(model.getText()));
            outState.putString("year1", String.valueOf(year1.getText()));
            outState.putString("engineCode", String.valueOf(engineCode.getText()));
            outState.putString("year2", String.valueOf(year2.getText()));
            outState.putString("traveledKM", String.valueOf(traveledKm.getText()));
            outState.putString("returnCause", String.valueOf(returnCause.getText()));
            outState.putString("observations", String.valueOf(observations.getText()));
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void setLine(InvoiceLineSimple invoiceLine){
        line= invoiceLine;
    }
//    public void setLine(InvoiceLine invoiceLine) {
//        rline=invoiceLine;
//    }
}
