package organotiki.mobile.mobilestreet.objects;

import android.util.Log;

import java.text.DecimalFormat;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import organotiki.mobile.mobilestreet.MyApplication;

/**
 * Created by Thanasis on 1/7/2016.
 */
public class FInvoice extends RealmObject {

    @PrimaryKey
    private String ID;
    private String DeviceID;
    private Customer myCustomer;
    private Address myAddress;
    private String Date;
    private String Time;
    private User myUser;
    private String Username;
    private Double Cash1 = 0.0;
    private Double Cash2 = 0.0;
    private Double Cash3 = 0.0;
    private Double Cash4 = 0.0;
    private Double CusBalanceAEY = 0.0;
    private Double CusBalanceASY = 0.0;
    private Double CusBalanceFEY = 0.0;
    private Double CusBalanceFSY = 0.0;
    private String DocNumber1 = "";
    private String DocNumber2 = "";
    private String DocNumber3 = "";
    private String DocNumber4 = "";
    private String Notes1 = "";
    private String Notes2 = "";
    private String Notes3 = "";
    private String Notes4 = "";
    private String Notes;
    private Double Total = 0.0;
    private boolean isFinal=false;
    private boolean isSent=false;


    public FInvoice() {
    }

    public FInvoice(String ID, String deviceID, String date, String time, User myUser, String username) {
        this.ID = ID;
        DeviceID = deviceID;
        Date = date;
        Time = time;
        this.myUser = myUser;
        Username = username;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Double getTotal() {
        return Total;
    }

    public void setTotal(Double total) {
        Total = total;
    }



    public void setTotal(){
        try {
            Total = (Double) MyApplication.getRealm().where(FInvoiceLine.class).equalTo("myFInvoice.ID", ID).sum("Value")+Cash1+Cash2+Cash3+Cash4;
//            Log.d("asdfg", "count: "+String.valueOf(MyApplication.getRealm().where(FInvoiceLine.class).equalTo("myEInvoice.ID", ID).count()));
//            Log.d("asdfg", "subtotal: "+String.valueOf(MyApplication.getRealm().where(FInvoiceLine.class).equalTo("myEInvoice.ID", ID).findFirst().getValue()));
        } catch (Exception e) {
            Log.e("asdfg",e.getMessage(),e);
        }
    }

    public String getTotalText() {
        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(Total).replace(",", ".");
    }

    public String getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(String deviceID) {
        DeviceID = deviceID;
    }

    public Customer getMyCustomer() {
        return myCustomer;
    }

    public void setMyCustomer(Customer myCustomer) {
        this.myCustomer = myCustomer;
    }

    public Address getMyAddress() {
        return myAddress;
    }

    public void setMyAddress(Address myAddress) {
        this.myAddress = myAddress;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public User getMyUser() {
        return myUser;
    }

    public void setMyUser(User myUser) {
        this.myUser = myUser;
    }

    public Double getCash1() {
        return Cash1;
    }

    public void setCash1(Double cash1) {
        Cash1 = cash1;
    }

    public Double getCash2() {
        return Cash2;
    }

    public void setCash2(Double cash2) {
        Cash2 = cash2;
    }

    public Double getCash3() {
        return Cash3;
    }

    public void setCash3(Double cash3) {
        Cash3 = cash3;
    }

    public Double getCash4() {
        return Cash4;
    }

    public void setCash4(Double cash4) {
        Cash4 = cash4;
    }

    public String getCash1Text() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(Cash1).replace(",", ".");
    }

    public void setCash1Text(String cash1) {
        Cash1 = Double.parseDouble(cash1.equals("") || cash1.equals("-")?"0":cash1.replace(",", "."));
    }

    public String getCash2Text() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(Cash2).replace(",", ".");
    }

    public void setCash2Text(String cash2) {
        Cash2 = Double.parseDouble(cash2.equals("") || cash2.equals("-")?"0":cash2.replace(",", "."));
    }

    public String getCash3Text() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(Cash3).replace(",", ".");
    }

    public void setCash3Text(String cash3) {
        Cash3 = Double.parseDouble(cash3.equals("") || cash3.equals("-")?"0":cash3.replace(",", "."));
    }

    public String getCash4Text() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(Cash4).replace(",", ".");
    }

    public void setCash4Text(String cash4) {
        Cash4 = Double.parseDouble(cash4.equals("") || cash4.equals("-")?"0":cash4.replace(",", "."));
    }

    public Double getCusBalanceAEY() {
        return CusBalanceAEY;
    }

    public void setCusBalanceAEY(Double cusBalanceAEY) {
        CusBalanceAEY = cusBalanceAEY;
    }

    public Double getCusBalanceASY() {
        return CusBalanceASY;
    }

    public void setCusBalanceASY(Double cusBalanceASY) {
        CusBalanceASY = cusBalanceASY;
    }

    public Double getCusBalanceFEY() {
        return CusBalanceFEY;
    }

    public void setCusBalanceFEY(Double cusBalanceFEY) {
        CusBalanceFEY = cusBalanceFEY;
    }

    public Double getCusBalanceFSY() {
        return CusBalanceFSY;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public void setCusBalanceFSY(Double cusBalanceFSY) {
        CusBalanceFSY = cusBalanceFSY;
    }

    public String getCusBalanceAEYText() {
        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(CusBalanceAEY).replace(",", ".");
    }
    public String getCusBalanceASYText() {
        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(CusBalanceASY).replace(",", ".");
    }
    public String getCusBalanceFEYText() {
        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(CusBalanceFEY).replace(",", ".");
    }
    public String getCusBalanceFSYText() {
        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(CusBalanceFSY).replace(",", ".");
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getOverallBalanceText(){
        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(CusBalanceAEY+CusBalanceASY+CusBalanceFEY+CusBalanceFSY).replace(",", ".");
    }

    public String getDocNumber1() {
        return DocNumber1;
    }

    public void setDocNumber1(String docNumber1) {
        DocNumber1 = docNumber1;
    }

    public String getDocNumber2() {
        return DocNumber2;
    }

    public void setDocNumber2(String docNumber2) {
        DocNumber2 = docNumber2;
    }

    public String getDocNumber3() {
        return DocNumber3;
    }

    public void setDocNumber3(String docNumber3) {
        DocNumber3 = docNumber3;
    }

    public String getDocNumber4() {
        return DocNumber4;
    }

    public void setDocNumber4(String docNumber4) {
        DocNumber4 = docNumber4;
    }

    public String getNotes1() {
        return Notes1;
    }

    public void setNotes1(String notes1) {
        Notes1 = notes1;
    }

    public String getNotes2() {
        return Notes2;
    }

    public void setNotes2(String notes2) {
        Notes2 = notes2;
    }

    public String getNotes3() {
        return Notes3;
    }

    public void setNotes3(String notes3) {
        Notes3 = notes3;
    }

    public String getNotes4() {
        return Notes4;
    }

    public void setNotes4(String notes4) {
        Notes4 = notes4;
    }
}
