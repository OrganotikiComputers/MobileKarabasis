package organotiki.mobile.mobilestreet.objects;

import java.text.DecimalFormat;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 1/7/2016.
 */
public class Company extends RealmObject {

    @PrimaryKey
    private String ID;
    private String Code;
    private String Description;
    private String InAppID;
    private String TIN;

    public Company() {
    }

    public Company(String ID, String code, String description, String inAppID,String tin) {
        this.ID = ID;
        Code = code;
        Description = description;
        InAppID = inAppID;
        TIN = tin;
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

    public String getInAppID() {
        return InAppID;
    }

    public void setInAppID(String inAppID) {
        InAppID = inAppID;
    }

    public String getTIN() {
        return TIN;
    }

    public void setTIN(String TIN) {
        this.TIN = TIN;
    }
}
