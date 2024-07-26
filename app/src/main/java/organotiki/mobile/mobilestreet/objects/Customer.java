package organotiki.mobile.mobilestreet.objects;

import java.text.DecimalFormat;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class Customer extends RealmObject {

    @PrimaryKey
    private String ID;
    private String Code;
    private String Name;
    private String TIN;
    private String TaxLevel;
    private String Notes;
    private String Message;
    private Double Debit=0.0;
    private Double Credit=0.0;
    private Double Balance=0.0;
    private String B2bCode;
    private boolean isNew;

    public Customer() {
    }

    public Customer(String ID, String code, String name, String TIN, String taxLevel, String notes, String message, Double debit, Double credit, Double balance, boolean isNew,String B2bCode) {
        this.ID = ID;
        Code = code;
        Name = name;
        this.TIN = TIN;
        TaxLevel = taxLevel;
        Notes = notes;
        Message = message;
        Debit = debit;
        Credit = credit;
        Balance = balance;
        this.B2bCode = B2bCode;
        this.isNew = isNew;
    }

    public Customer(String ID, String code, String name, String TIN, String taxLevel, boolean isnew,String B2bCode) {
        this.ID = ID;
        Code = code;
        Name = name;
        this.TIN = TIN;
        TaxLevel = taxLevel;
        this.B2bCode = B2bCode;
        isNew = isnew;
    }

    public String getB2bCode() {
        return B2bCode;
    }

    public void setB2bCode(String b2bCode) {
        B2bCode = b2bCode;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getTIN() {
        return TIN;
    }

    public void setTIN(String TIN) {
        this.TIN = TIN;
    }

    public String getTaxLevel() {
        return TaxLevel;
    }

    public void setTaxLevel(String taxLevel) {
        TaxLevel = taxLevel;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public Double getCredit() {
        return Credit;
    }

    public void setCredit(Double credit) {
        Credit = credit;
    }

    public Double getDebit() {
        return Debit;
    }

    public void setDebit(Double debit) {
        Debit = debit;
    }

    public Double getBalance() {
        return Balance;
    }

    public void setBalance(Double balance) {
        Balance = balance;
    }

    public String getCreditText() {
        DecimalFormat formatter = new DecimalFormat("#,###,##0.00");
        return formatter.format(Credit==null?0.0:Credit).replace(",", ".");
    }

    public void setCreditText(String credit) {
        Credit = Double.parseDouble(credit);
    }

    public String getDebitText() {
        DecimalFormat formatter = new DecimalFormat("#,###,##0.00");
        return formatter.format(Debit==null?0.0:Debit).replace(",", ".");
    }

    public void setDebit(String debit) {
        Debit = Double.parseDouble(debit);
    }

    public String getBalanceText() {
        DecimalFormat formatter = new DecimalFormat("#,###,##0.00");
        return formatter.format(Balance==null?0.0:Balance).replace(",", ".");
    }

    public void setBalance(String balance) {
        Balance = Double.parseDouble(balance);
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
