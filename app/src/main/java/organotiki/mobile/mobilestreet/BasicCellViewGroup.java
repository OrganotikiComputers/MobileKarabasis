package organotiki.mobile.mobilestreet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.List;
import miguelbcr.ui.tableFixHeadesWrapper.TableFixHeaderAdapter;

public class BasicCellViewGroup extends FrameLayout implements TableFixHeaderAdapter.FirstHeaderBinder<String>, TableFixHeaderAdapter.HeaderBinder<String>, TableFixHeaderAdapter.FirstBodyBinder<List<String>>, TableFixHeaderAdapter.BodyBinder<List<String>>, TableFixHeaderAdapter.SectionBinder<List<String>> {
    @SuppressLint("ResourceType")
    public TextView textView = ((TextView) findViewById(16908308));

    @SuppressLint("ResourceType")
    public BasicCellViewGroup(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(17367052, this, true);
    }

    public void bindFirstHeader(String headerName) {
        this.textView.setText(headerName);
    }

    public void bindHeader(String headerName, int column) {
        this.textView.setText(headerName);
    }

    public void bindFirstBody(List<String> items, int row) {
        Log.d("asdfg", "items.get(row):" + items.get(row) + ", row:" + row);
        this.textView.setText(items.get(row));
    }

    public void bindBody(List<String> items, int row, int column) {
        this.textView.setText(items.get(column + 1));
    }

    public void bindSection(List<String> list, int row, int column) {
        String str;
        TextView textView2 = this.textView;
        if (column == 0) {
            str = "Section:" + (row + 1);
        } else {
            str = "";
        }
        textView2.setText(str);
    }
}
