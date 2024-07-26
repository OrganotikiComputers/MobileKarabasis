package organotiki.mobile.mobilestreet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import com.github.mikephil.charting.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import organotiki.mobile.mobilestreet.objects.Bank;
import organotiki.mobile.mobilestreet.objects.Company;
import organotiki.mobile.mobilestreet.objects.Deposit;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class DepositsFragment extends DialogFragment {
    Button btnAdd;
    Button btnOK;
    Company company;
    GlobalVar gVar;
    ArrayList<Deposit> lines;
    MyListAdapter mAdapter;
    AlertDialog mAlertDialog;
    ListView mListView;
    Realm realm;
    ArrayList<String> accountlist;

    public ArrayList<String> getAccountlist() {
        return accountlist;
    }

    public void setAccountlist(ArrayList<String> accountlist) {
        this.accountlist = accountlist;
    }

    //VolleyRequests request;
    ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_deposits, (ViewGroup) null);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            this.realm = Realm.getDefaultInstance();
            this.gVar = (GlobalVar) this.realm.where(GlobalVar.class).findFirst();
            RealmList<Deposit> deposits = this.gVar.getMyUser().getMyDebosits();
           // request=new VolleyRequests();

            this.lines = new ArrayList<>();
            this.lines.addAll(deposits);
            this.btnAdd = (Button) view.findViewById(R.id.button_add);
            this.btnAdd.setTransformationMethod((TransformationMethod) null);
            this.btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        final Deposit line = new Deposit(UUID.randomUUID().toString(), Double.valueOf(0.0), (String) null,(String) null);
                        DepositsFragment.this.realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                Deposit l = (Deposit) realm.copyToRealmOrUpdate(line);
                                DepositsFragment.this.gVar.getMyUser().getMyDebosits().add(l);
                                DepositsFragment.this.lines.add(l);
                            }
                        });
                        mAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
            this.btnOK = (Button) view.findViewById(R.id.button_ok);
            this.btnOK.setTransformationMethod((TransformationMethod) null);
            this.btnOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        DepositsFragment.this.doExit();
                    } catch (Exception e) {
                        try {
                            Log.e("asdfg", e.getMessage(), e);
                        } catch (Exception e2) {
                            Log.e("asdfg", e2.getMessage(), e2);
                        }
                    }
                }
            });
            mAdapter = new MyListAdapter();
            mListView = (ListView) view.findViewById(R.id.listView_checks);
            mListView.setAdapter(mAdapter);
            setCancelable(false);
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode != 4) {
                        return false;
                    }
                    try {
                        DepositsFragment.this.doExit();
                        return true;
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                        return true;
                    }
                }
            });
            adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_collections_item, accountlist);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
/*
            request.GetDepositAccounts(getActivity());
*/
            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    /* access modifiers changed from: private */

    private void doExit() {

        try {
            boolean isNotCompleted = false;
            for (Deposit line : lines) {
                if (line.getDate() == null || line.getValue() == 0.0 || TextUtils.isEmpty(line.getAccount())) {
                    isNotCompleted = true;
                }
            }
            if (isNotCompleted) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setPositiveButton(getString(R.string.ok), (DialogInterface.OnClickListener) null);
                alertDialog.setMessage("Δεν έχουν συμπληρωθεί όλα τα απαραίτητα πεδία των καταθέσεων σας.\nΣυμπληρώστε τα ή διαγράψτε τις ελλιπής καταθέσεις για να συνεχίσετε.");
                alertDialog.setTitle(R.string.app_name);
                this.mAlertDialog = alertDialog.create();
                this.mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    public void onShow(DialogInterface dialog) {
                        ((AlertDialog) dialog).getButton(-1).setTransformationMethod((TransformationMethod) null);
                    }
                });
                this.mAlertDialog.show();
                return;
            }
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            imm.hideSoftInputFromWindow(btnOK.getWindowToken(), 0);
            dismiss();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
        super.onDismiss(dialog);
    }
    @Override
    public void onDestroyView() {
        try {
            ((InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
        super.onDestroyView();
    }


    public void respondDate(final Integer position, final int year, final int month, final int day) {
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    lines.get(position).setDate(day + "/" + month + "/" + year);
                }
            });
            mAdapter.notifyDataSetChanged();
//            mListView.setAdapter(new MyListAdapter());
//            ((MyListAdapter.ViewHolder)mListView.getItemAtPosition(position)).ExpirationDate.setText(lines.get(position).getExDate());
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void setCompany(Company company2) {
        this.company = company2;
    }

    private class MyListAdapter extends BaseAdapter {
        private MyListAdapter() {
        }
        @Override
        public int getCount() {
            if (lines != null && lines.size() != 0) {
                return lines.size();
            }
            return 0;
        }
        @Override
        public Object getItem(int position) {
            return lines.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                final ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = DepositsFragment.this.getActivity().getLayoutInflater().inflate(R.layout.listview_deposits, parent, false);
                    holder.Date = (EditText) convertView.findViewById(R.id.editText_expiration_date);
                    holder.Date.setKeyListener(null);
                    holder.Value = (EditText) convertView.findViewById(R.id.editText_value);
                    holder.Delete = (ImageButton) convertView.findViewById(R.id.imageButton_delete);
                    convertView.setTag(holder);
                    holder.Date.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FragmentManager manager = getFragmentManager();
                            DatePickerFragment fragment = new DatePickerFragment();
                            fragment.setLimit(false);
                            fragment.setPosition(holder.ref);
                            fragment.show(manager, "datePicker");
                        }
                    });
                    holder.Value.addTextChangedListener(new TextWatcher() {
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        public void afterTextChanged(Editable editable) {
                            try {
                                DepositsFragment.this.realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        DepositsFragment.this.lines.get(holder.ref).setValueText(String.valueOf(holder.Value.getText()));
                                    }
                                });
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    });
                    holder.Delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DepositsFragment.this.realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    try {
                                        DepositsFragment.this.lines.get(holder.ref).deleteFromRealm();
                                        DepositsFragment.this.lines.remove(holder.ref);
                                    } catch (Exception e) {
                                        Log.e("asdfg", e.getMessage(), e);
                                    }
                                }
                            });
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.Account = (Spinner) convertView.findViewById(R.id.spinner_account);
                // Specify the layout to use when the list of choices appears
                // Apply the mAdapter to the spinner
                holder.Account.setAdapter(adapter);
                holder.Account.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(final AdapterView<?> adapterView, View view, final int i, long l) {
                        try {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    try {
                                        lines.get(holder.ref).setAccount(String.valueOf(adapterView.getItemAtPosition(i)));
                                    } catch (Exception e) {
                                        Log.e("asdfg", e.getMessage(), e);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                holder.ref = position;
                holder.Date.setText(lines.get(holder.ref).getDate());
                holder.Value.setText(DepositsFragment.this.lines.get(holder.ref).getValueText());
                for (int i= 0; i < accountlist.size(); i++) {
                    if (accountlist.get(i).equals(lines.get(holder.ref).getAccount())) {
                        holder.Account.setSelection(i);
                        break;
                    }
                }
            }
            catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
            return convertView;
        }

        class ViewHolder {
            EditText Date;
            ImageButton Delete;
            EditText Value;
            Spinner Account;
            int ref;

        }
    }

   /* public void RespondAccounts(JSONObject jsonObject){
        fillArraysAsync async = new fillArraysAsync();
        async.execute(jsonObject.toString());
    }
    private class fillArraysAsync extends AsyncTask<String, String, String> {

        private String resp;

        @Override
        protected String doInBackground(String... params) {
//            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            try {
                // Do your long operations here and return the result
                JSONObject response = new JSONObject(params[0]);
                JSONArray jsonArray = response.getJSONArray("GetDepositAccountsResult");
                int arrayLen = jsonArray.length();
                accountlist = new ArrayList<>();
                for (int i = 0; i < arrayLen; i++) {
                    accountlist.add(String.valueOf(jsonArray.get(i)));
                }
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
                resp = e.getMessage();
            }
            return resp;
        }

        *//*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         *//*
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
//            finalResult.setText(result);
            adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_collections_item, accountlist);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            adapter.notifyDataSetChanged();
        }

        *//*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         *//*
        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }

        *//*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         *//*
        @Override
        protected void onProgressUpdate(String... text) {
//            finalResult.setText(text[0]);
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }
    }
*/
}
