package com.techassisto.mrinmoy.assisto.retailSales;

/**
 * Created by sayantan on 11/2/18.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LineItem {
    @SerializedName("quantity_returned")
    @Expose
    private String quantityReturned;
    @SerializedName("product")
    @Expose
    private Long product;
    @SerializedName("discount_amount")
    @Expose
    private String discountAmount;
    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("sgst_percent")
    @Expose
    private String sgstPercent;
    @SerializedName("sales_price")
    @Expose
    private String salesPrice;
    @SerializedName("cgst_percent")
    @Expose
    private String cgstPercent;
    @SerializedName("unit_multi")
    @Expose
    private String unitMulti;
    @SerializedName("is_tax_included")
    @Expose
    private Boolean isTaxIncluded;
    @SerializedName("cgst_value")
    @Expose
    private String cgstValue;
    @SerializedName("igst_value")
    @Expose
    private String igstValue;
    @SerializedName("unit")
    @Expose
    private String unit;
    @SerializedName("line_before_tax")
    @Expose
    private String lineBeforeTax;
    @SerializedName("product_hsn")
    @Expose
    private String productHsn;
    @SerializedName("igst_percent")
    @Expose
    private String igstPercent;
    @SerializedName("product_id")
    @Expose
    private Long productId;
    @SerializedName("product_name")
    @Expose
    private String productName;
    @SerializedName("quantity")
    @Expose
    private String quantity;
    @SerializedName("unit_id")
    @Expose
    private Long unitId;
    @SerializedName("line_total")
    @Expose
    private String lineTotal;
    @SerializedName("sgst_value")
    @Expose
    private String sgstValue;
    @SerializedName("return_line_before_tax")
    @Expose
    private String return_line_before_tax;
    @SerializedName("return_line_total")
    @Expose
    private String return_line_total;
    @SerializedName("current_quantity_returned")
    @Expose
    private String currentQuantityReturned;
    @SerializedName("return_cgst_value")
    @Expose
    private String returnCgstValue;

    public String getReturnCgstValue() {
        return returnCgstValue;
    }

    public void setReturnCgstValue(String returnCgstValue) {
        this.returnCgstValue = returnCgstValue;
    }

    public String getReturnSgstValue() {
        return returnSgstValue;
    }

    public void setReturnSgstValue(String returnSgstValue) {
        this.returnSgstValue = returnSgstValue;
    }

    @SerializedName("retirn_sgst_value")
    @Expose
    private String returnSgstValue;


    public double qtyAvailable;


    public String getQuantityReturned() {
        return quantityReturned;
    }

    public void setQuantityReturned(String quantityReturned) {
        this.quantityReturned = quantityReturned;
    }

    public Long getProduct() {
        return product;
    }

    public void setProduct(Long product) {
        this.product = product;
    }

    public String getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(String discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSgstPercent() {
        return sgstPercent;
    }

    public void setSgstPercent(String sgstPercent) {
        this.sgstPercent = sgstPercent;
    }

    public String getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(String salesPrice) {
        this.salesPrice = salesPrice;
    }

    public String getCgstPercent() {
        return cgstPercent;
    }

    public void setCgstPercent(String cgstPercent) {
        this.cgstPercent = cgstPercent;
    }

    public String getUnitMulti() {
        return unitMulti;
    }

    public void setUnitMulti(String unitMulti) {
        this.unitMulti = unitMulti;
    }

    public Boolean getIsTaxIncluded() {
        return isTaxIncluded;
    }

    public void setIsTaxIncluded(Boolean isTaxIncluded) {
        this.isTaxIncluded = isTaxIncluded;
    }

    public String getCgstValue() {
        return cgstValue;
    }

    public void setCgstValue(String cgstValue) {
        this.cgstValue = cgstValue;
    }

    public String getIgstValue() {
        return igstValue;
    }

    public void setIgstValue(String igstValue) {
        this.igstValue = igstValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getLineBeforeTax() {
        return lineBeforeTax;
    }

    public void setLineBeforeTax(String lineBeforeTax) {
        this.lineBeforeTax = lineBeforeTax;
    }

    public String getProductHsn() {
        return productHsn;
    }

    public void setProductHsn(String productHsn) {
        this.productHsn = productHsn;
    }

    public String getIgstPercent() {
        return igstPercent;
    }

    public void setIgstPercent(String igstPercent) {
        this.igstPercent = igstPercent;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(String lineTotal) {
        this.lineTotal = lineTotal;
    }

    public String getSgstValue() {
        return sgstValue;
    }

    public void setSgstValue(String sgstValue) {
        this.sgstValue = sgstValue;
    }

    public String getQuantityAvailable() {
        double qty = Double.parseDouble(this.getQuantity());
        double qtyReturned = Double.parseDouble(this.getQuantityReturned());
        return String.valueOf((qty - qtyReturned));
    }

    public String getReturn_line_before_tax() {
        return return_line_before_tax;
    }

    public void setReturn_line_before_tax(String return_line_before_tax) {
        this.return_line_before_tax = return_line_before_tax;
    }

    public String getReturn_line_total() {
        return return_line_total;
    }

    public void setReturn_line_total(String return_line_total) {
        this.return_line_total = return_line_total;
    }

    public String getCurrentQuantityReturned() {
        return currentQuantityReturned;
    }

    public void setCurrentQuantityReturned(String currentQuantityReturned) {
        this.currentQuantityReturned = currentQuantityReturned;
    }
}
