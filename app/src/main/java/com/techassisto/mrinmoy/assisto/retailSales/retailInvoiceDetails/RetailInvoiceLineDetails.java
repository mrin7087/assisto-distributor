package com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceDetails;

/**
 * Created by sayantan on 15/8/17.
 */

public class RetailInvoiceLineDetails {

    private String product_name;
    private String product_hsn;
    private String unit;
    private String sales_price;
    private String quantity;
    private String cgst_value;
    private String cgst_percent;
    private String sgst_value;
    private String sgst_percent;
    private String line_total;

    public RetailInvoiceLineDetails(String product_name, String sales_price, String quantity, String cgst_value, String sgst_value, String line_total) {
        this.product_name = product_name;
        this.sales_price = sales_price;
        this.quantity = quantity;
        this.cgst_value = cgst_value;
        this.sgst_value = sgst_value;
        this.line_total = line_total;
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
}
