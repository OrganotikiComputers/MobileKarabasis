package organotiki.mobile.mobilestreet;

import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.Rect;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;

import io.realm.Realm;
import organotiki.mobile.mobilestreet.objects.Company;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;

/**
 * Created by Thanasis on 15/7/2016.
 */
public class ListViewReturnsItemsAdapter extends ArrayAdapter<InvoiceLineSimple> {

    Realm realm;
    GlobalVar gVar;

    private DecimalFormat decim = new DecimalFormat("0.00");
    private ArrayList<InvoiceLineSimple> lines;
    private Activity activity;
    private Rect r = new Rect();
    private float historicX = Float.NaN, historicY = Float.NaN;
    private static int DELTA = 50;
    VolleyRequests request = new VolleyRequests();
    private SwipeRefreshLayout mSwipeRefreshLayout;


    private class ViewHolder {
        String ID;
        ImageView Image;
        TextView Code;
        TextView Description;
        TextView LastQuantity;
        TextView Price;       
		TextView ManagementCost;
        EditText Quantity;
        TextView Value;
        TextView LastDate;
        TextView LastCompany;
        AppCompatCheckBox Guarantee;
        AppCompatCheckBox FromCustomer;
        int ref;
    }

    public ListViewReturnsItemsAdapter(Activity activity, ArrayList<InvoiceLineSimple> invoiceLines) {
        super(activity, 0, invoiceLines);
        this.lines = invoiceLines;
        this.activity = activity;
        Log.d("asdfg", "in-" + lines.size());
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            // Get the data item for this position
            final InvoiceLineSimple invoiceLine = lines.get(position);
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
			boolean z = false;
            // Check if an existing view is being reused, otherwise inflate the view
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.listview_returns_items, parent, false);
                viewHolder.Image = (ImageView) convertView.findViewById(R.id.imageView_image);
                viewHolder.Code = (TextView) convertView.findViewById(R.id.code);
                viewHolder.Description = (TextView) convertView.findViewById(R.id.description);
                viewHolder.LastQuantity = (TextView) convertView.findViewById(R.id.last_quantity);
                viewHolder.Price = (TextView) convertView.findViewById(R.id.price);
                viewHolder.Quantity = (EditText) convertView.findViewById(R.id.quantity);
                viewHolder.Quantity.addTextChangedListener(new TextWatcher() {
                    String startingText;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        startingText = String.valueOf(s);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(final Editable s) {
                        try {
                            if (lines.get(viewHolder.ref).getLastQuantity() > 0) {
                                if (Double.parseDouble(String.valueOf(s).equals("") ? "0.0" : String.valueOf(s)) > lines.get(viewHolder.ref).getLastQuantity()) {
                                    Toast.makeText(activity, "Η ποσότητα δεν μπορεί να είναι μεγαλύτερη από την ποσότητα του τελευταίου παραστατικού.", Toast.LENGTH_LONG).show();
                                    viewHolder.Quantity.setText(startingText);
                                    if (activity instanceof Returns) {
                                        if (!((Returns) activity).isRecentPurchasesActive(lines.get(viewHolder.ref))) {
                                            request.getRecentPurchases(activity, lines.get(viewHolder.ref));
                                        }
                                    } else {
                                        if (!((ItemsReturns) activity).isRecentPurchasesActive(lines.get(viewHolder.ref))) {
                                            request.getRecentPurchases(activity, lines.get(viewHolder.ref));
                                        }
                                    }
                                } else {
                                    lines.get(viewHolder.ref).setQuantity(Double.parseDouble(s.toString().replace(",", ".").equals("") ? "0" : s.toString().replace(",", ".")));
                                    lines.get(viewHolder.ref).setValue(lines.get(viewHolder.ref).getPrice() * lines.get(viewHolder.ref).getQuantity() * (100 - lines.get(viewHolder.ref).getDiscount()) / 100);
                                    viewHolder.Value.setText(lines.get(viewHolder.ref).getValueText());
                                }
                            }else {
                                lines.get(viewHolder.ref).setQuantity(Double.parseDouble(s.toString().replace(",", ".").equals("") ? "0" : s.toString().replace(",", ".")));
                                lines.get(viewHolder.ref).setValue(lines.get(viewHolder.ref).getPrice() * lines.get(viewHolder.ref).getQuantity() * (100 - lines.get(viewHolder.ref).getDiscount()) / 100);
                                viewHolder.Value.setText(lines.get(viewHolder.ref).getValueText());
                            }
                        } catch (Exception e) {
                            Log.d("asdfg", String.valueOf(e));
                        }
                    }
                });
				viewHolder.ManagementCost = (TextView) convertView.findViewById(R.id.managementCost);
                viewHolder.Value = (TextView) convertView.findViewById(R.id.value);
                viewHolder.LastDate = (TextView) convertView.findViewById(R.id.textView_date);
                viewHolder.LastCompany = (TextView) convertView.findViewById(R.id.textView_company);
                viewHolder.Guarantee = (AppCompatCheckBox) convertView.findViewById(R.id.checkBox_guarantee);
                viewHolder.Guarantee.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        try {
                            if (viewHolder.Guarantee.isChecked()) {
                                lines.get(viewHolder.ref).setGuarantee(true);
                                FragmentManager manager = activity.getFragmentManager();
                                GuaranteeFormFragment frag = new GuaranteeFormFragment();
                                frag.setLine(invoiceLine);
                                frag.show(manager, "Items Details Fragment");
                            } else {
                                lines.get(viewHolder.ref).setGuarantee(false);
                            }
                            ListViewReturnsItemsAdapter.this.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });
                viewHolder.FromCustomer = (AppCompatCheckBox) convertView.findViewById(R.id.checkBox_script);
                viewHolder.FromCustomer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        try {
                            if (viewHolder.FromCustomer.isChecked()) {
                                lines.get(viewHolder.ref).setFromCustomer(true);
                            } else {
                                lines.get(viewHolder.ref).setFromCustomer(false);
                            }
//                            sortAdapter();
                            ListViewReturnsItemsAdapter.this.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });

//                convertView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        FragmentManager manager = activity.getFragmentManager();
//                        ItemDetailsFragment frag = new ItemDetailsFragment();
//                        frag.setLine(lines.get(viewHolder.ref));
//                        frag.show(manager, "Items Details Fragment");
//                    }
//                });

                convertView.setClickable(true);
                convertView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        try {
                            int action = event.getAction();
                            v.getHitRect(r);
                            float x = event.getX() + r.left;
                            float y = event.getY() + r.top;
                            LinearLayout linearLayout = (LinearLayout) v;
                            switch (action) {
                                case MotionEvent.ACTION_DOWN:
                                    historicX = x;
                                    historicY = y;
                                    Log.d("asdfg", String.valueOf(historicX) + "-" + String.valueOf(historicY));
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if (event.getX() - historicX < -DELTA) {
                                        Log.d("asdfg", "red");
                                        linearLayout.setBackgroundColor(Color.RED);
                                        ((ListView) linearLayout.getParent()).smoothScrollBy(0, 0);
                                        if (mSwipeRefreshLayout != null) {
                                            mSwipeRefreshLayout.setEnabled(false);
                                        }
                                        return true;
                                    } else if (event.getX() - historicX > DELTA) {
                                        Log.d("asdfg", "green");
                                        linearLayout.setBackgroundColor(Color.GREEN);
                                        ((ListView) linearLayout.getParent()).smoothScrollBy(0, 0);
                                        if (mSwipeRefreshLayout != null) {
                                            mSwipeRefreshLayout.setEnabled(false);
                                        }
                                        return true;
                                    }
                                    break;
                                case MotionEvent.ACTION_UP:
                                    try {
                                        double q;
                                        if (String.valueOf(viewHolder.Quantity.getText()).isEmpty()) {
                                            q = 0;
                                        } else {
                                            q = Double.parseDouble(String.valueOf(viewHolder.Quantity.getText()));
                                        }
                                        if (event.getX() - historicX < -DELTA) {
                                            Log.d("asdfg", "-1");
                                            if (q > 0) {
                                                viewHolder.Quantity.setText(decim.format(q - 1).replace(",", "."));
                                            }
                                            setBackground(linearLayout, viewHolder, viewHolder.Guarantee.isChecked(), invoiceLine.getMyInvoice() == null ? null : invoiceLine.getLastCompany());
//                                            ListViewReturnsItemsAdapter.this.notifyDataSetChanged();
//                                            linearLayout.setBackground(originalBackground);
                                            return false;
                                        } else if (event.getX() - historicX > DELTA) {
                                            viewHolder.Quantity.setText(decim.format(q + 1).replace(",", "."));
                                            Log.d("asdfg", "+1");
                                            setBackground(linearLayout, viewHolder, viewHolder.Guarantee.isChecked(), invoiceLine.getMyInvoice() == null ? null : invoiceLine.getLastCompany());
//                                            ListViewReturnsItemsAdapter.this.notifyDataSetChanged();
//                                            linearLayout.setBackgroundDrawable(originalBackground);
                                            return false;
                                        } else {
                                            Log.d("asdfg", "0");
                                            setBackground(linearLayout, viewHolder, viewHolder.Guarantee.isChecked(), invoiceLine.getMyInvoice() == null ? null : invoiceLine.getLastCompany());
//                                            ListViewReturnsItemsAdapter.this.notifyDataSetChanged();
//                                            linearLayout.setBackground(originalBackground);
                                            FragmentManager manager = activity.getFragmentManager();
                                            ItemDetailsFragment frag = new ItemDetailsFragment();
                                            frag.setLine(lines.get(viewHolder.ref));
                                            frag.show(manager, "Items Details Fragment");
                                        }

                                        if (mSwipeRefreshLayout != null) {
                                            mSwipeRefreshLayout.setEnabled(true);
                                        }
                                        break;
                                    } catch (Exception e) {
                                        Log.e("asdfg", e.getMessage(), e);
                                    }
                            }
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                        return false;
                    }
                });

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.ref = position;
            viewHolder.ID = invoiceLine.getMyItem().getID();
            // Populate the data into the template view using the data object
            viewHolder.Code.setText(invoiceLine.getMyItem().getCode());

            viewHolder.Image.setImageResource(R.drawable.logo);
            viewHolder.LastQuantity.setText(invoiceLine.getLastQuantityText());
            viewHolder.Description.setText(invoiceLine.getMyItem().getDescription());
            viewHolder.Price.setText(invoiceLine.getPriceText());
            viewHolder.Quantity.setText(invoiceLine.getQuantityText());
			viewHolder.ManagementCost.setText(invoiceLine.getManageCostText());
            viewHolder.Value.setText(invoiceLine.getValueText());
            String lastdatetext = invoiceLine.getLastDate() + (invoiceLine.getOverdue() == 1 ? " ΠΕΡΑΝ ΤΟΥ ΕΠΙΤΡΕΠΤΟΥ" : "");
            viewHolder.LastDate.setText(lastdatetext);
            viewHolder.LastCompany.setText(invoiceLine.getLastCompany() == null ? "" : invoiceLine.getLastCompany().getDescription() + "-" + invoiceLine.getTypeCode() + "/" + invoiceLine.getDosCode() + "-" + invoiceLine.getDocNumber());
//            Log.d("asdfg", invoiceLine.getLastUpdate() + " " + (invoiceLine.getLastCompany()==null?"":invoiceLine.getLastCompany().getDescription()));

            if ((invoiceLine.getLastDate() == null || invoiceLine.getLastDate().equals("")) && (invoiceLine.getLastCompany() == null) && getContext() instanceof Returns) {
                Log.d("asdfg", "requesting");
                request.getLastPurchase(getContext(), ListViewReturnsItemsAdapter.this, invoiceLine);
            }
            viewHolder.Guarantee.setChecked(invoiceLine.isGuarantee());
            viewHolder.FromCustomer.setEnabled(invoiceLine.isEY());
            viewHolder.FromCustomer.setChecked(invoiceLine.isEY() && invoiceLine.isFromCustomer());
            setBackground(convertView, viewHolder, viewHolder.Guarantee.isChecked(), invoiceLine.getMyInvoice() == null ? null : invoiceLine.getLastCompany());
			AppCompatCheckBox appCompatCheckBox = viewHolder.FromCustomer;
            if (invoiceLine.isEY() && invoiceLine.isFromCustomer()) {
                z = true;
            }
            appCompatCheckBox.setChecked(z);
//            sortAdapter();
            return convertView;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
        return null;
    }

    private void setBackground(View view, ViewHolder holder, Boolean isChecked, Company company) {
        if (isChecked) {
            view.setBackgroundResource(R.color.colorRed);
            setTextColor(false, holder);
        } else if (company == null) {
            view.setBackgroundResource(android.R.color.transparent);
            setTextColor(false, holder);
        } else if (company.getInAppID().equals("1")) {
            view.setBackgroundResource(R.color.colorA);
            setTextColor(true, holder);
        } else {
            view.setBackgroundResource(R.color.colorF);
            setTextColor(true, holder);
        }
    }

    private void setTextColor(boolean isBlack, ViewHolder holder) {
        if (isBlack) {
            holder.Code.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            holder.LastQuantity.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            holder.Description.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            holder.Price.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            holder.Quantity.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            holder.Value.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            holder.LastDate.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            holder.LastCompany.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
			holder.ManagementCost.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));

            /*ContextThemeWrapper newContext = new ContextThemeWrapper(activity, R.style.BlackCheckBox);
            holder.Guarantee = new AppCompatCheckBox(newContext);*/
            /*holder.Guarantee.setAlpha(1);*/
        } else {
            holder.Code.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
            holder.LastQuantity.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
            holder.Description.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
            holder.Price.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
            holder.Quantity.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
            holder.Value.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
            holder.LastDate.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
            holder.LastCompany.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
			holder.ManagementCost.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));

        }

    }


    public void setmSwipeRefreshLayout(SwipeRefreshLayout mSwipeRefreshLayout) {
        this.mSwipeRefreshLayout = mSwipeRefreshLayout;
    }

    public void sortAdapter() {
        ListViewReturnsItemsAdapter.this.sort(new Comparator<InvoiceLineSimple>() {
            @Override
            public int compare(InvoiceLineSimple o1, InvoiceLineSimple o2) {
                try {
                    int value1 = o1.isGuarantee() != o2.isGuarantee() ? o1.isGuarantee() ? 1 : -1 : 0;
//                    Log.d("asdfg", "value1 = "+String.valueOf(value1));
                    if (value1 == 0) {
//                        Log.d("asdfg","o1.company = "+o1.getLastCompany()+" o2.company = "+o2.getLastCompany());
                        if (o1.getLastCompany() != null && o2.getLastCompany() != null) {
                            int value2 = o1.getLastCompany().getInAppID().compareTo(o2.getLastCompany().getInAppID());
//                            Log.d("asdfg","value2 = "+ value2);
                            if (value2 == 0) {
//                                Log.d("asdfg", String.valueOf(o1.getTypeCode().compareTo(o2.getTypeCode())));
                                return o1.getTypeCode().compareTo(o2.getTypeCode());
                            } else {
//                                Log.d("asdfg","hi5");
                                return value2;
                            }
                        }
                    }
                    return value1;
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                    return 0;
                }
            }
        });
    }


}