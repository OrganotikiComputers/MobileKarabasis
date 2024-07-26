package organotiki.mobile.mobilestreet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import organotiki.mobile.mobilestreet.objects.Address;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.InvoiceLineSimple;

/**
 * Created by Thanasis on 25/5/2016.
 */
public interface Communicator {
    void respondCustomerSearch(Customer customer, Address address);
    void respondInvoiceType();
    void respondPaymentTerm();
    void respondCustomerCreate();
    void respondVolleyRequestFinished(Integer position,JSONObject jsonObject);
    void respondDate(Integer position,int year, int month, int day);
    void respondCompanySite();
    void respondRecentPurchases(ArrayList<InvoiceLineSimple> sLines);
}
