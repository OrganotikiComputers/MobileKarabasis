package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 6/9/2016.
 */
public class CompanySite extends RealmObject {

    @PrimaryKey
    private String ID;
    private Company myCompany;
    private String Code;
    private String Description;
    private boolean isMain;

    public CompanySite() {
    }

    public CompanySite(String ID, Company myCompany, String code, String description, boolean isMain) {
        this.ID = ID;
        this.myCompany = myCompany;
        Code = code;
        Description = description;
        this.isMain = isMain;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getMyCompany() {
        return myCompany;
    }

    public void setMyCompany(Company myCompany) {
        this.myCompany = myCompany;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }
}
