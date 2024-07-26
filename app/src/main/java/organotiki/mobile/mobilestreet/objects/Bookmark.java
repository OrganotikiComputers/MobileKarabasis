package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 11/1/2017.
 */

public class Bookmark extends RealmObject {

    @PrimaryKey
    private String ID;
    private String Name;
    private String Category;

    public Bookmark() {
    }

    public Bookmark(String ID, String name, String category) {
        this.ID = ID;
        Name = name;
        Category = category;
    }

    public String getID() { return ID; }
    public void   setID(String id) { this.ID = id; }
    public String getName() { return Name; }
    public void   setName(String name) { Name = name; }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }
}
