package organotiki.mobile.mobilestreet.objects;

import java.util.ArrayList;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 6/3/2017.
 */

public class BrowserTotalFilter extends RealmObject {

    @PrimaryKey
    private String ID;
    private User myUser ;
    private Boolean isPositive ;

    public BrowserTotalFilter() {
    }

    public BrowserTotalFilter(String ID, User myUser, Boolean isPositive) {
        this.ID = ID;
        this.myUser = myUser;
        this.isPositive = isPositive;
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

    public Boolean getPositive() {
        return isPositive;
    }

    public void setPositive(Boolean positive) {
        isPositive = positive;
    }
}
