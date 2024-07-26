package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 12/7/2016.
 */
public class OnlineReportType extends RealmObject {

    @PrimaryKey
    private String ID;
    private String Code;
    private String Description;
    private Boolean isFromDate;
    private Boolean isToDate;
    private Boolean isCustomer;
    private Boolean isSalesman;
    private Boolean isCompany;

    public OnlineReportType() {
    }

    public OnlineReportType(String ID, String code, String description, Boolean isFromDate, Boolean isToDate, Boolean isCustomer, Boolean isSalesman, Boolean isCompany) {
        this.ID = ID;
        Code = code;
        Description = description;
        this.isFromDate = isFromDate;
        this.isToDate = isToDate;
        this.isCustomer = isCustomer;
        this.isSalesman = isSalesman;
        this.isCompany = isCompany;
    }

    public Boolean getCompany() {
        return isCompany;
    }

    public void setCompany(Boolean company) {
        isCompany = company;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public Boolean getFromDate() {
        return isFromDate;
    }

    public void setFromDate(Boolean fromDate) {
        isFromDate = fromDate;
    }

    public Boolean getToDate() {
        return isToDate;
    }

    public void setToDate(Boolean toDate) {
        isToDate = toDate;
    }

    public Boolean getCustomer() {
        return isCustomer;
    }

    public void setCustomer(Boolean customer) {
        isCustomer = customer;
    }

    public Boolean getSalesman() {
        return isSalesman;
    }

    public void setSalesman(Boolean salesman) {
        isSalesman = salesman;
    }
}
