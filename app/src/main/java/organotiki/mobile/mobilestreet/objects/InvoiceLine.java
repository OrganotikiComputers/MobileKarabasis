package organotiki.mobile.mobilestreet.objects;

import java.text.DecimalFormat;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class InvoiceLine extends RealmObject {

    @PrimaryKey
    private String ID;
    private Invoice myInvoice;
    private Item myItem;
    private InvoiceType myInvoiceType;
    private Double WPrice = 0.0;
    private Double Price = 0.0;
    private Double Quantity = 0.0;
    private Double Discount = 0.0;
    private Double Value = 0.0;
    private String Notes;
    private String LastDate;
    private Company LastCompany;
    private Double LastQuantity = 0.0;
    private String WrhID;
    private String BraID;
    private String TypeCode;
    private String DosCode;
    private String DocNumber;
    private int Overdue;
    private boolean isEY;
    private boolean isGuarantee;
    private boolean fromCustomer;
    private String TRNID;
    private String Manufacturer;
    private String Model;
    private Integer Year1;
    private String EngineCode;
    private Integer Year2;
    private Long KMTraveled;
    private String ReturnCause;
    private String Observations;
    private boolean isExtraCharge;
    private Double DocValue = 0.0;
	private Double ChargePapi;
    private String DocID;
    private Double ExtraChargeLimit;
    private Double ExtraChargeValue;
	private Double DiscountReturnPercent;
    private boolean isAllowed;



    private String MessageAllowed;

    public InvoiceLine() {
    }

    public InvoiceLine(String ID, Invoice invoice, Item item, Double price, Double quantity, Double discount, Double value, String notes) {
        this.ID = ID;
        myInvoice = invoice;
        myItem = item;
        Price = price;
        Quantity = quantity;
        Discount = discount;
        Value = value;
        Notes = notes;
    }

    public InvoiceLine(String ID, Invoice myInvoice, Item myItem, Double wPrice, Double price, Double quantity, String notes, String lastDate, Company lastCompany,Double lastQuantity, String wrhID, String braID, String typeCode, String dosCode, String docNumber, int overdue, boolean isEY,boolean isGuarantee, boolean fromCustomer, String TRNID,  String manufacturer, String model, Integer year1, String engineCode, Integer year2, Long KMTraveled, String returnCause, String observations,String docID,double docValue, double ChargePapi,boolean isExtraCharge,double extraChargeValue,double extraChargeLimit,double discountReturnPercent,boolean isallowed,String messageallowed) {
        this.ID = ID;
        this.myInvoice = myInvoice;
        this.myItem = myItem;
        WPrice = wPrice;
        Price = price;
        Quantity = quantity;
        Value = (Price==null?0:Price) * (Quantity==null?0:Quantity) * (100 - (Discount==null?0:Discount)) / 100;
        Notes = notes;
        LastDate = lastDate;
        LastCompany = lastCompany;
        LastQuantity = lastQuantity;
        WrhID = wrhID;
        BraID = braID;
        TypeCode = typeCode;
        DosCode = dosCode;
        DocNumber = docNumber;
        Overdue = overdue;
        this.isEY = isEY;
        this.isGuarantee = isGuarantee;
        this.fromCustomer = fromCustomer;
        this.TRNID = TRNID;
        Manufacturer = manufacturer;
        Model = model;
        Year1 = year1;
        EngineCode = engineCode;
        Year2 = year2;
        this.KMTraveled = KMTraveled;
        ReturnCause = returnCause;
        Observations = observations;
        this.isExtraCharge = isExtraCharge;
        this.DocValue = docValue;
		DocID=docID;
		this.ChargePapi=ChargePapi;
		ExtraChargeLimit=extraChargeLimit;
		ExtraChargeValue=extraChargeValue;
		DiscountReturnPercent=discountReturnPercent;
		isAllowed=isallowed;
		MessageAllowed=messageallowed;
    }
    public String getMessageAllowed() {
        return MessageAllowed;
    }

    public void setMessageAllowed(String messageAllowed) {
        MessageAllowed = messageAllowed;
    }
    public boolean isAllowed() {
        return isAllowed;
    }

    public void setAllowed(boolean allowed) {
        isAllowed = allowed;
    }

    public Double getDiscountReturnPercent() {
        return DiscountReturnPercent;
    }

    public void setDiscountReturnPercent(Double discountReturnPercent) {
        DiscountReturnPercent = discountReturnPercent;
    }
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Invoice getMyInvoice() {
        return myInvoice;
    }

    public void setMyInvoice(Invoice myInvoice) {
        this.myInvoice = myInvoice;
    }

    public Item getMyItem() {
        return myItem;
    }

    public void setMyItem(Item myItem) {
        this.myItem = myItem;
    }

    public String getDosCode() {
        return DosCode;
    }

    public void setDosCode(String dosCode) {
        DosCode = dosCode;
    }

    public String getDocNumber() {
        return DocNumber;
    }

    public void setDocNumber(String docNumber) {
        DocNumber = docNumber;
    }

    public InvoiceType getMyInvoiceType() {
        return myInvoiceType;
    }

    public void setMyInvoiceType(InvoiceType myInvoiceType) {
        this.myInvoiceType = myInvoiceType;
    }

    public Double getWPrice() {
        return WPrice;
    }

    public void setWPrice(Double WPrice) {
        this.WPrice = WPrice;
    }

    public Double getPrice() {
        return Price;
    }

    public void setPrice(Double price) {
        Price = price;
    }

    public Double getQuantity() {
        return Quantity;
    }

    public void setQuantity(Double quantity) {
        Quantity = quantity;
    }

    public Double getDiscount() {
        return Discount;
    }

    public void setDiscount(Double discount) {
        Discount = discount;
    }

    public Double getValue() {
        return Value;
    }

    public void setValue(Double value) {
        Value = value;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

//    public Integer getType() {
//        return Type;
//    }
//
//    public void setType(Integer type) {
//        Type = type;
//    }


    public void setLastQuantityText(String lastQuantity) {
        LastQuantity = Double.parseDouble(lastQuantity.equals("")?"0":lastQuantity.replace(",", "."));
    }

    public String getLastQuantityText() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(LastQuantity).replace(",", ".");
    }
    public String getPriceText() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(Price).replace(",", ".");
    }

    public void setPriceText(String price) {
        Price = Double.parseDouble(price.equals("")?"0":price.replace(",", "."));
    }

    public String getQuantityText() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(Quantity).replace(",", ".");
    }

    public void setQuantityText(String quantity) {
        Quantity = Double.parseDouble(quantity.equals("")?"0":quantity.replace(",", "."));
    }

    public String getDiscountText() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(Discount).replace(",", ".");
    }

    public void setDiscountText(String discount) {
        Discount = Double.parseDouble(discount.equals("")?"0":discount.replace(",", "."));
    }

    public String getValueText() {

        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(Value).replace(",", ".");
    }

    public void setValueText(String value) {
        Value = Double.parseDouble(value.replace(",", "."));
    }

    public String getLastDate() {
        return LastDate;
    }

    public void setLastDate(String lastDate) {
        LastDate = lastDate;
    }

    public Company getLastCompany() {
        return LastCompany;
    }

    public void setLastCompany(Company lastCompany) {
        LastCompany = lastCompany;
    }

    public Double getLastQuantity() {
        return LastQuantity;
    }

    public void setLastQuantity(Double lastQuantity) {
        LastQuantity = lastQuantity;
    }

    public String getTypeCode() {
        return TypeCode;
    }

    public void setTypeCode(String typeCode) {
        TypeCode = typeCode;
    }

    public int getOverdue() {
        return Overdue;
    }

    public void setOverdue(int overdue) {
        Overdue = overdue;
    }

    public String getWrhID() {
        return WrhID;
    }

    public void setWrhID(String wrhID) {
        WrhID = wrhID;
    }

    public String getBraID() {
        return BraID;
    }

    public void setBraID(String braID) {
        BraID = braID;
    }

    public boolean isEY() {
        return isEY;
    }

    public void setEY(boolean EY) {
        isEY = EY;
    }

    public boolean isGuarantee() {
        return isGuarantee;
    }

    public void setGuarantee(boolean guarantee) {
        isGuarantee = guarantee;
    }

    public boolean isFromCustomer() {
        return fromCustomer;
    }

    public void setFromCustomer(boolean fromCustomer) {
        this.fromCustomer = fromCustomer;
    }

    public String getTRNID() {
        return TRNID;
    }

    public void setTRNID(String TRNID) {
        this.TRNID = TRNID;
    }

    public String getManufacturer() {
        return Manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        Manufacturer = manufacturer;
    }

    public String getModel() {
        return Model;
    }

    public void setModel(String model) {
        Model = model;
    }

    public Integer getYear1() {
        return Year1;
    }

    public void setYear1(Integer year1) {
        Year1 = year1;
    }

    public String getEngineCode() {
        return EngineCode;
    }

    public void setEngineCode(String engineCode) {
        EngineCode = engineCode;
    }

    public Integer getYear2() {
        return Year2;
    }

    public void setYear2(Integer year2) {
        Year2 = year2;
    }

    public Long getKMTraveled() {
        return KMTraveled;
    }

    public void setKMTraveled(Long KMTraveled) {
        this.KMTraveled = KMTraveled;
    }

    public String getReturnCause() {
        return ReturnCause;
    }

    public void setReturnCause(String returnCause) {
        ReturnCause = returnCause;
    }

    public String getObservations() {
        return Observations;
    }

    public void setObservations(String observations) {
        Observations = observations;
    }

    public boolean isExtraCharge() {
        return isExtraCharge;
    }

    public void setExtraCharge(boolean extraCharge) {
        isExtraCharge = extraCharge;
    }

    public Double getDocValue() {
        return DocValue;
    }

    public void setDocValue(Double docValue) {
        DocValue = docValue;
    }
	
	    public Double getChargePapi() {
        return ChargePapi;
    }

    public void setChargePapi(Double chargePapi) {
        ChargePapi = chargePapi;
    }

    public String getDocID() {
        return DocID;
    }

    public void setDocID(String docID) {
        DocID = docID;
    }

    public Double getExtraChargeLimit() {
        return ExtraChargeLimit;
    }

    public void setExtraChargeLimit(Double extraChargeLimit) {
        ExtraChargeLimit = extraChargeLimit;
    }

    public Double getExtraChargeValue() {
        return ExtraChargeValue;
    }

    public void setExtraChargeValue(Double extraChargeValue) {
        ExtraChargeValue = extraChargeValue;
    }
    public Double getManageCost() {
        if(Double.valueOf(getMyInvoice().getManagementCostPercent())== null||Double.valueOf(getMyInvoice().getManagementCostPercent())==0.0){
            if(Double.valueOf(getDiscountReturnPercent())==null){
                return 0.0;
            }
            else {
                return ((getQuantity().doubleValue() * getPrice().doubleValue()) * getDiscountReturnPercent().doubleValue()) / 100.0d;
            }
        }
        else{
            return ((getQuantity().doubleValue() * getPrice().doubleValue()) * getMyInvoice().getManagementCostPercent().doubleValue()) / 100.0d;
            }
    }

    public String getManageCostText() {
        if(getMyInvoice().getManagementCostPercent() == null){
            if(getDiscountReturnPercent()==null||Double.valueOf(getMyInvoice().getManagementCostPercent())==0.0){
                return "0";
            }
            else{
                return new DecimalFormat("0.00").format(((getQuantity().doubleValue() * getPrice().doubleValue()) * getDiscountReturnPercent().doubleValue()) / 100.0d).replace(",", ".");
            }
        }
        else{
            return new DecimalFormat("0.00").format(((getQuantity().doubleValue() * getPrice().doubleValue()) * getMyInvoice().getManagementCostPercent().doubleValue()) / 100.0d).replace(",", ".");
        }    }
}