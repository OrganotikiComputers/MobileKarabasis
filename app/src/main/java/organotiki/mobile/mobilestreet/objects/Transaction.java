package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 26/8/2016.
 */
public class Transaction extends RealmObject {

    @PrimaryKey
    private String ID;


    public Transaction() {
    }


}
