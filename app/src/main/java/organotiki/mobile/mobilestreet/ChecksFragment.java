package organotiki.mobile.mobilestreet;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.objects.Bank;
import organotiki.mobile.mobilestreet.objects.Company;
import organotiki.mobile.mobilestreet.objects.FInvoice;
import organotiki.mobile.mobilestreet.objects.FInvoiceLine;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

/**
 * Created by Thanasis on 15/7/2016.
 */
public class ChecksFragment extends DialogFragment {

    Realm realm;
    GlobalVar gVar;
    Button addCheck, addPromissoryNote, back;
    ArrayList<FInvoiceLine> lines;
    ListView mListView;
    ArrayList<String> banklist;
    Company company;
    MyListAdapter mAdapter;
    AlertDialog mAlertDialog;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            View view = inflater.inflate(R.layout.fragment_checks, null);

            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            RealmResults<Bank> banks = realm.where(Bank.class).findAll();
            Integer bankCount = banks.size();
            banklist = new ArrayList<>();
//            banklist.add("ΠΕΙΡΑΙΩΣ");
//            banklist.add("ΕΘΝΙΚΗ");
            for (int i = 0; i < bankCount; i++) {
                banklist.add(banks.get(i).getDescription());
            }

            lines = new ArrayList<>();
//            if (realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", gVar.getMyFInvoice().getID()).equalTo("myCompany.ID", company.getID()).count() == 0 && !gVar.getMyFInvoice().isFinal()) {
//                final FInvoiceLine line = new FInvoiceLine(UUID.randomUUID().toString(), gVar.getMyFInvoice(), company);
//                realm.executeTransaction(new Realm.FTransaction() {
//                    @Override
//                    public void execute(Realm realm) {
//                        FInvoiceLine l = realm.copyToRealmOrUpdate(line);
//                        lines.add(l);
//                    }
//                });
//            } else {
            RealmResults<FInvoiceLine> rr = realm.where(FInvoiceLine.class).equalTo("myFInvoice.ID", gVar.getMyFInvoice().getID()).equalTo("myCompany.InAppID", company.getInAppID()).findAll();
            Integer rrCount = rr.size();
            for (int i = 0; i < rrCount; i++) {
                lines.add(rr.get(i));
            }
//            }

            addCheck = (Button) view.findViewById(R.id.button_add_check);
            if (gVar.getMyFInvoice().isFinal()) {
                addCheck.setVisibility(View.GONE);
            } else {
                addCheck.setTransformationMethod(null);
                addCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            final FInvoiceLine line = new FInvoiceLine(UUID.randomUUID().toString(), gVar.getMyFInvoice(), company, 1);
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    FInvoiceLine l = realm.copyToRealmOrUpdate(line);
                                    lines.add(l);
                                }
                            });
                            mAdapter.notifyDataSetChanged();
//                        mListView.setAdapter(new MyListAdapter());
//                    saveItemList();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }

                    }
                });
            }

            addPromissoryNote = (Button) view.findViewById(R.id.button_add_promissory_note);
            if (gVar.getMyFInvoice().isFinal()) {
                addPromissoryNote.setVisibility(View.GONE);
            } else {
                addPromissoryNote.setTransformationMethod(null);
                addPromissoryNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            final FInvoiceLine line = new FInvoiceLine(UUID.randomUUID().toString(), gVar.getMyFInvoice(), company, 2);
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    FInvoiceLine l = realm.copyToRealmOrUpdate(line);
                                    lines.add(l);
                                }
                            });
                            mAdapter.notifyDataSetChanged();
//                        mListView.setAdapter(new MyListAdapter());
//                    saveItemList();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }

                    }
                });
            }

//            final TextView title = (TextView) view.findViewById(R.id.textView_title);

            back = (Button) view.findViewById(R.id.button_back);
            back.setTransformationMethod(null);
            if (gVar.getMyFInvoice().isFinal()) {
                back.setText(R.string.back);
            }
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        try {
                            doExit();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }

                }
            });

            mAdapter = new MyListAdapter();
            mListView = (ListView) view.findViewById(R.id.listView_checks);
            mListView.setAdapter(mAdapter);

            setCancelable(false);
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(android.content.DialogInterface dialog, int keyCode, android.view.KeyEvent event) {

                    if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                        try {
                            doExit();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
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

    private void doExit() {
        try {
            boolean isNotCompleted = false;
            for (FInvoiceLine line : lines) {
                if (line.getExDate() == null || (line.getType() == 1 && line.getNumber() == null)) {
                    isNotCompleted = true;
                } else if (line.getValue() == 0.0 || line.getExDate().equals("") || (line.getType() == 1 && line.getNumber().equals(""))) {
                    isNotCompleted = true;
                }
            }
            if (isNotCompleted) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        getActivity());

                /*alertDialog.setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (final FInvoiceLine line : lines) {
                            if (line.getValue() == 0.0) {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
//                                        lines.remove(line);
                                        line.deleteFromRealm();
                                    }
                                });
                            }
                        }
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(close.getWindowToken(), 0);
                        dismiss();
                    }
                });*/

                alertDialog.setPositiveButton(getString(R.string.ok), null);
                alertDialog.setMessage("Δεν έχουν συμπληρωθεί όλα τα απαραίτητα πεδία των αξιογράφων σας.\nΣυμπληρώστε τα ή διαγράψτε τα ελλιπή αξιόγραφα για να συνεχίσετε.");
                alertDialog.setTitle(R.string.app_name);
                mAlertDialog = alertDialog.create();
                mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setTransformationMethod(null);
                    }
                });

                mAlertDialog.show();
            } else {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(back.getWindowToken(), 0);
                dismiss();
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onDismiss(dialog);
    }

    @Override
    public void onDestroyView() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
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
                    lines.get(position).setExDate(day + "/" + month + "/" + year);
                }
            });
            mAdapter.notifyDataSetChanged();
//            mListView.setAdapter(new MyListAdapter());
//            ((MyListAdapter.ViewHolder)mListView.getItemAtPosition(position)).ExpirationDate.setText(lines.get(position).getExDate());
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    private class MyListAdapter extends BaseAdapter {

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
        public View getView(final int position, View convertView, final ViewGroup parent) {
            try {
                final ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    convertView = inflater.inflate(R.layout.listview_collections, parent, false);
                    holder.Bank = (Spinner) convertView.findViewById(R.id.spinner_bank);

                    holder.ExpirationDate = (EditText) convertView.findViewById(R.id.editText_expiration_date);
                    holder.ExpirationDate.setKeyListener(null);
                    holder.Number = (EditText) convertView.findViewById(R.id.editText_number);
                    holder.Value = (EditText) convertView.findViewById(R.id.editText_value);
                    holder.Delete = (ImageButton) convertView.findViewById(R.id.imageButton_delete);


                    convertView.setTag(holder);

                    if (gVar.getMyFInvoice().isFinal()) {
                        holder.Bank.setClickable(false);
                        holder.Bank.setEnabled(false);
//                        holder.Number.setKeyListener(null);
                        holder.Number.setEnabled(false);
                        holder.Number.setFocusable(false);
                        holder.Number.setLongClickable(false);
                        holder.ExpirationDate.setEnabled(false);
//                        holder.Value.setKeyListener(null);
                        holder.Value.setFocusable(false);
                        holder.Value.setEnabled(false);
                        holder.Delete.setImageAlpha(130);
                        holder.Delete.setEnabled(false);

                    } else {


                        holder.ExpirationDate.setOnClickListener(new View.OnClickListener() {
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
                                        public void execute(Realm realm) {
                                            lines.get(holder.ref).setValueText(String.valueOf(holder.Value.getText()));
                                            gVar.getMyFInvoice().setTotal();
                                        }
                                    });
                                    ((Collections) getActivity()).setTotal();
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                            }
                        });

                        holder.Delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        try {
                                            lines.get(holder.ref).deleteFromRealm();
                                            lines.remove(holder.ref);
                                            gVar.getMyFInvoice().setTotal();
                                        } catch (Exception e) {
                                            Log.e("asdfg", e.getMessage(), e);
                                        }
                                    }
                                });
                                mAdapter.notifyDataSetChanged();
                                ((Collections) getActivity()).setTotal();
                            }
                        });
                    }
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                if (gVar.getMyFInvoice().isFinal()) {
                    if (lines.get(position).getType() == 1) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_collections_item, banklist);
                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        // Apply the mAdapter to the spinner
                        holder.Bank.setAdapter(adapter);
                    } else {
                        holder.Bank.setAdapter(null);
                    }
                } else {
                    if (lines.get(position).getType() == 1) {
                        Log.d("asdfg", "epitagi " + position + " " + holder.ref);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_collections_item, banklist);
                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        // Apply the mAdapter to the spinner
                        holder.Bank.setAdapter(adapter);
                        holder.Bank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(final AdapterView<?> adapterView, View view, final int i, long l) {
                                try {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            try {
                                                lines.get(holder.ref).setMyBank(realm.where(Bank.class).equalTo("Description", String.valueOf(adapterView.getItemAtPosition(i))).findFirst());
                                                Log.d("asdfg", "description3:" + lines.get(holder.ref).getMyBank());
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

                        holder.Number.addTextChangedListener(new TextWatcher() {
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
                                        public void execute(Realm realm) {
                                            lines.get(holder.ref).setNumber(String.valueOf(holder.Number.getText()));
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                            }
                        });
                        holder.Bank.setClickable(true);
                        holder.Bank.setEnabled(true);
                        holder.Number.setEnabled(true);
                        holder.Number.setLongClickable(true);
                        holder.Number.setFocusableInTouchMode(true);
                        holder.Number.setFocusable(true);
                        holder.Number.setHint(R.string.number);
//                        holder.Number.setKeyListener(new EditText(getActivity()).getKeyListener());
                    } else {
                        Log.d("asdfg", "grammatio " + position + " " + holder.ref);
                        holder.Bank.setClickable(false);
                        holder.Bank.setEnabled(false);
                        holder.Bank.setAdapter(null);
                        holder.Number.setEnabled(false);
                        holder.Number.setFocusable(false);
                        holder.Number.setLongClickable(false);
                        holder.Number.setHint("");
//                        holder.Number.setKeyListener(null);
                    }
                }

                holder.ref = position;
//                Log.d("asdfg","position: "+position+", type: "+ String.valueOf(lines.get(holder.ref).getType()));
                if (lines.get(holder.ref).getMyBank() == null) {
//                    Log.d("asdfg", "description1:"+lines.get(holder.ref).getMyBank());
                    holder.Bank.setSelection(0);
//                    Log.d("asdfg", "description2:"+lines.get(holder.ref).getMyBank());
                } else {
                    holder.Bank.setSelection(banklist.indexOf(lines.get(holder.ref).getMyBank().getDescription()));
                }

                holder.ExpirationDate.setText(lines.get(holder.ref).getExDate());
                holder.Number.setText(lines.get(holder.ref).getNumber());
                holder.Value.setText(lines.get(holder.ref).getValueText());


            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }

            return convertView;
        }

        class ViewHolder {
            String ID;
            Spinner Bank;
            EditText ExpirationDate;
            EditText Number;
            EditText Value;
            int ref;
            ImageButton Delete;
        }
    }
}
