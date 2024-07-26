package organotiki.mobile.mobilestreet;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import io.realm.Realm;
import java.util.ArrayList;
import organotiki.mobile.mobilestreet.objects.CustomerDetailRow;
import organotiki.mobile.mobilestreet.objects.CustomerDetailTab;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

public class AdditionalCusDetailsHorizontalFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private static final String ARG_TITLE = "title";
    int columnCount;
    ArrayList<CustomerDetailRow> details;
    GlobalVar gVar;
    private int position;
    Realm realm;
    int rowCount;
    TableLayout tableLayout;
    private String title;
    View view;

    public static AdditionalCusDetailsHorizontalFragment newInstance(int position2, String Title) {
        AdditionalCusDetailsHorizontalFragment f = new AdditionalCusDetailsHorizontalFragment();
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
        try {
            super.onCreate(savedInstanceState);
            this.view = inflater.inflate(R.layout.fragment_additional_cus_details_horizontal_tab, (ViewGroup) null);
            this.realm = MyApplication.getRealm();
            this.gVar = (GlobalVar) this.realm.where(GlobalVar.class).findFirst();
            this.details = new ArrayList<>();
            this.details.addAll(((CustomerDetailTab) this.realm.where(CustomerDetailTab.class).equalTo("Title", this.title).findFirst()).getHorizontalDetails());
            this.columnCount = this.details.get(0).getData().size();
            this.rowCount = this.details.size();
            this.tableLayout = (TableLayout) this.view.findViewById(R.id.tableLayout_customer_info_additional_customer_details);
            setTableLayout();
            return this.view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    private void setTableLayout() {
        try {
            TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams();
            TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams();
            tableRowParams.weight = 1.0f;
            for (int i = 0; i < this.rowCount; i++) {
                TableRow tableRow = new TableRow(getActivity());
                for (int j = 0; j < this.columnCount; j++) {
                    TextView textView = new TextView(getActivity());
                    textView.setTextColor(getResources().getColor(R.color.colorAccent));
                    textView.setGravity(17);
                    textView.setPadding(10, 10, 10, 10);
                    textView.setBackground(getResources().getDrawable(R.drawable.textview_border));
                    textView.setTextSize(18.0f);
                    textView.setText(this.details.get(i).getData().get(j));
                    if (i == 0) {
                        textView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    }
                    tableRow.addView(textView, tableRowParams);
                }
                this.tableLayout.addView(tableRow, tableLayoutParams);
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
}
