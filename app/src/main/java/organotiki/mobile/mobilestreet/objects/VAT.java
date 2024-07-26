package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 31/5/2016.
 */
public class VAT extends RealmObject {

    @PrimaryKey
    private String ID;
    private String Name;
    private Double Basic;
    private Double Low;
    private Double Free;

    public VAT() {
    }

    public VAT(Double basic, Double free, String iD, Double low, String name) {
        this.Basic = basic;
        this.Free = free;
        this.ID = iD;
        this.Low = low;
        this.Name = name;
    }

    public Double getBasic() {
        return Basic;
    }

    public void setBasic(Double basic) {
        this.Basic = basic;
    }

    public Double getFree() {
        return Free;
    }

    public void setFree(Double free) {
        this.Free = free;
    }

    public String getID() {
        return ID;
    }

    public void setID(String iD) {
        this.ID = iD;
    }

    public Double getLow() {
        return Low;
    }

    public void setLow(Double low) {
        this.Low = low;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

}