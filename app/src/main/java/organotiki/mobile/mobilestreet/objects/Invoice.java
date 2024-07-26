package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import organotiki.mobile.mobilestreet.MyApplication;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class Invoice  extends  RealmObject{

    @PrimaryKey
    private String ID;
    private InvoiceType myType;
    private PaymentTerm myPayment;
    private String DeviceID;
    private User myUser;
    private String Username;
    private Customer myCustomer;
    private Address myAddress;
    private String Date;
    private String Time;
    private String Notes;
    private Double TotalAEY = 0.0;
    private Double TotalASY = 0.0;
    private Double TotalFEY = 0.0;
    private Double TotalFSY = 0.0;
    private Double Total = 0.0;
    private boolean isSent=false;
    private boolean isFinal=false;
    private boolean isReturns=false;
	private Double ManagementCostPercent;



    public Invoice() {
    }

    public Invoice(String ID, String deviceID, User myUser, String username, String date, String time, boolean isReturns) {
        this.ID = ID;
        DeviceID = deviceID;
        this.myUser = myUser;
        Username = username;
        Date = date;
        Time = time;
        this.isReturns = isReturns;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public InvoiceType getMyType() {
        return myType;
    }

    public void setMyType(InvoiceType myType) {
        this.myType = myType;
    }

    public PaymentTerm getMyPayment() {
        return myPayment;
    }

    public void setMyPayment(PaymentTerm myPayment) {
        this.myPayment = myPayment;
    }

    public Double getTotal() {
        return Total;
    }

    public void setTotal(Double total) {
        Total = total;
    }

    public void setTotal() {
        Total = (Double) MyApplication.getRealm().where(InvoiceLine.class).equalTo("myInvoice.ID", ID).equalTo("isGuarantee",false).sum("Value");
    }

    public String getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(String deviceID) {
        DeviceID = deviceID;
    }

    public User getMyUser() {
        return myUser;
    }

    public void setMyUser(User myUser) {
        this.myUser = myUser;
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

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public boolean isReturns() {
        return isReturns;
    }

    public void setReturns(boolean returns) {
        isReturns = returns;
    }

    public Double getTotalAEY() {
        return TotalAEY;
    }

    public void setTotalAEY(Double totalAEY) {
        TotalAEY = totalAEY;
    }

    public Double getTotalASY() {
        return TotalASY;
    }

    public void setTotalASY(Double totalASY) {
        TotalASY = totalASY;
    }

    public Double getTotalFEY() {
        return TotalFEY;
    }

    public void setTotalFEY(Double totalFEY) {
        TotalFEY = totalFEY;
    }

    public Double getTotalFSY() {
        return TotalFSY;
    }

    public void setTotalFSY(Double totalFSY) {
        TotalFSY = totalFSY;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }
	
	public Double getManagementCostPercent() {
        return ManagementCostPercent;
    }

    public void setManagementCostPercent(Double managementCostPercent) {
        ManagementCostPercent = managementCostPercent;
    }
}
