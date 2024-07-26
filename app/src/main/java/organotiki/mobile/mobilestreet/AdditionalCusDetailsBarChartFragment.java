package organotiki.mobile.mobilestreet;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.InputType;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import io.realm.Realm;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import organotiki.mobile.mobilestreet.R;
import organotiki.mobile.mobilestreet.objects.CustomerDetail;
import organotiki.mobile.mobilestreet.objects.CustomerDetailRow;
import organotiki.mobile.mobilestreet.objects.CustomerDetailTab;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

public class AdditionalCusDetailsBarChartFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private static final String ARG_TITLE = "title";
    BarChart barChart;
    AppCompatButton btnEmail;
    int columnCount;
    ArrayList<CustomerDetailRow> details;
    String[] emailList;
    GlobalVar gVar;
    AlertDialog mAlertDialog;
    /* access modifiers changed from: private */
    public long mLastClickTime = 0;
    CustomerDetailTab myTab;
    private int position;
    Button positiveButton;
    Realm realm;
    VolleyRequests request;
    int rowCount;
    /* access modifiers changed from: private */
    public String title;
    AppCompatTextView totals;
    View view;

    public static AdditionalCusDetailsBarChartFragment newInstance(int position2, String Title) {
        AdditionalCusDetailsBarChartFragment f = new AdditionalCusDetailsBarChartFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position2);
        b.putString(ARG_TITLE, Title);
        f.setArguments(b);
        return f;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.position = getArguments().getInt(ARG_POSITION);
        this.title = getArguments().getString(ARG_TITLE);
        Log.d("asdfg", "Position: " + this.position);
        Log.d("asdfg", "Title: " + this.title);
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        String str;
        try {
            super.onCreate(savedInstanceState);
            this.view = inflater.inflate(R.layout.fragment_additional_cus_details_bar_chart_tab, (ViewGroup) null);
            this.realm = MyApplication.getRealm();
            this.gVar = (GlobalVar) this.realm.where(GlobalVar.class).findFirst();
            this.request = new VolleyRequests();
            this.barChart = (BarChart) this.view.findViewById(R.id.barChart_additional_customer_details);
            this.totals = (AppCompatTextView) this.view.findViewById(R.id.textView_total);
            this.btnEmail = (AppCompatButton) this.view.findViewById(R.id.button_email_chart);
            this.btnEmail.setTransformationMethod((TransformationMethod) null);
            this.btnEmail.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ((AdditionalCustomerDetails) AdditionalCusDetailsBarChartFragment.this.getActivity()).setRequestingFragment(AdditionalCusDetailsBarChartFragment.this);
                    AdditionalCusDetailsBarChartFragment.this.request.getCustomerEmails(AdditionalCusDetailsBarChartFragment.this.getActivity(), ((AdditionalCustomerDetails) AdditionalCusDetailsBarChartFragment.this.getActivity()).getCustomer());
                }
            });
            this.details = new ArrayList<>();
            this.myTab = (CustomerDetailTab) this.realm.where(CustomerDetailTab.class).equalTo("Title", this.title).findFirst();
            if (this.myTab.getDetails().size() > 0) {
                String totalTitle = "";
                Iterator<CustomerDetail> it = this.myTab.getDetails().iterator();
                while (it.hasNext()) {
                    CustomerDetail row = it.next();
                    StringBuilder sb = new StringBuilder();
                    sb.append(totalTitle);
                    if (isNullOrWhitespace(totalTitle)) {
                        str = "";
                    } else {
                        str = " - ";
                    }
                    sb.append(str);
                    sb.append(row.getHeader());
                    sb.append(" : ");
                    sb.append(row.getValue());
                    totalTitle = sb.toString();
                }
                this.totals.setText(totalTitle);
            } else {
                this.totals.setVisibility(View.GONE);
            }
            this.details.addAll(this.myTab.getBarChartDetails());
            this.columnCount = this.details.get(0).getData().size();
            this.rowCount = this.details.size();
            setBarChart();
            return this.view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    private void setBarChart() {
        try {
            int i = this.rowCount;
            if (i == 2) {
                BarDataSet dataSet1 = new BarDataSet(DetailRowToBarEntryList(this.details.get(1).getData()), this.details.get(1).getData().get(0));
                dataSet1.setColors(getResources().getColor(R.color.colorRed));
                dataSet1.setValueTextColor(getResources().getColor(R.color.colorAccent));
                final String[] Titles = DetailRowToStringArray(this.details.get(0).getData());
                IAxisValueFormatter formatter = new IAxisValueFormatter() {
                    public String getFormattedValue(float value, AxisBase axis) {
                        if (value < 0.0f) {
                            return "";
                        }
                        String[] strArr = Titles;
                        if (value < ((float) strArr.length)) {
                            return strArr[(int) value];
                        }
                        return "";
                    }
                };
                XAxis xAxis = this.barChart.getXAxis();
                xAxis.setCenterAxisLabels(true);
                xAxis.setGranularity(1.0f);
                xAxis.setTextColor(getResources().getColor(R.color.colorAccent));
                xAxis.setTextSize(12.0f);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(Titles));
                xAxis.setLabelCount(12);
                xAxis.setSpaceMin(0.9f / 2.0f);
                xAxis.setSpaceMax(0.9f / 2.0f);
                YAxis leftAxis = this.barChart.getAxisLeft();
                leftAxis.setTextSize(12.0f);
                leftAxis.setTextColor(getResources().getColor(R.color.colorAccent));
                leftAxis.setAxisMinimum(0.0f);
                YAxis rightAxis = this.barChart.getAxisRight();
                rightAxis.setTextSize(12.0f);
                rightAxis.setTextColor(getResources().getColor(R.color.colorAccent));
                rightAxis.setAxisMinimum(0.0f);
                BarData data = new BarData(dataSet1);
                this.barChart.getLegend().setTextColor(getResources().getColor(R.color.colorAccent));
                this.barChart.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                this.barChart.getDescription().setEnabled(false);
                data.setBarWidth(0.9f);
                this.barChart.setData(data);
                this.barChart.invalidate();
            } else if (i == 3) {
                List<BarEntry> entries = DetailRowToBarEntryList(this.details.get(1).getData());
                List<BarEntry> entries2 = DetailRowToBarEntryList(this.details.get(2).getData());
                BarDataSet dataSet12 = new BarDataSet(entries, this.details.get(1).getData().get(0));
                BarDataSet dataSet2 = new BarDataSet(entries2, this.details.get(2).getData().get(0));
                dataSet12.setColors(getResources().getColor(R.color.colorRed));
                dataSet12.setValueTextColor(getResources().getColor(R.color.colorAccent));
                dataSet2.setColors(getResources().getColor(R.color.colorGreen));
                dataSet2.setValueTextColor(getResources().getColor(R.color.colorAccent));
                final String[] Titles2 = DetailRowToStringArray(this.details.get(0).getData());
                IAxisValueFormatter formatter2 = new IAxisValueFormatter() {
                    public String getFormattedValue(float value, AxisBase axis) {
                        if (value < 0.0f) {
                            return "";
                        }
                        String[] strArr = Titles2;
                        if (value < ((float) strArr.length)) {
                            return strArr[(int) value];
                        }
                        return "";
                    }
                };
                XAxis xAxis2 = this.barChart.getXAxis();
                xAxis2.setCenterAxisLabels(true);
                xAxis2.setGranularity(1.0f);
                xAxis2.setTextColor(getResources().getColor(R.color.colorAccent));
                xAxis2.setTextSize(12.0f);
                xAxis2.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis2.setAxisMinimum(0.0f);
                xAxis2.setAxisMaximum((float) Titles2.length);
                xAxis2.setValueFormatter((new IndexAxisValueFormatter(Titles2)));
                xAxis2.setLabelCount(12);
                YAxis leftAxis2 = this.barChart.getAxisLeft();
                leftAxis2.setTextSize(12.0f);
                leftAxis2.setTextColor(getResources().getColor(R.color.colorAccent));
                leftAxis2.setAxisMinimum(0.0f);
                YAxis rightAxis2 = this.barChart.getAxisRight();
                rightAxis2.setTextSize(12.0f);
                rightAxis2.setTextColor(getResources().getColor(R.color.colorAccent));
                rightAxis2.setAxisMinimum(0.0f);
                BarData data2 = new BarData(dataSet12, dataSet2);
                List<BarEntry> list = entries;
                List<BarEntry> list2 = entries2;
                this.barChart.getLegend().setTextColor(getResources().getColor(R.color.colorAccent));
                this.barChart.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                this.barChart.getDescription().setEnabled(false);
                data2.setBarWidth(0.45f);
                this.barChart.setData(data2);
                this.barChart.groupBars(0.02f, 0.06f, 0.02f);
                this.barChart.invalidate();
            } else if (i == 4) {
                List<BarEntry> entries3 = DetailRowToBarEntryList(this.details.get(1).getData());
                List<BarEntry> entries22 = DetailRowToBarEntryList(this.details.get(2).getData());
                List<BarEntry> entries32 = DetailRowToBarEntryList(this.details.get(3).getData());
                BarDataSet dataSet13 = new BarDataSet(entries3, this.details.get(1).getData().get(0));
                BarDataSet dataSet22 = new BarDataSet(entries22, this.details.get(2).getData().get(0));
                BarDataSet dataSet3 = new BarDataSet(entries32, this.details.get(3).getData().get(0));
                dataSet13.setColors(getResources().getColor(R.color.colorRed));
                dataSet13.setValueTextColor(getResources().getColor(R.color.colorAccent));
                dataSet22.setColors(getResources().getColor(R.color.colorGreen));
                dataSet22.setValueTextColor(getResources().getColor(R.color.colorAccent));
                dataSet3.setColors(getResources().getColor(R.color.colorF));
                dataSet3.setValueTextColor(getResources().getColor(R.color.colorAccent));
                final String[] Titles3 = DetailRowToStringArray(this.details.get(0).getData());
                IAxisValueFormatter formatter3 = new IAxisValueFormatter() {
                    public String getFormattedValue(float value, AxisBase axis) {
                        if (value < 0.0f) {
                            return "";
                        }
                        String[] strArr = Titles3;
                        if (value < ((float) strArr.length)) {
                            return strArr[(int) value];
                        }
                        return "";
                    }
                };
                XAxis xAxis3 = this.barChart.getXAxis();
                xAxis3.setCenterAxisLabels(true);
                xAxis3.setGranularity(1.0f);
                xAxis3.setTextColor(getResources().getColor(R.color.colorAccent));
                xAxis3.setTextSize(12.0f);
                xAxis3.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis3.setAxisMinimum(0.0f);
                xAxis3.setAxisMaximum((float) Titles3.length);
                xAxis3.setValueFormatter((new IndexAxisValueFormatter(Titles3)));
                xAxis3.setLabelCount(12);
                YAxis leftAxis3 = this.barChart.getAxisLeft();
                leftAxis3.setTextSize(12.0f);
                leftAxis3.setTextColor(getResources().getColor(R.color.colorAccent));
                leftAxis3.setAxisMinimum(0.0f);
                YAxis rightAxis3 = this.barChart.getAxisRight();
                rightAxis3.setTextSize(12.0f);
                rightAxis3.setTextColor(getResources().getColor(R.color.colorAccent));
                rightAxis3.setAxisMinimum(0.0f);
                List<BarEntry> list3 = entries3;
                List<BarEntry> list4 = entries22;
                BarData data3 = new BarData(dataSet13, dataSet22, dataSet3);
                List<BarEntry> list5 = entries32;
                BarDataSet barDataSet = dataSet13;
                this.barChart.getLegend().setTextColor(getResources().getColor(R.color.colorAccent));
                this.barChart.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                this.barChart.getDescription().setEnabled(false);
                data3.setBarWidth(0.3f);
                this.barChart.setData(data3);
                this.barChart.groupBars(0.02f, 0.04f, 0.02f);
                this.barChart.invalidate();
            } else if (i == 5) {
                List<BarEntry> entries4 = DetailRowToBarEntryList(this.details.get(1).getData());
                List<BarEntry> entries23 = DetailRowToBarEntryList(this.details.get(2).getData());
                List<BarEntry> entries33 = DetailRowToBarEntryList(this.details.get(3).getData());
                List<BarEntry> entries42 = DetailRowToBarEntryList(this.details.get(4).getData());
                BarDataSet dataSet14 = new BarDataSet(entries4, this.details.get(1).getData().get(0));
                BarDataSet dataSet23 = new BarDataSet(entries23, this.details.get(2).getData().get(0));
                BarDataSet dataSet32 = new BarDataSet(entries33, this.details.get(3).getData().get(0));
                BarDataSet dataSet4 = new BarDataSet(entries42, this.details.get(4).getData().get(0));
                dataSet14.setColors(getResources().getColor(R.color.colorRed));
                dataSet14.setValueTextColor(getResources().getColor(R.color.colorAccent));
                dataSet23.setColors(getResources().getColor(R.color.colorGreen));
                dataSet23.setValueTextColor(getResources().getColor(R.color.colorAccent));
                dataSet32.setColors(getResources().getColor(R.color.colorA));
                dataSet32.setValueTextColor(getResources().getColor(R.color.colorAccent));
                dataSet4.setColors(getResources().getColor(R.color.colorA));
                dataSet4.setValueTextColor(getResources().getColor(R.color.colorAccent));
                final String[] Titles4 = DetailRowToStringArray(this.details.get(0).getData());
                IAxisValueFormatter r9 = new IAxisValueFormatter() {
                    public String getFormattedValue(float value, AxisBase axis) {
                        if (value < 0.0f) {
                            return "";
                        }
                        String[] strArr = Titles4;
                        if (value < ((float) strArr.length)) {
                            return strArr[(int) value];
                        }
                        return "";
                    }
                };
                XAxis xAxis4 = this.barChart.getXAxis();
                xAxis4.setCenterAxisLabels(true);
                xAxis4.setGranularity(1.0f);
                xAxis4.setTextColor(getResources().getColor(R.color.colorAccent));
                xAxis4.setTextSize(12.0f);
                xAxis4.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis4.setAxisMinimum(0.0f);
                xAxis4.setAxisMaximum((float) Titles4.length);
                xAxis4.setValueFormatter(new IndexAxisValueFormatter(Titles4));
                xAxis4.setLabelCount(12);
                YAxis leftAxis4 = this.barChart.getAxisLeft();
                leftAxis4.setTextSize(12.0f);
                leftAxis4.setTextColor(getResources().getColor(R.color.colorAccent));
                leftAxis4.setAxisMinimum(0.0f);
                YAxis rightAxis4 = this.barChart.getAxisRight();
                rightAxis4.setTextSize(12.0f);
                List<BarEntry> list6 = entries4;
                rightAxis4.setTextColor(getResources().getColor(R.color.colorAccent));
                rightAxis4.setAxisMinimum(0.0f);
                List<BarEntry> list7 = entries42;
                String[] strArr = Titles4;
                IAxisValueFormatter r17 = r9;
                BarData data4 = new BarData(dataSet14, dataSet23, dataSet32, dataSet4);
                BarDataSet barDataSet2 = dataSet14;
                BarDataSet barDataSet3 = dataSet32;
                this.barChart.getLegend().setTextColor(getResources().getColor(R.color.colorAccent));
                this.barChart.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                this.barChart.getDescription().setEnabled(false);
                data4.setBarWidth(0.22f);
                this.barChart.setData(data4);
                this.barChart.groupBars(0.02f, 0.04f, 0.02f);
                this.barChart.invalidate();
            }
        } catch (Exception e) {
            Exception e2 = e;
            Log.e("asdfg", e2.getMessage(), e2);
        }
    }

    private void doEmail() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setMinimumWidth(-1);
            layout.setPadding(10, 0, 10, 0);
            final AutoCompleteTextView input1 = new AutoCompleteTextView(getActivity());
            final AutoCompleteTextView input2 = new AutoCompleteTextView(getActivity());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, this.emailList);
            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, this.emailList);
            input1.setLayoutParams(lp);
            input1.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            input1.setHint(R.string.email);
            input1.setThreshold(0);
            input1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    input1.showDropDown();
                }
            });
            input1.setAdapter(adapter1);
            if (((AdditionalCustomerDetails) getActivity()).getAddress() != null && !((AdditionalCustomerDetails) getActivity()).getAddress().getEmail().equals("")) {
                input1.setText(((AdditionalCustomerDetails) getActivity()).getAddress().getEmail());
            }
            Log.d("asdfg", Arrays.toString(this.emailList));
            input2.setLayoutParams(lp);
            input2.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            input2.setHint(R.string.email);
            input2.setThreshold(0);
            input2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    input2.showDropDown();
                }
            });
            input2.setAdapter(adapter2);
            layout.addView(input1);
            layout.addView(input2);
            alertDialog.setView(layout);
            alertDialog.setPositiveButton("Αποστολή", (DialogInterface.OnClickListener) null);
            alertDialog.setNegativeButton("Άκυρο", (DialogInterface.OnClickListener) null);
            alertDialog.setMessage("Σε ποια e-mail θέλετε να σταλθεί το γράφημα;");
            alertDialog.setTitle(R.string.app_name);
            this.mAlertDialog = alertDialog.create();
            this.mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                public void onShow(DialogInterface dialog) {
                    ((AlertDialog) dialog).getButton(-1).setTransformationMethod((TransformationMethod) null);
                    ((AlertDialog) dialog).getButton(-2).setTransformationMethod((TransformationMethod) null);
                }
            });
            this.mAlertDialog.show();
            this.positiveButton = this.mAlertDialog.getButton(-1);
            this.positiveButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        if (SystemClock.elapsedRealtime() - AdditionalCusDetailsBarChartFragment.this.mLastClickTime > 5000) {
                            long unused = AdditionalCusDetailsBarChartFragment.this.mLastClickTime = SystemClock.elapsedRealtime();
                            if (!AdditionalCusDetailsBarChartFragment.this.isEmailValid(input1.getText())) {
                                if (!AdditionalCusDetailsBarChartFragment.this.isEmailValid(input2.getText())) {
                                    Toast.makeText(AdditionalCusDetailsBarChartFragment.this.getActivity(), "Πρέπει να συμπληρώσετε με τουλάχιστον μια σωστή διεύθυνση email.", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                            Toast.makeText(AdditionalCusDetailsBarChartFragment.this.getActivity(), "Παρακαλώ περιμένετε.", Toast.LENGTH_LONG).show();
                            AdditionalCusDetailsBarChartFragment.this.request.SentEmailWithImage(AdditionalCusDetailsBarChartFragment.this.getActivity(), AdditionalCusDetailsBarChartFragment.this.barChart.getChartBitmap(), (ArrayList<CustomerDetailRow>) null, AdditionalCusDetailsBarChartFragment.this.title, String.valueOf(input1.getText()), String.valueOf(input2.getText()));
                            AdditionalCusDetailsBarChartFragment.this.positiveButton.setEnabled(false);
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

    public void setEmails(JSONObject jsonObject) {
        try {
            JSONArray array = jsonObject.getJSONArray("GetCustomerEmailsResult");
            int l = array.length();
            this.emailList = new String[l];
            if (l > 0) {
                for (int i = 0; i < l; i++) {
                    this.emailList[i] = String.valueOf(array.get(i));
                }
            }
            doEmail();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private List<BarEntry> DetailRowToBarEntryList(RealmList<String> Data) {
        List<BarEntry> entryList = new ArrayList<>();
        for (int i = 1; i < Data.size(); i++) {
            String data = Data.get(i);
            entryList.add(new BarEntry((float) i, Float.parseFloat(isNullOrWhitespace(data) ? "0.0" : data.replace(",", "."))));
        }
        return entryList;
    }

    private String[] DetailRowToStringArray(RealmList<String> Data) {
        String[] array = new String[(Data.size() - 1)];
        for (int i = 1; i < Data.size(); i++) {
            array[i - 1] = Data.get(i);
        }
        return array;
    }

    public static boolean isNullOrWhitespace(String s) {
        return s == null || isWhitespace(s);
    }

    private static boolean isWhitespace(String s) {
        int length = s.length();
        if (length <= 0) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public int getColorByName(String name) {
        int colorId = 0;
        try {
            colorId = R.color.class.getField(name).getInt((Object) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("asdfg", "ColorName:" + name + ",ColorID:" + colorId);
        return colorId;
    }

    /* access modifiers changed from: package-private */
    public boolean isEmailValid(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
