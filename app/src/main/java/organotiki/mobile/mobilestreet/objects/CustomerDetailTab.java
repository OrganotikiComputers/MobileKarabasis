package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 1/7/2016.
 */
public class CustomerDetailTab extends RealmObject {

    private RealmList<CustomerDetailRow> BarChartDetails;
    private RealmList<String> Colors;
    private RealmList<CustomerDetail> Details;
    private RealmList<CustomerDetailRow> HorizontalDetails;
    private String Orientation;
    private String Title;

    public CustomerDetailTab() {
    }

    public CustomerDetailTab(String title,RealmList<CustomerDetail> details) {
        Title=title;
		Details=details;
    }
	
	public RealmList<CustomerDetailRow> getBarChartDetails() {
        return BarChartDetails;
    }

    public void setBarChartDetails(RealmList<CustomerDetailRow> barChartDetails) {
        BarChartDetails = barChartDetails;
    }

    public RealmList<String> getColors() {
        return Colors;
    }

    public void setColors(RealmList<String> colors) {
        Colors = colors;
    }

    public RealmList<CustomerDetail> getDetails() {
        return Details;
    }

    public void setDetails(RealmList<CustomerDetail> details) {
        Details = details;
    }

    public RealmList<CustomerDetailRow> getHorizontalDetails() {
        return HorizontalDetails;
    }

    public void setHorizontalDetails(RealmList<CustomerDetailRow> horizontalDetails) {
        HorizontalDetails = horizontalDetails;
    }

    public String getOrientation() {
        return Orientation;
    }

    public void setOrientation(String orientation) {
        Orientation = orientation;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }
}
