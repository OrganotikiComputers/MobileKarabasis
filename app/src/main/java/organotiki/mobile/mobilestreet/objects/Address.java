package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class Address extends RealmObject {

    @PrimaryKey
    private String ID;
    private Customer myCustomer;
    private String CustomerName;
    private String Street;
    private String City;
    private String PostalCode;
    private String Phone1;
    private String Phone2;
    private String Mobile;
    private String Email;
    private String Latitude;
    private String Longitude;

    // Standard getters & setters generated by your IDE…
    public Address(){}
    public Address(String ID, Customer customer, String customerName, String street, String city, String postalCode, String phone1, String phone2, String mobile, String email, String email2,String latitude,String longitude) {
        this.ID = ID;
        myCustomer = customer;
        CustomerName = customerName;
        Street = street;
        City = city;
        PostalCode = postalCode;
        Phone1 = phone1;
        Phone2 = phone2;
        Mobile = mobile;
        Email = email;
        Latitude=latitude;
        Longitude=longitude;
    }

    public String getID() { return ID; }
    public void   setID(String id) { this.ID = id; }

    public Customer getMyCustomer() {
        return myCustomer;
    }

    public void setMyCustomer(Customer myCustomer) {
        this.myCustomer = myCustomer;
    }

    public String getStreet() {
        return Street;
    }

    public void setStreet(String street) {
        Street = street;
    }

    public String getCity() { return City; }
    public void   setCity(String city) { this.City = city; }
    public String getPostalCode() { return PostalCode; }
    public void   setPostalCode(String postalCode) { this.PostalCode = postalCode; }
    public String getPhone1() { return Phone1; }
    public void   setPhone1(String phone1) { this.Phone1 = phone1; }
    public String getPhone2() { return Phone2; }
    public void   setPhone2(String phone2) { this.Phone2 = phone2; }
    public String getMobile() { return Mobile; }
    public void   setMobile(String mobile) { this.Mobile = mobile; }
    public String getEmail() { return Email; }
    public void setEmail(String email) { this.Email = email; }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }
}
