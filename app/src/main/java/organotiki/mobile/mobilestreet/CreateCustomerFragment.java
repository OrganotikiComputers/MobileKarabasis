package organotiki.mobile.mobilestreet;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class CreateCustomerFragment extends DialogFragment implements View.OnClickListener {
    Button create, cancel;
    TextView titleTextView, codeTextView;
    Communicator comm;
    EditText code, name, tin, street, city, postalcode, phone1, phone2, mobile, email;
    Spinner taxlevel;
    Realm realm;
    GlobalVar gVar;
    Customer customer;
    Address address;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            View view = inflater.inflate(R.layout.fragment_create_customer_dialog, null);

            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            titleTextView = view.findViewById(R.id.textView_Title);
            code = view.findViewById(R.id.editText_Code);
            code.setVisibility(View.GONE);
            code.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            codeTextView = view.findViewById(R.id.textView_Code);
            codeTextView.setVisibility(View.GONE);
            name = view.findViewById(R.id.editText_Name);
            name.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            tin = view.findViewById(R.id.editText_TIN);
            tin.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            street = view.findViewById(R.id.editText_Address);
            street.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            city = view.findViewById(R.id.editText_City);
            city.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            postalcode = view.findViewById(R.id.editText_PostalCode);
            postalcode.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            phone1 = view.findViewById(R.id.editText_Phone);
            phone1.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            phone2 = view.findViewById(R.id.editText_phone2);
            phone2.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            mobile = view.findViewById(R.id.editText_Mobile);
            mobile.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            email = view.findViewById(R.id.editText_email);
            taxlevel = view.findViewById(R.id.spinner_tax_level);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.taxLevels, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the mAdapter to the spinner
            taxlevel.setAdapter(adapter);
            taxlevel.setSelection(0);

            create = view.findViewById(R.id.button_Do);
            create.setTransformationMethod(null);
            create.setOnClickListener(this);
            cancel = view.findViewById(R.id.button_Cancel);
            cancel.setTransformationMethod(null);
            cancel.setOnClickListener(this);

            comm = (Communicator) getActivity();
            if (customer!=null){
//                cusCode.setText(customer.getCode());
                name.setText(customer.getName());
                tin.setText(customer.getTIN());
                street.setText(address.getStreet());
                city.setText(address.getCity());
                postalcode.setText(address.getPostalCode());
                phone1.setText(address.getPhone1());
                phone2.setText(address.getPhone2());
                mobile.setText(address.getMobile());
                email.setText(address.getEmail());
                taxlevel.setSelection(Integer.parseInt(customer.getTaxLevel()));
                titleTextView.setText(R.string.changeCustomerDetails);
                create.setText(R.string.save);
            }else{
                titleTextView.setText(R.string.createCustomer);
                create.setText(R.string.create);
            }


            setCancelable(false);
            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_Do:
                try {
                    if (customer==null) {
                        createCustomer();
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(cancel.getWindowToken(), 0);
                    } else {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                customer.setName(String.valueOf(name.getText()));
                                customer.setTIN(String.valueOf(tin.getText()));
                                customer.setNew(true);
                                customer.setTaxLevel(String.valueOf(taxlevel.getSelectedItemId()));
                                address.setStreet(String.valueOf(street.getText()));
                                address.setCity(String.valueOf(city.getText()));
                                address.setPostalCode(String.valueOf(postalcode.getText()));
                                address.setPhone1(String.valueOf(phone1.getText()));
                                address.setPhone2(String.valueOf(phone2.getText()));
                                address.setMobile(String.valueOf(mobile.getText()));
                                address.setEmail(String.valueOf(email.getText()));

                                comm.respondCustomerSearch(customer,address);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                dismiss();
                break;
            case R.id.button_Cancel:
                try {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(cancel.getWindowToken(), 0);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                dismiss();
                break;
        }
    }

    private void createCustomer() {
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<Customer> results = realm.where(Customer.class).findAll();
                    int myCode = results.size()+1;
                    Customer cus = new Customer(UUID.randomUUID().toString(), String.valueOf(myCode), String.valueOf(name.getText()), String.valueOf(tin.getText()), String.valueOf(taxlevel.getSelectedItem()),true,"");
                    Address adr = new Address(UUID.randomUUID().toString(), cus, cus.getName(), String.valueOf(street.getText()), String.valueOf(city.getText()),String.valueOf(postalcode.getText()), String.valueOf(phone1.getText()),String.valueOf(phone2.getText()),String.valueOf(mobile.getText()),String.valueOf(email.getText()),"","","");
                    Customer customer = realm.copyToRealmOrUpdate(cus);
                    Address myAddress = realm.copyToRealmOrUpdate(adr);
                    gVar.getMyInvoice().setMyCustomer(customer);
                    gVar.getMyInvoice().setMyAddress(myAddress);
                    comm.respondCustomerCreate();
                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void setCustomerDetails(Customer customer,Address address) {
        this.customer = customer;
        this.address = address;
    }
}