package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class ItemCategory extends RealmObject {

    @PrimaryKey
    private String ID;
    private String Name;


    public ItemCategory() {
    }

    public ItemCategory(String ID, String name) {
        this.ID = ID;
        Name = name;
    }

    public String getID() { return ID; }
    public void   setID(String id) { this.ID = id; }
    public String getName() { return Name; }
    public void   setName(String name) { Name = name; }
}