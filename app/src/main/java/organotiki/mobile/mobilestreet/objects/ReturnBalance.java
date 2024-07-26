package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 1/7/2016.
 */
public class ReturnBalance extends RealmObject {

    @PrimaryKey
    private String CustomerCode;
    private Customer MyCustomer;
    private double ReturnsPercent;
    private double ReturnsValue;

    public ReturnBalance() {
    }

    public ReturnBalance(String CustomerCode, Customer MyCustomer, double ReturnsValue,double ReturnsPercent) {
        this.CustomerCode=CustomerCode;
        this.MyCustomer=MyCustomer;
        this.ReturnsPercent=ReturnsPercent;
		this.ReturnsValue=ReturnsValue;
    }
	
	public String getCustomerCode() {
        return CustomerCode;
    }

    public void setCustomerCode(String customerCode) {
        CustomerCode = customerCode;
    }

    public Customer getMyCustomer() {
        return MyCustomer;
    }

    public void setMyCustomer(Customer myCustomer) {
        MyCustomer = myCustomer;
    }

    public Double getReturnsPercent() {
        return ReturnsPercent;
    }

    public void setReturnsPercent(Double returnsPercent) {
        ReturnsPercent = returnsPercent;
    }

    public Double getReturnsValue() {
        return ReturnsValue;
    }

    public void setReturnsValue(Double returnsValue) {
        ReturnsValue = returnsValue;
    }
}
