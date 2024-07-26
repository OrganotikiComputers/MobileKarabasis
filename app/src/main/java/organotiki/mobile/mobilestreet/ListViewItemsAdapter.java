package organotiki.mobile.mobilestreet;

import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import io.realm.Realm;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;

/**
 * Created by Athan on 02/06/2016.
 */
public class ListViewItemsAdapter extends ArrayAdapter<InvoiceLineSimple> {

    Realm realm;
    GlobalVar gVar;

    private DecimalFormat decim = new DecimalFormat("0.00");
    private ArrayList<InvoiceLineSimple> lines;
    private Activity activity;
    private Rect r = new Rect();
    private float historicX = Float.NaN, historicY = Float.NaN;
    private static int DELTA = 50;

    private class ViewHolder {
        String ID;
        ImageView Image;
        TextView Code;
        TextView Description;
        EditText Price;
        EditText Quantity;
        EditText Discount;
        TextView Value;
        int ref;
    }

    public ListViewItemsAdapter(Activity activity, ArrayList<InvoiceLineSimple> invoiceLines) {
        super(activity, 0, invoiceLines);
        this.lines=invoiceLines;
        this.activity=activity;
        Log.d("asdfg","in-"+lines.size());
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        InvoiceLineSimple invoiceLine = lines.get(position);
        realm = Realm.getDefaultInstance();
        gVar = realm.where(GlobalVar.class).findFirst();
        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_view_items, parent, false);
            viewHolder.Image = (ImageView) convertView.findViewById(R.id.imageView_image);
            viewHolder.Code=(TextView) convertView.findViewById(R.id.code);
            viewHolder.Description = (TextView) convertView.findViewById(R.id.description);
            viewHolder.Price = (EditText) convertView.findViewById(R.id.price);
            viewHolder.Price.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(final Editable s) {
                    try{
                        lines.get(viewHolder.ref).setPrice(Double.parseDouble(s.toString().replace(",", ".").equals("")?"0":s.toString().replace(",", ".")));
                        lines.get(viewHolder.ref).setValue(lines.get(viewHolder.ref).getPrice()*lines.get(viewHolder.ref).getQuantity()*(100-lines.get(viewHolder.ref).getDiscount())/100);
                    }
                    catch (Exception e)
                    {
                        Log.d("asdfg", String.valueOf(e));
                    }
                    viewHolder.Value.setText(decim.format(lines.get(viewHolder.ref).getValue()).replace(",", "."));
                }
            });
            viewHolder.Discount = (EditText) convertView.findViewById(R.id.discount);
            viewHolder.Discount.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(final Editable s) {
                    try{
                        lines.get(viewHolder.ref).setDiscount(Double.parseDouble(s.toString().replace(",", ".").equals("")?"0":s.toString().replace(",", ".")));
                        lines.get(viewHolder.ref).setValue(lines.get(viewHolder.ref).getPrice()*lines.get(viewHolder.ref).getQuantity()*(100-lines.get(viewHolder.ref).getDiscount())/100);
                    }
                    catch (Exception e)
                    {
                        Log.d("asdfg", String.valueOf(e));
                    }
                    viewHolder.Value.setText(decim.format(lines.get(viewHolder.ref).getValue()).replace(",", "."));
                }
            });
            viewHolder.Quantity = (EditText) convertView.findViewById(R.id.quantity);
            viewHolder.Quantity.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(final Editable s) {
                    try{
                        lines.get(viewHolder.ref).setQuantity(Double.parseDouble(s.toString().replace(",", ".").equals("")?"0":s.toString().replace(",", ".")));
                        lines.get(viewHolder.ref).setValue(lines.get(viewHolder.ref).getPrice()*lines.get(viewHolder.ref).getQuantity()*(100-lines.get(viewHolder.ref).getDiscount())/100);
                    }
                    catch (Exception e)
                    {
                        Log.d("asdfg", String.valueOf(e));
                    }
                    viewHolder.Value.setText(decim.format(lines.get(viewHolder.ref).getValue()).replace(",", "."));
                }
            });
            viewHolder.Value = (TextView) convertView.findViewById(R.id.value);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = activity.getFragmentManager();
                    ItemDetailsFragment frag = new ItemDetailsFragment();
                    frag.setLine(lines.get(viewHolder.ref));
                    frag.show(manager, "Items Details Fragment");
                }
            });
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
                                //Log.d("asdfg", String.valueOf(historicX) + "-" + String.valueOf(historicY));
                                break;
                            case MotionEvent.ACTION_MOVE:
                                if (event.getX() - historicX < -DELTA) {
                                    linearLayout.setBackgroundColor(Color.RED);
                                    ((ListView) linearLayout.getParent()).smoothScrollBy(0, 0);
                                    return true;
                                } else if (event.getX() - historicX > DELTA) {
                                    linearLayout.setBackgroundColor(Color.GREEN);
                                    ((ListView) linearLayout.getParent()).smoothScrollBy(0, 0);
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
                                        //Log.d("asdfg", "-1");
                                        if (q > 0) {
                                            viewHolder.Quantity.setText(decim.format(q - 1).replace(",", "."));
                                        }
                                        linearLayout.setBackgroundColor(Color.alpha(808080));
                                        return true;
                                    } else if (event.getX() - historicX > DELTA) {
                                        viewHolder.Quantity.setText(decim.format(q + 1).replace(",", "."));
                                        //Log.d("asdfg", "+1");
                                        linearLayout.setBackgroundColor(Color.alpha(808080));
                                        return true;
                                    } else {
                                        //Log.d("asdfg", "0");
                                        linearLayout.setBackgroundColor(Color.alpha(808080));
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
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.ref = position;

        // Populate the data into the template view using the data object
        viewHolder.Code.setText(invoiceLine.getMyItem().getCode().toString());
        String filepath = Environment.getExternalStorageDirectory().getPath() + "/Pictures/" + viewHolder.Code.getText()+".jpg";
        File file = new File(filepath);
        viewHolder.Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentManager manager = activity.getFragmentManager();
                    ImageFragment frag = new ImageFragment();
                    frag.setImageCode(lines.get(position).getMyItem().getCode());
                    frag.show(manager, "Image Fragment");
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
            }
        });
        if(file.exists()){

            try {
                ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getContext()));

//                ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(getContext());
//                config.threadPriority(Thread.NORM_PRIORITY - 2);
//                config.denyCacheImageMultipleSizesInMemory();
//                config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
//                config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
//                config.tasksProcessingOrder(QueueProcessingType.LIFO);
//                config.writeDebugLogs(); // Remove for release app
//
//                 //Initialize ImageLoader with configuration.
//                ImageLoader.getInstance().init(config.build());
                ImageLoader imageLoader = ImageLoader.getInstance();
//                imageLoader.init(ImageLoaderConfiguration.createDefault(getContext()));
//                imageLoader.displayImage(gVar.getOnlineIP()+"GetImage/"+viewHolder.Code,viewHolder.Image);
                imageLoader.displayImage("file://"+filepath,viewHolder.Image);
                Log.d("asdfg", "file://"+filepath);
//            viewHolder.Image.setImageURI(Uri.parse(filepath));
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
        }else{
            try {
                viewHolder.Image.setImageResource(R.drawable.logoo);
                Log.d("asdfg", filepath+" does not exist");
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
        }
        viewHolder.Description.setText(invoiceLine.getMyItem().getDescription());
        viewHolder.Price.setText(invoiceLine.getPriceText());
        viewHolder.Discount.setText(invoiceLine.getDiscountText());
        viewHolder.Quantity.setText(invoiceLine.getQuantityText());
        viewHolder.Value.setText(invoiceLine.getValueText());
        // Return the completed view to render on screen
        return convertView;
    }
}
