package organotiki.mobile.mobilestreet.objects;

import android.util.Log;

import java.text.DecimalFormat;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import organotiki.mobile.mobilestreet.MyApplication;

/**
 * Created by Thanasis on 24/8/2016.
 */
public class FTransaction extends RealmObject {

    @PrimaryKey
    private String ID;
    private String Notes;
    private Double CusBalanceAEY;
    private Double CusBalanceASY;
    private Double CusBalanceFEY;
    private Double CusBalanceFSY;
    private Double Total=0.0;
    private boolean isMail=false;
    private boolean isPrint=false;

    public FTransaction() {
        setTotal(0.0);
        setisMail(false);
        setisPrint(false);
    }



    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public boolean isMail() {
        return isMail;
    }

    public void setMail(boolean mail) {
        isMail = mail;
    }

    public boolean isPrint() {
        return isPrint;
    }

    public void setPrint(boolean print) {
        isPrint = print;
    }

    public void setisMail(boolean ismail) {
        this.isMail = ismail;
    }

    public void setisPrint(boolean isprint) {
        this.isPrint = isprint;
    }
    public Double getTotal() {
        return Total;
    }

    public void setTotal(Double total) {
        Total = total;
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

    public void setCusBalanceFSY(Double cusBalanceFSY) {
        CusBalanceFSY = cusBalanceFSY;
    }

    public void setTotal(){
        try {
            Total = (Double) MyApplication.getRealm().where(FInvoice.class).equalTo("myFTransaction.ID", ID).sum("Total");
        } catch (Exception e) {
            Log.e("asdfg",e.getMessage(),e);
        }
    }

    public String getTotalText() {
        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(Total).replace(",", ".");
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
}
