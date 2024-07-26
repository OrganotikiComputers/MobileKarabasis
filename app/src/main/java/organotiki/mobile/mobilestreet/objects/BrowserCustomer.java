package organotiki.mobile.mobilestreet.objects;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 22/12/2016.
 */

public class BrowserCustomer extends RealmObject {

    private User myUser;
	@PrimaryKey
    private String CustomerCode;
    private String CustomerName;
    private Double TotalBalance;
    private Double BalanceAEY;
    private Double BalanceASY;
    private Double BalanceFEY;
    private Double BalanceFSY;
    @Index
    private String City;
    private Double Coupon;
    private boolean WillVisit;
    private boolean Visited;

    public BrowserCustomer() {
    }

    public BrowserCustomer(User myUser, String customerCode, String customerName, Double totalBalance, Double balanceAEY, Double balanceASY, Double balanceFEY, Double balanceFSY, String city, Double coupon, boolean willVisit, boolean visited) {
        this.myUser = myUser;
        CustomerCode = customerCode;
        CustomerName = customerName;
        TotalBalance = totalBalance;
        BalanceAEY = balanceAEY;
        BalanceASY = balanceASY;
        BalanceFEY = balanceFEY;
        BalanceFSY = balanceFSY;
        City = city;
        Coupon = coupon;
        WillVisit = willVisit;
        Visited = visited;
    }

    public Double getCoupon() {
        return Coupon;
    }

    public void setCoupon(Double coupon) {
        Coupon = coupon;
    }

    public User getMyUser() {
        return myUser;
    }

    public void setMyUser(User myUser) {
        this.myUser = myUser;
    }

    public String getCustomerCode() {
        return CustomerCode;
    }

    public void setCustomerCode(String customerCode) {
        CustomerCode = customerCode;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public Double getTotalBalance() {
        return TotalBalance;
    }

    public void setTotalBalance(Double totalBalance) {
        TotalBalance = totalBalance;
    }

    public Double getBalanceAEY() {
        return BalanceAEY;
    }

    public void setBalanceAEY(Double balanceAEY) {
        BalanceAEY = balanceAEY;
    }

    public Double getBalanceASY() {
        return BalanceASY;
    }

    public void setBalanceASY(Double balanceASY) {
        BalanceASY = balanceASY;
    }

    public Double getBalanceFEY() {
        return BalanceFEY;
    }

    public void setBalanceFEY(Double balanceFEY) {
        BalanceFEY = balanceFEY;
    }

    public Double getBalanceFSY() {
        return BalanceFSY;
    }

    public void setBalanceFSY(Double balanceFSY) {
        BalanceFSY = balanceFSY;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public boolean isWillVisit() {
        return WillVisit;
    }

    public void setWillVisit(boolean willVisit) {
        WillVisit = willVisit;
    }

    public boolean isVisited() {
        return Visited;
    }

    public void setVisited(boolean visited) {
        Visited = visited;
    }

    public String getTotalBalanceText() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(TotalBalance).replace(",", ".");
    }

    public void setTotalBalanceText(String totalBalance) {
        TotalBalance = Double.parseDouble(totalBalance.equals("")?"0":totalBalance.replace(",", "."));
    }

    public String getBalanceAEYText() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(BalanceAEY).replace(",", ".");
    }

    public void setBalanceAEYText(String balanceAEY) {
        BalanceAEY = Double.parseDouble(balanceAEY.equals("")?"0":balanceAEY.replace(",", "."));
    }

    public String getBalanceASYText() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(BalanceASY).replace(",", ".");
    }

    public void setBalanceASYText(String balanceASY) {
        BalanceASY = Double.parseDouble(balanceASY.equals("")?"0":balanceASY.replace(",", "."));
    }

    public String getBalanceFEYText() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(BalanceFEY).replace(",", ".");
    }

    public void setBalanceFEYText(String balanceFEY) {
        BalanceFEY = Double.parseDouble(balanceFEY.equals("")?"0":balanceFEY.replace(",", "."));
    }

    public String getBalanceFSYText() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(BalanceFSY).replace(",", ".");
    }

    public void setBalanceFSYText(String balanceFSY) {
        BalanceFSY = Double.parseDouble(balanceFSY.equals("")?"0":balanceFSY.replace(",", "."));
    }

    public String getCouponText() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(Coupon).replace(",", ".");
    }

    public void setCouponText(String coupon) {
        Coupon = Double.parseDouble(coupon.equals("")?"0":coupon.replace(",", "."));
    }
}
