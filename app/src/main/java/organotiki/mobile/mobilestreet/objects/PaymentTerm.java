package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class PaymentTerm extends RealmObject {
    @PrimaryKey
    private String ID;
    private String Description;

    public PaymentTerm() {
    }

    public PaymentTerm(String ID, String description) {
        this.ID = ID;
        Description = description;
    }

    public String getID() { return ID; }
    public void   setID(String id) { this.ID = id; }
    public String getDescription() { return Description; }
    public void   setDescription(String description) { Description = description; }
}
