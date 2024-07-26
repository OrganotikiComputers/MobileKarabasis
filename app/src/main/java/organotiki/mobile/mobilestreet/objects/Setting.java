package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 1/7/2016.
 */
public class Setting extends RealmObject {

    @PrimaryKey
    private String ID;
    private String System;
    private String Value;
    private String Code;

    public Setting() {
    }

    public Setting(String ID, String code, String value,String system) {
        this.ID = ID;
        Code = code;
        Value = value;
		System=system;
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

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }
	
	 public String getSystem() {
        return System;
    }

    public void setSystem(String system) {
        System = system;
    }
}
