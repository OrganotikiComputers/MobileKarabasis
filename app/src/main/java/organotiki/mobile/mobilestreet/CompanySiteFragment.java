package organotiki.mobile.mobilestreet;

import android.app.DialogFragment;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.Company;
import organotiki.mobile.mobilestreet.objects.CompanySite;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

/**
 * Created by Thanasis on 17/11/2016.
 */

public class CompanySiteFragment extends DialogFragment {

    TextView title;
    Button close;
    AppCompatSpinner company, companySite;
    ArrayList<String> companyList, companySiteList;
    Realm realm;
    GlobalVar gVar;
    DecimalFormat decim = new DecimalFormat("0.00");
    Button btnSave, btnBack;
    CompanySite lastSite;
    Communicator comm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            View view = inflater.inflate(R.layout.fragment_company_site, null);

            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            title = (TextView) view.findViewById(R.id.textView_title);
            title.setText(R.string.companySite);

            RealmResults<Company> companies = realm.where(Company.class).findAll();
            lastSite = gVar.getMyCompanySite();
            Integer bankCount = companies.size();
            companyList = new ArrayList<>();
            comm = (Communicator) getActivity();
//            banklist.add("ΠΕΙΡΑΙΩΣ");
//            banklist.add("ΕΘΝΙΚΗ");
            for (int i = 0; i < bankCount; i++) {
                companyList.add(companies.get(i).getDescription());
            }
            company = (AppCompatSpinner) view.findViewById(R.id.spinner_company);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    R.layout.spinner_collections_item, companyList);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the mAdapter to the spinner
            company.setAdapter(adapter);
            if (lastSite != null) {
                for (int i = 0; i < companyList.size(); i++) {
                    if (companyList.get(i).equals(lastSite.getMyCompany().getDescription())) {
                        company.setSelection(i);
                        break;
                    }
                }
            }
//        company.setSelection(0);
            company.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(final AdapterView<?> adapterView, View view, final int i, long l) {
                    try {
                        SelectCompanySite();
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            companySite = (AppCompatSpinner) view.findViewById(R.id.spinner_companySite);

            btnBack = (Button) view.findViewById(R.id.button_back);
            btnBack.setTransformationMethod(null);
            btnBack.setOnClickListener(new View.OnClickListener() {
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
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            gVar.setMyCompanySite(realm.where(CompanySite.class).equalTo("myCompany.Description", String.valueOf(company.getSelectedItem())).equalTo("Description", String.valueOf(companySite.getSelectedItem())).findFirst());
                            comm.respondCompanySite();
                        }
                    });
                    dismiss();
                }
            });

            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    private void SelectCompanySite() {
        try {

            Log.d("asdfg", String.valueOf(company.getSelectedItem()));
            if (company.getSelectedItem() != null) {
                RealmResults<CompanySite> companySites = realm.where(CompanySite.class).equalTo("myCompany.Description", String.valueOf(company.getSelectedItem())).findAll();
                Integer companySiteCount = companySites.size();
                Log.d("asdfg", String.valueOf(companySites));
                companySiteList = new ArrayList<>();
//            banklist.add("ΠΕΙΡΑΙΩΣ");
//            banklist.add("ΕΘΝΙΚΗ");
                for (int i = 0; i < companySiteCount; i++) {
                    if (companySites.get(i).isMain()) {
                        companySiteList.add(0, companySites.get(i).getDescription());
                    } else {
                        companySiteList.add(companySites.get(i).getDescription());
                    }
                }
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<String> adapterSite = new ArrayAdapter<>(getActivity(),
                        R.layout.spinner_collections_item, companySiteList);
                // Specify the layout to use when the list of choices appears
                adapterSite.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the mAdapter to the spinner
                companySite.setAdapter(adapterSite);

                if (lastSite != null) {
                    for (int i = 0; i < companySiteList.size(); i++) {
                        if (companySiteList.get(i).equals(lastSite.getDescription())) {
                            companySite.setSelection(i);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
}
