package com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceDetails;

/**
 * Created by sayantan on 15/8/17.
 */

public class RetailInvoiceLineDetails {

//    private ProductInfo mProduct;
    private String id;
    private String product_name;
    private String product_hsn;
    private String sales_price;
    private String quantity;
    private String cgst_value;
    private String cgst_percent;
    private String sgst_value;
    private String sgst_percent;
    private String line_total;
    private String original_qty;
    private String quantity_returned;
    private String current_return;
    private Boolean is_tax_included;
    private String product_id;
    private String unit_multi;
    private String unit_id;
    private String unit;
    private String discount_amount;


    public RetailInvoiceLineDetails(RetailInvoiceLineDetails item) {
        this.product_name = item.getProduct_name(); //Ok
        this.sales_price = item.getSales_price(); //Ok
        this.quantity = item.getQuantity();  //Ok
        this.original_qty = item.getQuantity();
        this.cgst_value = item.getCgst_value();
        this.sgst_value = item.getSgst_value();
        this.line_total = item.getLine_total();
        this.product_hsn = item.getProduct_hsn();  //Ok
        this.cgst_percent = item.getCgst_percent();  //Ok
        this.sgst_percent = item.getSgst_percent();  //Ok
        this.product_id = item.getProduct_id();  //Ok
        this.is_tax_included = item.getIs_tax_included();
        this.unit_multi = item.getUnit_multi();
        this.unit = item.getUnit();  //Ok
        this.unit_id = item.getUnit_id();  //Ok
        this.discount_amount = item.getDiscount_amount();
        this.quantity_returned = item.quantity_returned;
    }

//    public RetailInvoiceLineDetails (ProductInfo product){
//        this.mProduct = product;
//
//    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getSales_price() {
        return sales_price;
    }

    public void setSales_price(String sales_price) {
        this.sales_price = sales_price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCgst_value() {
        return cgst_value;
    }

    public void setCgst_value(String cgst_value) {
        this.cgst_value = cgst_value;
    }

    public String getSgst_value() {
        return sgst_value;
    }

    public void setSgst_value(String sgst_value) {
        this.sgst_value = sgst_value;
    }

    public String getLine_total() {
        return line_total;
    }

    public void setLine_total(String line_total) {
        this.line_total = line_total;
    }

    public String getProduct_hsn() {
        return product_hsn;
    }

    public String getOriginal_qty() {
        return original_qty;
    }

    public String getCgst_percent() {
        return cgst_percent;
    }

    public String getSgst_percent() {
        return sgst_percent;
    }

    public Boolean getIs_tax_included() {
        return is_tax_included;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getUnit() {
        return unit;
    }

    public String getUnit_multi() {
        return unit_multi;
    }

    public String getUnit_id() {
        return unit_id;
    }

    public String getDiscount_amount() {
        return discount_amount;
    }

    public void setDiscount_amount(String discount_amount) {
        this.discount_amount = discount_amount;
    }

    public String getQuantity_returned() {
        return quantity_returned;
    }

    public void setQuantity_returned(String quantity_returned) {
        this.quantity_returned = quantity_returned;
    }
}
