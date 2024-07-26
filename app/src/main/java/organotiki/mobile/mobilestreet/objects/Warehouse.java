package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 1/7/2016.
 */
public class Warehouse extends RealmObject {

    @PrimaryKey
    private String ID;
    private String Code;
    private String Notes;
    private Company myCompany;

    public Warehouse() {
    }

    public Warehouse(String ID, String code, String notes, Company myCompany) {
        this.ID = ID;
        Code = code;
        Notes = notes;
        this.myCompany = myCompany;
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

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public Company getMyCompany() {
        return myCompany;
    }

    public void setMyCompany(Company myCompany) {
        this.myCompany = myCompany;
    }
}
