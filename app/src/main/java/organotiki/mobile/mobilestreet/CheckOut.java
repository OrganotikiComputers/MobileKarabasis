package organotiki.mobile.mobilestreet;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.InvoiceLine;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class CheckOut extends AppCompatActivity implements Communicator {

    Realm realm;
    GlobalVar gVar;
    ArrayList<InvoiceLine> lines;
    DecimalFormat decim = new DecimalFormat("0.00");
    DecimalFormat formatter = new DecimalFormat("#,###,##0.00");
    DecimalFormat decimq = new DecimalFormat("0.000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_check_out);

        realm = Realm.getDefaultInstance();
        gVar = realm.where(GlobalVar.class).findFirst();

        Toolbar toolbar = (Toolbar) findViewById(R.id.checkOutBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        lines = new ArrayList<>();
        lines.addAll(realm.where(InvoiceLine.class).equalTo("myInvoice.ID",gVar.getMyInvoice().getID()).findAll());

        FillCheckOut();*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.complete_check_out:
                try {
                    doSend();
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                return false;
            case android.R.id.home:
                finish();
                return true;

        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /*getMenuInflater().inflate(R.menu.check_out_menu, menu);
        if (gVar.getMyInvoice().isFinal()){
            MenuItem item = menu.findItem(R.id.complete_check_out);
            item.setVisible(false);
            //menu.getItem(0).setEnabled(false); // here pass the index of save menu item
        }*/
        return true;
    }

    private void doEmail() {

        /*AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                CheckOut.this);

        alertDialog.setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    //dbHandler.setEmail(dbHandler.getInvoiceID(),1);
                    Toast.makeText(getBaseContext(),"Θα αποσταλλεί E-mail στον πελάτη.",Toast.LENGTH_SHORT).show();
                    doPrint();
                } catch (Exception e) {
                    Log.e("asdfg",e.getMessage(),e);
                }
            }
        });

        alertDialog.setNegativeButton("Όχι", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    //dbHandler.setEmail(dbHandler.getInvoiceID(),0);
                    Toast.makeText(getBaseContext(),"Δεν θα αποσταλλεί E-mail στον πελάτη.",Toast.LENGTH_SHORT).show();
                    doPrint();
                } catch (Exception e) {
                    Log.e("asdfg",e.getMessage(),e);
                }
            }
        });


        alertDialog.setMessage("Θέλετε να στείλετε E-mail στον πελάτη;");
        alertDialog.setTitle("Mobile Store");
        alertDialog.show();*/
    }

    private void doPrint() {

        /*AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                CheckOut.this);

        alertDialog.setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            gVar.getMyInvoice().setPrint(true);
                        }
                    });
                    Toast.makeText(getBaseContext(),"Το παραστατικό θα εκτυπωθεί.",Toast.LENGTH_SHORT).show();
                    saveDocument();
                } catch (Exception e) {
                    Log.e("asdfg",e.getMessage(),e);
                }
            }
        });

        alertDialog.setNegativeButton("Όχι", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Toast.makeText(getBaseContext(),"Το παραστατικό δεν θα εκτυπωθεί.",Toast.LENGTH_SHORT).show();
                    saveDocument();
                } catch (Exception e) {
                    Log.e("asdfg",e.getMessage(),e);
                }
            }
        });

        alertDialog.setMessage("Θέλετε να εκτυπώσετε το παραστατικό?");
        alertDialog.setTitle("Mobile Store");
        alertDialog.show();*/
    }

    private void doSend() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                CheckOut.this);

        alertDialog.setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    doPrint();
                } catch (Exception e) {
                    Log.e("asdfg",e.getMessage(),e);
                }
            }
        });

        alertDialog.setNegativeButton("Όχι", null);

        alertDialog.setMessage("Θέλετε να καταχωρείσετε το παραστατικό?");
        alertDialog.setTitle("Mobile Store");
        alertDialog.show();
    }

    private void saveDocument() {
        /*if(isNetworkAvailable()){
            Log.d("asdfg","we got network");
        }else{

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    gVar.getMyInvoice().setFinal(true);
                }
            });
            Toast.makeText(this,"Δεν βρέθηκε σύνδεση στο Διαδίκτυο, αποθηκεύτηκε τοπικά.",Toast.LENGTH_SHORT).show();
            Intent i = new Intent(CheckOut.this,MainScreen.class);
            startActivity(i);
        }*/
    }

    private void FillCheckOut(){
        /*try {


            final MyListAdapter myListAdapter = new MyListAdapter();
            ListView listView = (ListView)findViewById(R.id.listViewCheckOutItems);
            listView.setAdapter(myListAdapter);

            Customer customer = gVar.getMyInvoice().getMyCustomer();
            Address address = gVar.getMyInvoice().getMyAddress();
            TextView tvCusCode = (TextView) findViewById(R.id.textView_customer_code);
            tvCusCode.setText("Κωδικός: " + customer.getCode());
            TextView tvCusName = (TextView) findViewById(R.id.textView_customer_name);
            tvCusName.setText("Όνομα: " + customer.getName());
            TextView tvCusTIN = (TextView) findViewById(R.id.textView_customer_tin);
            tvCusTIN.setText("ΑΦΜ: " + customer.getTIN());
            TextView tvCusAddress = (TextView) findViewById(R.id.textView_customer_address);
            tvCusAddress.setText("Διεύθυνση: " + address.getStreet());
            TextView tvCusCity = (TextView) findViewById(R.id.textView_customer_city);
            tvCusCity.setText("Πόλη: " + address.getCity());
            TextView tvCusPostalCode = (TextView) findViewById(R.id.textView_customer_postalcode);
            tvCusPostalCode.setText("Τ.Κ.: " + address.getPostalCode());
            String phones="";
            phones += address.getPhone1() != null && !address.getPhone1().isEmpty() ? address.getPhone1().replaceFirst("\\s+$", "") + " - " : "";
            phones += address.getPhone2() != null && !address.getPhone2().isEmpty() ? address.getPhone2().replaceFirst("\\s+$", "") + " - " : "";
            phones = phones.length() > 3 ? phones.substring(0, phones.length() - 3) : "";
            TextView tvCusPhone = (TextView) findViewById(R.id.textView_customer_phone);
            tvCusPhone.setText("Τηλέφωνα: " + phones);
            TextView tvCusMobile = (TextView) findViewById(R.id.textView_customer_mobile);
            tvCusMobile.setText("Κινητό: " + address.getMobile());
            TextView tvCusEmail = (TextView) findViewById(R.id.textView_customer_email);
            tvCusEmail.setText("E-mail: " + address.getEmail());

            TextView tvEntryType= (TextView) findViewById(R.id.textView_Entry_Type);
            tvEntryType.setText(gVar.getMyInvoice().getMyType().getDescription()+" - "+gVar.getMyInvoice().getMyPayment().getDescription());

            final TextView textViewTotal = (TextView) findViewById(R.id.textView_total);
            textViewTotal.setText("Σύνολο: "+ formatter.format(gVar.getMyInvoice().getTotal())+"€");

            final TextView textViewGeneralNotes = (TextView) findViewById(R.id.textView_generalNotes);
            String txt = getString(R.string.generalNotes) + ": " + gVar.getMyInvoice().getGeneralNotes();
            textViewGeneralNotes.setText(txt);

        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }*/
    }


    @Override
    public void respondCustomerSearch(Customer customer, Address address) {

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
    public void respondVolleyRequestFinished(Integer position, JSONObject jsonArray) {

    }

    @Override
    public void respondDate(Integer position, int year, int month, int day) {

    }

    @Override
    public void respondCompanySite() {

    }

    @Override
    public void respondRecentPurchases(ArrayList<InvoiceLineSimple> sLines) {

    }

    /*private class MyListAdapter extends BaseAdapter {

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
                    LayoutInflater inflater = CheckOut.this.getLayoutInflater();
                    convertView = inflater.inflate(R.layout.list_view_check_out, null);
//                    convertView.setClickable(true);
//                    convertView.setFocusable(true);
                    final View finalConvertView = convertView;
                    convertView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(finalConvertView.getWindowToken(), 0);
                        }

                    });
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentManager manager = getFragmentManager();
                            ItemDetailsFragment frag = new ItemDetailsFragment();
                            frag.setLine(lines.get(holder.ref));
                            frag.isReadOnly(true);
                            frag.show(manager, "Item Details Fragment");
                        }
                    });
                    holder.Image = (ImageView) convertView.findViewById(R.id.imageView_image);
                    holder.Code = (TextView) convertView.findViewById(R.id.cusCode);
                    holder.Description = (TextView) convertView.findViewById(R.id.description);
                    holder.Price = (TextView) convertView.findViewById(R.id.price);
                    holder.Quantity = (TextView) convertView.findViewById(R.id.quantity);
                    holder.Discount = (TextView) convertView.findViewById(R.id.discount);
                    holder.Value = (TextView) convertView.findViewById(R.id.value);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.ref = position;
                holder.ID = lines.get(position).getID();

                String filepath = Environment.getExternalStorageDirectory().getPath() + "/Pictures/" + lines.get(position).getMyItem().getCode()+".jpg";
                File file = new File(filepath);
                if(file.exists()){
                    Log.d("asdfg", filepath);
                    holder.Image.setImageURI(Uri.parse(filepath));
                }else{
                    holder.Image.setImageResource(R.drawable.logoo);
                    Log.d("asdfg", filepath+" does not exist");
                }
                holder.Image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            FragmentManager manager = getFragmentManager();
                            ImageFragment frag = new ImageFragment();
                            frag.setImageCode(lines.get(position).getMyItem().getCode());
                            frag.show(manager, "Image Fragment");
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });

                holder.Code.setText(lines.get(position).getMyItem().getCode());
                holder.Description.setText(lines.get(position).getMyItem().getDescription());
                holder.Price.setText(decim.format(lines.get(position).getPrice()).replace(",", "."));
                holder.Quantity.setText(decim.format(lines.get(position).getQuantity()).replace(",", "."));
                holder.Discount.setText(decim.format(lines.get(position).getDiscount()).replace(",", "."));
                holder.Value.setText(decim.format(lines.get(holder.ref).getValue()).replace(",", "."));


            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }

            return convertView;
        }

        private class ViewHolder {
            String ID;
            ImageView Image;
            TextView Code;
            TextView Description;
            TextView Price;
            TextView Quantity;
            TextView Discount;
            TextView Value;
            int ref;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean checkIfExists(String itemID,String invoiceID){

        RealmQuery<InvoiceLine> query = realm.where(InvoiceLine.class)
                .equalTo("myItem.ID", itemID).equalTo("myInvoice.ID", invoiceID);

        return query.count() != 0;
    }*/
}