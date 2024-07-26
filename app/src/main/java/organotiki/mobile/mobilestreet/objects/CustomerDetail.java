package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 13/7/2016.
 */
public class CustomerDetail extends RealmObject {

//    @PrimaryKey
    private String Header;
    private String Value;

    public CustomerDetail() {
    }

    public CustomerDetail(String header, String value) {
        Header = header;
        Value = value;
    }

    public String getHeader() {
        return Header;
    }

    public void setHeader(String header) {
        Header = header;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }
}
