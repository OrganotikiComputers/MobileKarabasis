package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 30/5/2016.
 */
public class AlternateCode extends RealmObject {

    @PrimaryKey
    private String ID;
    private Item myItem;
    private String AltCode;
    private Double Items;

    public AlternateCode() {
    }

    public AlternateCode(String ID, Item myItem, String altCode, Double items) {
        this.ID = ID;
        this.myItem = myItem;
        AltCode = altCode;
        Items = items;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Item getMyItem() {
        return myItem;
    }

    public void setMyItem(Item myItem) {
        this.myItem = myItem;
    }

    public String getAltCode() {
        return AltCode;
    }

    public void setAltCode(String altCode) {
        AltCode = altCode;
    }

    public Double getItems() {
        return Items;
    }

    public void setItems(Double items) {
        Items = items;
    }
}
