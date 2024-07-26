package organotiki.mobile.mobilestreet.objects;

import android.util.Log;

import java.text.DecimalFormat;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 1/7/2016.
 */
public class FInvoiceLine extends RealmObject {

    @PrimaryKey
    private String ID;
    private FInvoice myFInvoice;
    private Company myCompany;
    private boolean isEY = true;
    private Integer Type;
    private Bank myBank;
    private String ExDate;
    private String Number;
    private Double Value = 0.0;

    public FInvoiceLine() {
    }

    public FInvoiceLine(String ID, FInvoice myFInvoice, Company myCompany, Integer type) {
        this.ID = ID;
        this.myFInvoice = myFInvoice;
        this.myCompany = myCompany;
        Type = type;
    }

    public FInvoiceLine(String ID, FInvoice myFInvoice, Integer type, Bank myBank, Company myCompany, String number, String exDate, Double value) {
        this.ID = ID;
        this.myFInvoice = myFInvoice;
        this.myCompany = myCompany;
        Type = type;
        this.myBank = myBank;
        Number = number;
        ExDate = exDate;
        Value = value;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public FInvoice getMyFInvoice() {
        return myFInvoice;
    }

    public void setMyFInvoice(FInvoice myFInvoice) {
        this.myFInvoice = myFInvoice;
    }

    public Integer getType() {
        return Type;
    }

    public void setType(Integer type) {
        Type = type;
    }

    public Bank getMyBank() {
        return myBank;
    }

    public void setMyBank(Bank myBank) {
        this.myBank = myBank;
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public String getExDate() {
        return ExDate;
    }

    public void setExDate(String exDate) {
        ExDate = exDate;
    }

    public Double getValue() {
        return Value;
    }

    public void setValue(Double value) {
        Value = value;
    }

    public Company getMyCompany() {
        return myCompany;
    }

    public void setMyCompany(Company myCompany) {
        this.myCompany = myCompany;
    }

    public boolean isEY() {
        return isEY;
    }

    public void setEY(boolean EY) {
        isEY = EY;
    }

    public String getValueText() {
        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(Value).replace(",", ".");
    }

    public void setValueText(String value) {
        Value = value.equals("")?0.0:Double.parseDouble(value.replace(",", "."));
        Log.d("asdfg", String.valueOf(Value));
    }
}
