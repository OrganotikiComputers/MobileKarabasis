package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 1/7/2016.
 */
public class Bank extends RealmObject {

    @PrimaryKey
    private String ID;
    private String Code;
    private String Description;

    public Bank() {
    }

    public Bank(String ID, String code, String description) {
        this.ID = ID;
        Code = code;
        Description = description;
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
}
