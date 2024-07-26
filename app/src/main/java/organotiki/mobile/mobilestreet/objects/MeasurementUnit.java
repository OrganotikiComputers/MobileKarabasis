package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 31/5/2016.
 */
public class MeasurementUnit extends RealmObject {

    @PrimaryKey
    private String ID;
    private String Code;


    public MeasurementUnit() {
    }


    public MeasurementUnit(String code, String iD) {
        this.Code = code;
        this.ID = iD;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        this.Code = code;
    }

    public String getID() {
        return ID;
    }

    public void setID(String iD) {
        this.ID = iD;
    }
}
