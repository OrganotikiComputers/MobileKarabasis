package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 6/3/2017.
 */

public class BrowserCityFilter extends RealmObject {

    @PrimaryKey
    private String ID;
    private User myUser;
    private String City;

    public BrowserCityFilter() {
    }

    public BrowserCityFilter(String ID, User myUser, String city) {
        this.ID = ID;
        this.myUser = myUser;
        City = city;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public User getMyUser() {
        return myUser;
    }

    public void setMyUser(User myUser) {
        this.myUser = myUser;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }
}
