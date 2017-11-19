package com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceDetails;

/**
 * Created by sayantan on 15/8/17.
 */

public class RetailInvoiceLineDetails {

//    private ProductInfo mProduct;

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
    private Boolean is_tax_included;
    private String product_id;
    private String unit_multi;
    private String unit_id;
    private String unit;

    public RetailInvoiceLineDetails(String product_id, String product_name, String sales_price, String quantity, String cgst_value, String sgst_value, String line_total, String product_hsn, String cgst_percent, String sgst_percent, Boolean is_tax_included, String unit_multi, String unit, String unit_id) {
        this.product_name = product_name; //Ok
        this.sales_price = sales_price; //Ok
        this.quantity = quantity;  //Ok
        this.original_qty = quantity;
        this.cgst_value = cgst_value;
        this.sgst_value = sgst_value;
        this.line_total = line_total;
        this.product_hsn = product_hsn;  //Ok
        this.cgst_percent = cgst_percent;  //Ok
        this.sgst_percent = sgst_percent;  //Ok
        this.product_id = product_id;  //Ok
        this.is_tax_included = is_tax_included;
        this.unit_multi = unit_multi;
        this.unit = unit;  //Ok
        this.unit_id = unit_id;  //Ok
    }

//    public RetailInvoiceLineDetails (ProductInfo product){
//        this.mProduct = product;
//
//    }

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
}
