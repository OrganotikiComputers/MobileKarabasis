package organotiki.mobile.mobilestreet;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Company;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.CustomerDetail;
import organotiki.mobile.mobilestreet.objects.FInvoiceLine;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.Invoice;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;

/**
 * Created by Thanasis on 13/6/2016.
 */

public class Collections extends AppCompatActivity implements Communicator {

    Button split, submit, cancel;
    EditText checks1, checks2;
    private TextView txvReturns1, txvReturns2, txvReturns3, txvReturns4, txvNewBalance1, txvNewBalance2, txvNewBalance3, txvNewBalance4, txvFinalBalance1, txvFinalBalance2, txvFinalBalance3, txvFinalBalance4;
    private TextView txvCustomerCode, txvCustomerName, txvCustomerEmail, txvTotal, txvOverallBalance, txvBalanceFormation, txvBalance1, txvBalance2, txvBalance3, txvBalance4, txvTitle1, txvTitle2, txvTitle3, txvTitle4;
    private EditText edtCash1, edtCash2, edtCash3, edtCash4, edtDocNumber1, edtDocNumber2, edtDocNumber3, edtDocNumber4, edtSplit;
    Realm realm;
    GlobalVar gVar;
    ChecksFragment checksfrag;
    DecimalFormat decim = new DecimalFormat("0.00");
    //    DecimalFormat formatter = new DecimalFormat("#,###,##0.00");
    VolleyRequests request;
    private long mLastClickTime = 0;
    Double ret1 = 0.0, ret2 = 0.0, ret3 = 0.0, ret4 = 0.0;
    CollectionSummaryFragment frag;
    AlertDialog mAlertDialog;
    String[] emailList;
    Button positiveButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_collections);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            Toolbar toolbar = findViewById(R.id.collectionsBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            request = new VolleyRequests();

            ImageButton imbCus = findViewById(R.id.imageButton_search_customer);

            checks1 = findViewById(R.id.button_company1);
            checks1.setFocusable(false);
            checks1.setLongClickable(false);

            checks2 = findViewById(R.id.button_company2);
            checks2.setFocusable(false);
            checks2.setLongClickable(false);

            txvBalance1 = findViewById(R.id.textView_balance1);
            txvBalance2 = findViewById(R.id.textView_balance2);
            txvBalance3 = findViewById(R.id.textView_balance3);
            txvBalance4 = findViewById(R.id.textView_balance4);

            txvReturns1 = findViewById(R.id.textView_returns1);
            txvReturns2 = findViewById(R.id.textView_returns2);
            txvReturns3 = findViewById(R.id.textView_returns3);
            txvReturns4 = findViewById(R.id.textView_returns4);

            txvNewBalance1 = findViewById(R.id.textView_new_balance1);
            txvNewBalance2 = findViewById(R.id.textView_new_balance2);
            txvNewBalance3 = findViewById(R.id.textView_new_balance3);
            txvNewBalance4 = findViewById(R.id.textView_new_balance4);

            txvFinalBalance1 = findViewById(R.id.textView_final_balance1);
            txvFinalBalance2 = findViewById(R.id.textView_final_balance2);
            txvFinalBalance3 = findViewById(R.id.textView_final_balance3);
            txvFinalBalance4 = findViewById(R.id.textView_final_balance4);

            txvTitle1 = findViewById(R.id.textView_balance1_title);
            txvTitle2 = findViewById(R.id.textView_balance2_title);
            txvTitle3 = findViewById(R.id.textView_balance3_title);
            txvTitle4 = findViewById(R.id.textView_balance4_title);




            submit = findViewById(R.id.button_submit);
            submit.setTransformationMethod(null);


            txvCustomerCode = findViewById(R.id.textView_customer_code);

            txvCustomerName = findViewById(R.id.textView_customer_name);

            txvCustomerEmail = findViewById(R.id.textView_customer_email);

            txvTotal = findViewById(R.id.textView_total);
            txvOverallBalance = findViewById(R.id.textView_overall_balance);

            txvBalanceFormation= findViewById(R.id.textView_balance_formation);

            edtCash1 = findViewById(R.id.editText_cash1);
            edtCash2 = findViewById(R.id.editText_cash2);
            edtCash3 = findViewById(R.id.editText_cash3);
            edtCash4 = findViewById(R.id.editText_cash4);

            edtDocNumber1 = findViewById(R.id.editText_doc_number1);
            edtDocNumber2 = findViewById(R.id.editText_doc_number2);
            edtDocNumber3 = findViewById(R.id.editText_doc_number3);
            edtDocNumber4 = findViewById(R.id.editText_doc_number4);

            cancel = findViewById(R.id.button_back);
            cancel.setTransformationMethod(null);

            edtSplit = findViewById(R.id.editText_split_cash);
            split = findViewById(R.id.button_split_cash);
            split.setTransformationMethod(null);
            split.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Split();
                }
            });

            fillCustomer();

            RelativeLayout layoutCus = findViewById(R.id.relativeLayout_cus); // id fetch from xml

            if (gVar.getMyFInvoice().getMyCustomer()!=null){
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            realm.delete(CustomerDetail.class);
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });
                request.getCustomerDetails(Collections.this, "SenService/GetSenCustomerDetail", gVar.getMyFInvoice().getMyCustomer().getID());
            }

            if (gVar.getMyFInvoice().isFinal()) {
                edtCash1.setFocusable(false);
                edtCash1.setFocusableInTouchMode(false);
                edtCash1.setLongClickable(false);
                edtCash2.setFocusable(false);
                edtCash2.setFocusableInTouchMode(false);
                edtCash2.setLongClickable(false);
                edtCash3.setFocusable(false);
                edtCash3.setFocusableInTouchMode(false);
                edtCash3.setLongClickable(false);
                edtCash4.setFocusable(false);
                edtCash4.setFocusableInTouchMode(false);
                edtCash4.setLongClickable(false);
                edtDocNumber1.setFocusable(false);
                edtDocNumber1.setFocusableInTouchMode(false);
                edtDocNumber1.setLongClickable(false);
                edtDocNumber2.setFocusable(false);
                edtDocNumber2.setFocusableInTouchMode(false);
                edtDocNumber2.setLongClickable(false);
                edtDocNumber3.setFocusable(false);
                edtDocNumber3.setFocusableInTouchMode(false);
                edtDocNumber3.setLongClickable(false);
                edtDocNumber4.setFocusable(false);
                edtDocNumber4.setFocusableInTouchMode(false);
                edtDocNumber4.setLongClickable(false);
                edtSplit.setEnabled(false);
                split.setEnabled(false);
                cancel.setText(R.string.back);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            finish();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });
                submit.setText(R.string.sendEmail);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        request.getCustomerEmails(Collections.this, gVar.getMyFInvoice().getMyCustomer());
                    }
                });
                imbCus.setEnabled(!gVar.getMyFInvoice().isFinal());
                imbCus.setImageAlpha(gVar.getMyFInvoice().isFinal() ? 130 : 255);
            } else {
                if (layoutCus != null) {
                    layoutCus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentManager searchmanager = getFragmentManager();
                            SearchCustomerFragment searchfrag = new SearchCustomerFragment();
                            searchfrag.show(searchmanager, "Search Customer Fragment");
                        }
                    });
                }

                if (imbCus != null) {
                    imbCus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentManager searchmanager = getFragmentManager();
                            SearchCustomerFragment searchfrag = new SearchCustomerFragment();
                            searchfrag.show(searchmanager, "Search Customer Fragment");

                        }
                    });
                }

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doExit();
                    }
                });

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            String message = "";

                            message += gVar.getMyFInvoice().getTotal() == 0 ? "Το σύνολο της είσπραξης είναι μηδενικό." : "";
                            message += gVar.getMyFInvoice().getMyCustomer() == null ? message.equals("") ? "Δεν επιλέξατε πελάτη." : "\nΔεν επιλέξατε πελάτη." : "";

                            if (message.equals("")) {
                                FragmentManager manager = getFragmentManager();
                                frag = new CollectionSummaryFragment();
                                frag.show(manager, "Collections Summary Fragment");
                            } else {
                                Toast.makeText(Collections.this, message, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });

                edtCash1.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        try {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(@NonNull Realm realm) {
                                    gVar.getMyFInvoice().setCash1Text(String.valueOf(edtCash1.getText()));
                                    gVar.getMyFInvoice().setTotal();
                                }
                            });
                            setTotal();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });

                edtCash2.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        try {

                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(@NonNull Realm realm) {
                                    gVar.getMyFInvoice().setCash2Text(String.valueOf(edtCash2.getText()));
                                    gVar.getMyFInvoice().setTotal();
                                }
                            });
                            setTotal();
                            checkSY();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });

                edtCash3.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        try {

                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(@NonNull Realm realm) {
                                    gVar.getMyFInvoice().setCash3Text(String.valueOf(edtCash3.getText()));
                                    gVar.getMyFInvoice().setTotal();
                                }
                            });
                            setTotal();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });

                edtCash4.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        try {

                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(@NonNull Realm realm) {
                                    gVar.getMyFInvoice().setCash4Text(String.valueOf(edtCash4.getText()));
                                    gVar.getMyFInvoice().setTotal();
                                }
                            });
                            setTotal();
                            checkSY();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });
            }

            edtDocNumber1.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(final Editable editable) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(@NonNull Realm realm) {
                                try {
                                gVar.getMyFInvoice().setDocNumber1(String.valueOf(editable));
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                            }
                        });
                }
            });

            edtDocNumber2.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(final Editable editable) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(@NonNull Realm realm) {
                            try {
                                gVar.getMyFInvoice().setDocNumber2(String.valueOf(editable));
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    });
                }
            });

            edtDocNumber3.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(final Editable editable) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(@NonNull Realm realm) {
                            try {
                                gVar.getMyFInvoice().setDocNumber3(String.valueOf(editable));
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    });
                }
            });

            edtDocNumber4.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(final Editable editable) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(@NonNull Realm realm) {
                            try {
                                gVar.getMyFInvoice().setDocNumber4(String.valueOf(editable));
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    });
                }
            });
            setTotal();

        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    public void onBackPressed() {
        if (gVar.getMyFInvoice().isFinal()) {
            finish();
        } else {
            doExit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void respondCustomerSearch(final Customer customer, final Address address) {
        try {
            Log.d("asdfg", String.valueOf(customer));
            Log.d("asdfg", String.valueOf(address));
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    gVar.getMyFInvoice().setMyCustomer(customer);
                    gVar.getMyFInvoice().setMyAddress(address);
                }
            });

            fillCustomer();

            request.getCustomerBalance(Collections.this, gVar.getMyFInvoice().getMyCustomer());
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    try {
                        realm.delete(CustomerDetail.class);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
            request.getCustomerDetails(Collections.this, "SenService/GetSenCustomerDetail", gVar.getMyFInvoice().getMyCustomer().getID());
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    public void respondInvoiceType() {

    }

    @Override
    public void respondPaymentTerm() {

    }

    @Override
    public void respondCustomerCreate() {

    }

    @Override
    public void respondVolleyRequestFinished(Integer position, JSONObject jsonObject) {
        try {
            switch (position) {
                case 0:
                    setEmails(jsonObject);
                    break;
                case 1:
                    CustomerDetail diamYpol = realm.where(CustomerDetail.class).equalTo("Header", "ΔΙΑΜΟΡΦΩΣΗ ΥΠΟΛΟΙΠΟΥ").findFirst();
                    if (diamYpol==null){
                        Toast.makeText(Collections.this, "Δεν βρέθηκε διαμόφωση υπολοίπου για τον πελάτη.", Toast.LENGTH_LONG).show();
                    }else {
                        txvBalanceFormation.setText(diamYpol.getValue());
                    }
                    break;
                case 2:
                    Log.d("asdfg", jsonObject.toString());
    //                    isSaved = jsonArray.getInt("SendCustomerDetailsEmailResult") == 1;
                    if (gVar.getMyFInvoice().isFinal()) {
    //                        Toast.makeText(Collections.this, "Το e-mail στάλθηκε επιτυχώς.", Toast.LENGTH_LONG).show();
                        mAlertDialog.dismiss();
                        NavUtils.navigateUpFromSameTask(Collections.this);
                    } else {
                        positiveButton.setEnabled(true);
    //                        Toast.makeText(Collections.this, "Το e-mail δεν στάλθηκε. Προσπαθήστε ξανά.", Toast.LENGTH_LONG).show();
                    }
                    break;
                case 3:
                    try {
                        Log.d("asdfg", String.valueOf(jsonObject));
                        if (!(jsonObject.length() == 0) || jsonObject.getString("GetBalancesResult").equals("null") || jsonObject.getString("GetBalancesResult") == null || jsonObject.isNull("GetBalancesResult")) {
                            final JSONObject jo = jsonObject.getJSONObject("GetBalancesResult");
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(@NonNull Realm realm) {
                                    try {
                                        gVar.getMyFInvoice().setCusBalanceAEY(jo.getDouble("AEY"));
                                        gVar.getMyFInvoice().setCusBalanceASY(jo.getDouble("ASY"));
                                        gVar.getMyFInvoice().setCusBalanceFEY(jo.getDouble("FEY"));
                                        gVar.getMyFInvoice().setCusBalanceFSY(jo.getDouble("FSY"));
                                    } catch (Exception e) {
                                        Log.e("asdfg", e.getMessage(), e);
                                    }
                                }
                            });

                            fillBalances();
                        } else {
                            repeatBalance();
                        }
                    } catch (Exception e) {
                        repeatBalance();
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    break;
                case 4:
                    try {
                        positiveButton.setEnabled(true);
                    }catch (Exception e){
                        Log.d("asdfg",e.getMessage(),e);
                    }
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    public void respondDate(Integer position, int year, int month, int day) {
        checksfrag.respondDate(position, year, month, day);
    }

    @Override
    public void respondCompanySite() {

    }

    @Override
    public void respondRecentPurchases(ArrayList<InvoiceLineSimple> sLines) {

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.collections_menu, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.customerReport:
                try {
                    Intent intent = new Intent(this, CustomerReport.class);
                    if (this.gVar.getMyFInvoice().getMyCustomer() != null) {
                        Bundle b = new Bundle();
                        b.putString("CustomerID", this.gVar.getMyFInvoice().getMyCustomer().getID());
                        b.putString("AddressID", this.gVar.getMyFInvoice().getMyAddress().getID());
                        intent.putExtras(b);
                    }
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                return false;
        }
        return false;
    }



    public void setEmails(JSONObject jsonObject){
        try {
            JSONArray array = jsonObject.getJSONArray("GetCustomerEmailsResult") ;
            int l = array.length();
            emailList = new String[l];
            if (l>0) {
                for (int i=0;i<l; i++){
                    emailList[i] = String.valueOf(array.get(i));
                }
            }
            doEmail();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private void doEmail() {
        try {
//            String message = gVar.getMyFInvoice().getMyAddress().getEmail()== null || gVar.getMyFInvoice().getMyAddress().getEmail().equals("")?"Σε ποιο e-mail θέλετε να σταλεί η είσπραξη;":"Η είσπραξη θα σταλεί στο : "+gVar.getMyFInvoice().getMyAddress().getEmail()+"\nΑν θέλετε να σταλεί και κάπου αλλού προσθέστε το λογαριασμό e-mail.";
            String message = "Σε ποια e-mail θέλετε να σταλθεί η είσπραξη; (μπορείτε να μην βάλετε κανένα)";
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Collections.this);

            LinearLayout layout = new LinearLayout(Collections.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setMinimumWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            layout.setPadding(10, 0, 10, 0);

            final AutoCompleteTextView input1 = new AutoCompleteTextView(Collections.this);
            final AutoCompleteTextView input2 = new AutoCompleteTextView(Collections.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(Collections.this, android.R.layout.simple_list_item_1,emailList);
            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(Collections.this, android.R.layout.simple_list_item_1, emailList);
            input1.setLayoutParams(lp);
            input1.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
//            input1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            input1.setHint(R.string.email);
            input1.setThreshold(0);
            input1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    input1.showDropDown();
                }
            });
            input1.setAdapter(adapter1);
            if (!(gVar.getMyFInvoice().getMyAddress() == null || gVar.getMyFInvoice().getMyAddress().getEmail().equals(""))) {
                input1.setText(gVar.getMyFInvoice().getMyAddress().getEmail());
            }

            Log.d("asdfg", Arrays.toString(emailList));
            input2.setLayoutParams(lp);
            input2.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            input2.setHint(R.string.email);
            input2.setThreshold(0);
            input2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    input2.showDropDown();
                }
            });
            input2.setAdapter(adapter2);
            layout.addView(input1);
            layout.addView(input2);
            alertDialog.setView(layout);

            alertDialog.setPositiveButton("Αποστολή", null);
            alertDialog.setNegativeButton("Άκυρο", null);
//        alertDialog.setNeutralButton("Άκυρο",null);
            alertDialog.setMessage(message);
            alertDialog.setTitle(R.string.app_name);




            mAlertDialog = alertDialog.create();
            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTransformationMethod(null);

                    Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    negativeButton.setTransformationMethod(null);
                }
            });
            mAlertDialog.show();
            positiveButton = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (SystemClock.elapsedRealtime() - mLastClickTime > 5000) {
                            mLastClickTime = SystemClock.elapsedRealtime();

                            if ((isEmailValid(input1.getText()) || String.valueOf(input1.getText()).equals("")) && (isEmailValid(input2.getText())|| String.valueOf(input2.getText()).equals(""))) {
                                Toast.makeText(Collections.this, "Παρακαλώ περιμένετε.", Toast.LENGTH_LONG).show();
                                if (gVar.getMyFInvoice().isFinal()){
                                    String link = "SenService/GetSenReSendEmail?ID="+gVar.getMyFInvoice().getID()+"&Email1="+String.valueOf(input1.getText())+"&Email2="+String.valueOf(input2.getText());
                                    request.sendFInvoiceEmail(Collections.this, link);
                                    positiveButton.setEnabled(false);
                                }else {
                                    request.setFInvoice(Collections.this, gVar.getMyFInvoice().getID(), String.valueOf(input1.getText()), String.valueOf(input2.getText()));
                                    positiveButton.setEnabled(false);
                                }
                            } else {
                                Toast.makeText(Collections.this, "Πρέπει να συμπληρώσετε με σωστή διεύθυνση email ή να αφήσετε κενά τα πεδία.", Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void fillBalances() {
        edtCash1.setText(gVar.getMyFInvoice().getCash1Text());
        edtCash2.setText(gVar.getMyFInvoice().getCash2Text());
        edtCash3.setText(gVar.getMyFInvoice().getCash3Text());
        edtCash4.setText(gVar.getMyFInvoice().getCash4Text());

        edtDocNumber1.setText(gVar.getMyFInvoice().getDocNumber1());
        edtDocNumber2.setText(gVar.getMyFInvoice().getDocNumber2());
        edtDocNumber3.setText(gVar.getMyFInvoice().getDocNumber3());
        edtDocNumber4.setText(gVar.getMyFInvoice().getDocNumber4());

        txvBalance1.setText(gVar.getMyFInvoice().getCusBalanceAEYText());
        txvBalance2.setText(gVar.getMyFInvoice().getCusBalanceASYText());
        txvBalance3.setText(gVar.getMyFInvoice().getCusBalanceFEYText());
        txvBalance4.setText(gVar.getMyFInvoice().getCusBalanceFSYText());
        RealmResults<Invoice> invoices = realm.where(Invoice.class).equalTo("myCustomer.ID", gVar.getMyFInvoice().getMyCustomer().getID()).equalTo("isFinal", true).findAll();
        int l = invoices.size();
        ret1 = 0.0;
        ret2 = 0.0;
        ret3 = 0.0;
        ret4 = 0.0;
        for (int i = 0; i < l; i++) {
            ret1 += invoices.get(i).getTotalAEY();
            ret2 += invoices.get(i).getTotalASY();

            ret3 += invoices.get(i).getTotalFEY();
            ret4 += invoices.get(i).getTotalFSY();

        }
        txvReturns1.setText(decim.format(ret1).replace(",", "."));
        txvReturns2.setText(decim.format(ret2).replace(",", "."));
        txvReturns3.setText(decim.format(ret3).replace(",", "."));
        txvReturns4.setText(decim.format(ret4).replace(",", "."));

        txvNewBalance1.setText(decim.format(gVar.getMyFInvoice().getCusBalanceAEY() - ret1).replace(",", "."));
        txvNewBalance2.setText(decim.format(gVar.getMyFInvoice().getCusBalanceASY() - ret2).replace(",", "."));
        txvNewBalance3.setText(decim.format(gVar.getMyFInvoice().getCusBalanceFEY() - ret3).replace(",", "."));
        txvNewBalance4.setText(decim.format(gVar.getMyFInvoice().getCusBalanceFSY() - ret4).replace(",", "."));

        checkSY();
    }


    public void setTotal() {
        if (gVar.getMyFInvoice().getMyCustomer() != null) {
            checks1.setText(decim.format(realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", gVar.getMyFInvoice().getID()).equalTo("myCompany.InAppID", "1").sum("Value")).replace(",", "."));
            checks2.setText(decim.format(realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", gVar.getMyFInvoice().getID()).equalTo("myCompany.InAppID", "2").sum("Value")).replace(",", "."));

            edtDocNumber1.setEnabled(gVar.getMyFInvoice().getCash1() + Double.parseDouble(String.valueOf(checks1.getText()).equals("")?"0.0":String.valueOf(checks1.getText()))>0);
            edtDocNumber2.setEnabled(gVar.getMyFInvoice().getCash2() >0);
            edtDocNumber3.setEnabled(gVar.getMyFInvoice().getCash3() + Double.parseDouble(String.valueOf(checks2.getText()).equals("")?"0.0":String.valueOf(checks2.getText()))>0);
            edtDocNumber4.setEnabled(gVar.getMyFInvoice().getCash4() >0);

            txvFinalBalance1.setText(decim.format(gVar.getMyFInvoice().getCusBalanceAEY() - ret1 - gVar.getMyFInvoice().getCash1() - Double.parseDouble(String.valueOf(checks1.getText()))).replace(",", "."));
            txvFinalBalance2.setText(decim.format(gVar.getMyFInvoice().getCusBalanceASY() - ret2 - gVar.getMyFInvoice().getCash2()).replace(",", "."));
            txvFinalBalance3.setText(decim.format(gVar.getMyFInvoice().getCusBalanceFEY() - ret3 - gVar.getMyFInvoice().getCash3() - Double.parseDouble(String.valueOf(checks2.getText()))).replace(",", "."));
            txvFinalBalance4.setText(decim.format(gVar.getMyFInvoice().getCusBalanceFSY() - ret4 - gVar.getMyFInvoice().getCash4()).replace(",", "."));

            txvOverallBalance.setText(decim.format(Double.parseDouble(String.valueOf(txvFinalBalance1.getText())) + Double.parseDouble(String.valueOf(txvFinalBalance2.getText())) + Double.parseDouble(String.valueOf(txvFinalBalance3.getText())) + Double.parseDouble(String.valueOf(txvFinalBalance4.getText()))).replace(",", "."));
            txvTotal.setText(gVar.getMyFInvoice().getTotalText());
        }
    }

    private void doExit() {

        if (gVar.getMyFInvoice().getMyCustomer() == null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    gVar.getMyFInvoice().deleteFromRealm();
                }
            });
            NavUtils.navigateUpFromSameTask(Collections.this);
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                    Collections.this);

            alertDialog.setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    NavUtils.navigateUpFromSameTask(Collections.this);
                }
            });

            alertDialog.setNegativeButton("Όχι", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(@NonNull Realm realm) {
                                realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", gVar.getMyFInvoice().getID()).findAll().deleteAllFromRealm();
                                gVar.getMyFInvoice().deleteFromRealm();
                            }
                        });
                        NavUtils.navigateUpFromSameTask(Collections.this);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
//                finish();
                }
            });
            alertDialog.setNeutralButton("Άκυρο", null);
            alertDialog.setMessage("Η είσπραξη δεν έχει ολοκληρωθεί.\nΘες να την αποθηκεύσεις;");
            alertDialog.setTitle(R.string.app_name);
            mAlertDialog = alertDialog.create();
            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTransformationMethod(null);

                    Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    negativeButton.setTransformationMethod(null);

                    Button neutralButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                    neutralButton.setTransformationMethod(null);
                }
            });

            mAlertDialog.show();
        }
    }

    private void repeatBalance() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                    Collections.this);

            alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        request.getCustomerBalance(Collections.this, gVar.getMyFInvoice().getMyCustomer());
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });

            alertDialog.setNegativeButton(getString(R.string.no), null);
            alertDialog.setMessage("Ο Διακομιστής δεν ανταποκρίθηκε.\nΕπαναφόρτωση των υπολοίπων του πελάτη;");
            alertDialog.setTitle(R.string.app_name);
            mAlertDialog = alertDialog.create();
            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTransformationMethod(null);

                    Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    negativeButton.setTransformationMethod(null);
                }
            });

            mAlertDialog.show();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private void fillCustomer() {
        String codetxt;
        String nametxt;
        String emailtxt;

        if (gVar.getMyFInvoice().getMyCustomer() != null) {
//                cus = gVar.getMyFInvoice().getMyCustomer();
            codetxt = getResources().getString(R.string.code) + ": " + gVar.getMyFInvoice().getMyCustomer().getCode();
            nametxt = getResources().getString(R.string.name) + ": " + gVar.getMyFInvoice().getMyCustomer().getName();
            emailtxt = getResources().getString(R.string.email) + ": " + gVar.getMyFInvoice().getMyAddress().getEmail();

            edtCash2.setEnabled(true);
            edtCash2.setFocusable(true);
            edtCash2.setFocusableInTouchMode(true);
            edtCash2.setLongClickable(true);
            edtCash4.setEnabled(true);
            edtCash4.setFocusable(true);
            edtCash4.setFocusableInTouchMode(true);
            edtCash4.setLongClickable(true);
            edtDocNumber1.setEnabled(gVar.getMyFInvoice().getCash1() + Double.parseDouble(String.valueOf(checks1.getText()).equals("")?"0.0":String.valueOf(checks1.getText()))>0);
            edtDocNumber2.setEnabled(gVar.getMyFInvoice().getCash2() >0);
            edtDocNumber3.setEnabled(gVar.getMyFInvoice().getCash3() + Double.parseDouble(String.valueOf(checks2.getText()).equals("")?"0.0":String.valueOf(checks2.getText()))>0);
            edtDocNumber4.setEnabled(gVar.getMyFInvoice().getCash4()>0);
            edtSplit.setEnabled(true);
            split.setEnabled(true);
            txvTitle1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getFragmentManager();
                    FInvoiceNotesFragment frag = new FInvoiceNotesFragment();
                    Log.d("asdfg", String.valueOf(gVar.getMyFInvoice()));
                    frag.setParent(1);
                    frag.show(manager, "Checks Fragment");
                }
            });
            txvTitle2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getFragmentManager();
                    FInvoiceNotesFragment frag = new FInvoiceNotesFragment();
                    Log.d("asdfg", String.valueOf(gVar.getMyFInvoice()));
                    frag.setParent(2);
                    frag.show(manager, "Checks Fragment");
                }
            });
            txvTitle3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getFragmentManager();
                    FInvoiceNotesFragment frag = new FInvoiceNotesFragment();
                    Log.d("asdfg", String.valueOf(gVar.getMyFInvoice()));
                    frag.setParent(3);
                    frag.show(manager, "Checks Fragment");
                }
            });
            txvTitle4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getFragmentManager();
                    FInvoiceNotesFragment frag = new FInvoiceNotesFragment();
                    Log.d("asdfg", String.valueOf(gVar.getMyFInvoice()));
                    frag.setParent(4);
                    frag.show(manager, "Checks Fragment");
                }
            });

            if (gVar.getMyFInvoice().getCusBalanceAEY() == null) {
                request.getCustomerBalance(Collections.this, gVar.getMyFInvoice().getMyCustomer());
            } else {
                fillBalances();
            }
        } else {
            codetxt = getResources().getString(R.string.code) + ": ";
            nametxt = getResources().getString(R.string.name) + ": ";
            emailtxt = getResources().getString(R.string.email) + ": ";
            edtCash1.setFocusable(false);
            edtCash1.setFocusableInTouchMode(false);
            edtCash1.setLongClickable(false);
            edtCash1.setEnabled(false);
            edtCash2.setFocusable(false);
            edtCash2.setFocusableInTouchMode(false);
            edtCash2.setLongClickable(false);
            edtCash2.setEnabled(false);
            edtCash3.setFocusable(false);
            edtCash3.setFocusableInTouchMode(false);
            edtCash3.setLongClickable(false);
            edtCash3.setEnabled(false);
            edtCash4.setFocusable(false);
            edtCash4.setFocusableInTouchMode(false);
            edtCash4.setLongClickable(false);
            edtCash4.setEnabled(false);
            checks1.setEnabled(false);
            checks2.setEnabled(false);
            edtSplit.setEnabled(false);
            split.setEnabled(false);
        }

        txvCustomerCode.setText(codetxt);
        txvCustomerName.setText(nametxt);
        txvCustomerEmail.setText(emailtxt);
    }

    private void checkSY() {
        Double totalASY = Double.parseDouble(String.valueOf(txvFinalBalance2.getText()).equals("") ? "0.0" : String.valueOf(txvFinalBalance2.getText()));
        Double totalFSY = Double.parseDouble(String.valueOf(txvFinalBalance4.getText()).equals("") ? "0.0" : String.valueOf(txvFinalBalance4.getText()));

        if ( totalASY> 0 && totalASY+totalFSY>0) {
            edtCash1.setFocusable(false);
            edtCash1.setFocusableInTouchMode(false);
            edtCash1.setLongClickable(false);
            edtCash1.setEnabled(false);
            edtCash1.setText("0.00");
            checks1.setEnabled(false);
            /*edtCash1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(Collections.this, "Πρέπει πρώτα να καλυφθεί το υπόλοιπο ΣΥ της εταιρείας.", Toast.LENGTH_SHORT).show();
                }
            });
            checks1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(Collections.this, "Πρέπει πρώτα να καλυφθεί το υπόλοιπο ΣΥ της εταιρείας.", Toast.LENGTH_SHORT).show();
                }
            });*/
        } else {
            edtCash1.setFocusable(true);
            edtCash1.setFocusableInTouchMode(true);
            edtCash1.setLongClickable(true);
//            edtCash1.setOnClickListener(null);
            edtCash1.setEnabled(true);
            checks1.setEnabled(true);
            checks1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager manager = getFragmentManager();
                    checksfrag = new ChecksFragment();
                    Log.d("asdfg", String.valueOf(gVar.getMyFInvoice()));
                    checksfrag.setCompany(realm.where(Company.class).equalTo("InAppID", "1").findFirst());
                    checksfrag.show(manager, "Checks Fragment");
                }
            });
        }

        if (totalFSY > 0 && totalASY+totalFSY>0) {
            edtCash3.setFocusable(false);
            edtCash3.setFocusableInTouchMode(false);
            edtCash3.setLongClickable(false);
            edtCash3.setEnabled(false);
            edtCash3.setText("0.00");
            checks2.setEnabled(false);
            /*edtCash3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(Collections.this, "Πρέπει πρώτα να καλυφθεί το υπόλοιπο ΣΥ της εταιρείας.", Toast.LENGTH_SHORT).show();
                }
            });
            checks2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(Collections.this, "Πρέπει πρώτα να καλυφθεί το υπόλοιπο ΣΥ της εταιρείας.", Toast.LENGTH_SHORT).show();
                }
            });*/
        } else {
            edtCash3.setFocusable(true);
            edtCash3.setFocusableInTouchMode(true);
            edtCash3.setLongClickable(true);
//            edtCash3.setOnClickListener(null);
            edtCash3.setEnabled(true);
            checks2.setEnabled(true);
            checks2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager manager = getFragmentManager();
                    checksfrag = new ChecksFragment();
                    Log.d("asdfg", String.valueOf(gVar.getMyFInvoice()));
                    checksfrag.setCompany(realm.where(Company.class).equalTo("InAppID", "2").findFirst());
                    checksfrag.show(manager, "Checks Fragment");
                }
            });
        }
    }

    private void Split(){
        try {
            Double splitCash = Double.parseDouble(String.valueOf(edtSplit.getText()).equals("") ? "0.0" : String.valueOf(edtSplit.getText()));
            Double totalAEY = Double.parseDouble(String.valueOf(txvFinalBalance1.getText()).equals("") ? "0.0" : String.valueOf(txvFinalBalance1.getText()));
            Double cashAEY = Double.parseDouble(String.valueOf(edtCash1.getText()).equals("") ? "0.0" : String.valueOf(edtCash1.getText()));
            Double totalASY = Double.parseDouble(String.valueOf(txvFinalBalance2.getText()).equals("") ? "0.0" : String.valueOf(txvFinalBalance2.getText()));
            Double cashASY = Double.parseDouble(String.valueOf(edtCash2.getText()).equals("") ? "0.0" : String.valueOf(edtCash2.getText()));
            Double totalFEY = Double.parseDouble(String.valueOf(txvFinalBalance3.getText()).equals("") ? "0.0" : String.valueOf(txvFinalBalance3.getText()));
            Double cashFEY = Double.parseDouble(String.valueOf(edtCash3.getText()).equals("") ? "0.0" : String.valueOf(edtCash3.getText()));
            Double totalFSY = Double.parseDouble(String.valueOf(txvFinalBalance4.getText()).equals("") ? "0.0" : String.valueOf(txvFinalBalance4.getText()));
            Double cashFSY = Double.parseDouble(String.valueOf(edtCash4.getText()).equals("") ? "0.0" : String.valueOf(edtCash4.getText()));
            Double totalSY = totalASY+totalFSY;
            Double totalEY = totalAEY+totalFEY;

            while (splitCash>0) {
                Log.d("asdfg", "splitCash="+String.valueOf(splitCash)+",totalEY="+String.valueOf(totalEY)+",totalSY="+String.valueOf(totalSY)+",totalAEY="+String.valueOf(totalAEY)+",cashAEY="+String.valueOf(cashAEY)+",totalASY="+String.valueOf(totalASY)+",cashASY="+String.valueOf(cashASY)+",totalFEY="+String.valueOf(totalFEY)+",cashFEY="+String.valueOf(cashFEY)+",totalFSY="+String.valueOf(totalFSY)+",cashFSY="+String.valueOf(cashFSY));
                if (totalSY > 0) {
                    if ((totalASY > 0 && totalASY < totalFSY) || totalFSY<=0) {
                        Double c = totalASY > totalSY ? totalSY : totalASY;
                        c = c>splitCash?splitCash:c;
                        edtCash2.setText(decim.format(cashASY+c).replace(",", "."));
                        totalASY = Double.parseDouble(String.valueOf(txvFinalBalance2.getText()).equals("") ? "0.0" : String.valueOf(txvFinalBalance2.getText()));
                        cashASY = Double.parseDouble(String.valueOf(edtCash2.getText()).equals("") ? "0.0" : String.valueOf(edtCash2.getText()));
                        totalSY = totalASY+totalFSY;
                        splitCash -= c;
                    }else{
                        Double c = totalFSY > totalSY ? totalSY : totalFSY;
                        c = c>splitCash?splitCash:c;
                        edtCash4.setText(decim.format(cashFSY+c).replace(",", "."));
                        totalFSY = Double.parseDouble(String.valueOf(txvFinalBalance4.getText()).equals("") ? "0.0" : String.valueOf(txvFinalBalance4.getText()));
                        cashFSY = Double.parseDouble(String.valueOf(edtCash4.getText()).equals("") ? "0.0" : String.valueOf(edtCash4.getText()));
                        totalSY = totalASY+totalFSY;
                        splitCash -= c;
                    }
                }else if (totalEY>0){
                    if ((totalAEY > 0 && totalAEY < totalFEY) || totalFEY<=0) {
                        Double c = totalAEY > totalEY ? totalEY : totalAEY;
                        c = c>splitCash?splitCash:c;
                        edtCash1.setText(decim.format(cashAEY+c).replace(",", "."));
                        totalAEY = Double.parseDouble(String.valueOf(txvFinalBalance1.getText()).equals("") ? "0.0" : String.valueOf(txvFinalBalance1.getText()));
                        cashAEY = Double.parseDouble(String.valueOf(edtCash1.getText()).equals("") ? "0.0" : String.valueOf(edtCash1.getText()));
                        totalEY = totalAEY+totalFEY;
                        splitCash -= c;
                    }else{
                        Double c = totalFEY > totalEY ? totalEY : totalFEY;
                        c = c>splitCash?splitCash:c;
                        edtCash3.setText(decim.format(cashFEY+c).replace(",", "."));
                        totalFEY = Double.parseDouble(String.valueOf(txvFinalBalance3.getText()).equals("") ? "0.0" : String.valueOf(txvFinalBalance3.getText()));
                        cashFEY = Double.parseDouble(String.valueOf(edtCash3.getText()).equals("") ? "0.0" : String.valueOf(edtCash3.getText()));
                        totalEY = totalAEY+totalFEY;
                        splitCash -= c;
                    }
                }else{
                    if (gVar.getMyCompanySite().getMyCompany().getInAppID().equals("1")){
                        edtCash1.setText(decim.format(cashAEY+splitCash).replace(",", "."));
                        totalAEY = Double.parseDouble(String.valueOf(txvFinalBalance1.getText()).equals("") ? "0.0" : String.valueOf(txvFinalBalance1.getText()));
                        cashAEY = Double.parseDouble(String.valueOf(edtCash1.getText()).equals("") ? "0.0" : String.valueOf(edtCash1.getText()));
                        splitCash = 0.0;
                    }else{
                        edtCash3.setText(decim.format(cashFEY+splitCash).replace(",", "."));
                        totalFEY = Double.parseDouble(String.valueOf(txvFinalBalance3.getText()).equals("") ? "0.0" : String.valueOf(txvFinalBalance3.getText()));
                        cashFEY = Double.parseDouble(String.valueOf(edtCash3.getText()).equals("") ? "0.0" : String.valueOf(edtCash3.getText()));
                        splitCash = 0.0;
                    }
                }
            }
            edtSplit.setText("");
        }
        catch (Exception e){
            Log.e("asdfg", e.getMessage(), e);
        }
    }
}
