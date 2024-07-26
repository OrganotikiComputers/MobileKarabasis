package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 1/7/2016.
 */
public class CustomerDetailRow extends RealmObject {

    private RealmList<String> Data;

    public CustomerDetailRow() {
    }
	
	public RealmList<String> getData() {
        return Data;
    }

    public void setData(RealmList<String> data) {
        Data = data;
    }
	
}
