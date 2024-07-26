package organotiki.mobile.mobilestreet;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.realm.Realm;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

/**
 * Created by Thanasis on 5/12/2016.
 */

public class FInvoiceNotesFragment extends DialogFragment implements View.OnClickListener {

    Button cancel, save;
    Integer parent;
    Communicator comm;
    EditText edtNotes;
    boolean readOnly=false;
    Realm realm;
    GlobalVar gVar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            View view = inflater.inflate(R.layout.fragment_item_details, null);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            TextView txvTitle = (TextView) view.findViewById(R.id.textView_title);
            txvTitle.setText(R.string.invoiceNotes);


            edtNotes = (EditText) view.findViewById(R.id.editText_company);
            String notes = "";
            switch (parent){
                case 1:
                    notes = gVar.getMyFInvoice().getNotes1();
                    break;
                case 2:
                    notes = gVar.getMyFInvoice().getNotes2();
                    break;
                case 3:
                    notes = gVar.getMyFInvoice().getNotes3();
                    break;
                case 4:
                    notes = gVar.getMyFInvoice().getNotes4();
                    break;
            }
            edtNotes.setText(notes);
//            edtCompanyItem = (EditText) view.findViewById(R.id.editText_item);
//            edtCompanyItem.setText(line==null?rline.getCompanyItem():line.getCompanyItem());
            save = (Button) view.findViewById(R.id.button_save);
            save.setTransformationMethod(null);
            save.setOnClickListener(this);
            cancel = (Button) view.findViewById(R.id.button_back);
            cancel.setTransformationMethod(null);
            cancel.setOnClickListener(this);
            if(readOnly) {
                edtNotes.setKeyListener(null);
//                edtCompanyItem.setKeyListener(null);
                save.setVisibility(View.GONE);
                cancel.setText(R.string.close);
                View v = view.findViewById(R.id.view_filler);
                v.setVisibility(View.GONE);
//                TableRow.LayoutParams params = (TableRow.LayoutParams)close.getLayoutParams();
//                params.span = 2;
//                close.setLayoutParams(params);
            }else{
//                edtNotes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//                    @Override
//                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                        boolean handled = false;
//                        if (actionId == EditorInfo.IME_ACTION_SEND) {
//                            try {
//                                realm.executeTransaction(new Realm.Transaction() {
//                                    @Override
//                                    public void execute(Realm realm) {
//                                        if(line==null){
//                                            rline.setNotes(String.valueOf(edtNotes.getText()));
////                                rline.setCompanyItem(String.valueOf(edtCompanyItem.getText()));
//                                        }else{
//                                            line.setNotes(String.valueOf(edtNotes.getText()));
////                                line.setCompanyItem(String.valueOf(edtCompanyItem.getText()));
//                                        }
//                                    }
//                                });
//                                handled = true;
//                                dismiss();
//                            } catch (Exception e) {
//                                Log.e("asdfg", e.getMessage(), e);
//                            }
//                        }
//                        return handled;
//                    }
//                });
            }


            comm = (Communicator) getActivity();

            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
        Window window = getDialog().getWindow();
        //window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setLayout(width,height);
        window.setGravity(Gravity.CENTER);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_save:
                try {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            switch (parent){
                                case 1:
                                    gVar.getMyFInvoice().setNotes1(String.valueOf(edtNotes.getText()));
                                    break;
                                case 2:
                                    gVar.getMyFInvoice().setNotes2(String.valueOf(edtNotes.getText()));
                                    break;
                                case 3:
                                    gVar.getMyFInvoice().setNotes3(String.valueOf(edtNotes.getText()));
                                    break;
                                case 4:
                                    gVar.getMyFInvoice().setNotes4(String.valueOf(edtNotes.getText()));
                                    break;
                            }
                        }
                    });
                    dismiss();
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_back:
                dismiss();
                break;
        }
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }
    public void isReadOnly(boolean bool){
        readOnly=bool;
    }
}