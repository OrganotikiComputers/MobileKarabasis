package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 1/7/2016.
 */
public class PaymentType extends RealmObject {

    @PrimaryKey
    private String ID;
    private String Code;
    private String Description;
    private Boolean isCheck;

    public PaymentType() {
    }

    public PaymentType(String ID, String code, String description, Boolean isCheck) {
        this.ID = ID;
        Code = code;
        Description = description;
        this.isCheck = isCheck;
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

    public Boolean getCheck() {
        return isCheck;
    }

    public void setCheck(Boolean check) {
        isCheck = check;
    }
}
