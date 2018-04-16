package com.techassisto.mrinmoy.assisto.retailSales;

/**
 * Created by sayantan on 11/2/18.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SalesInvoiceDetail {
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("customer_name")
    @Expose
    private Object customerName;
    @SerializedName("tenant_name")
    @Expose
    private String tenantName;
    @SerializedName("warehouse_city")
    @Expose
    private String warehouseCity;
    @SerializedName("total")
    @Expose
    private String total;
    @SerializedName("roundoff")
    @Expose
    private String roundoff;
    @SerializedName("amount_paid")
    @Expose
    private String amountPaid;
    @SerializedName("invoice_id")
    @Expose
    private Long invoiceId;
    @SerializedName("subtotal")
    @Expose
    private String subtotal;
    @SerializedName("cgsttotal")
    @Expose
    private String cgsttotal;
    @SerializedName("line_items")
    @Expose
    private ArrayList<LineItem> lineItems = null;
    @SerializedName("warehouse_pin")
    @Expose
    private String warehousePin;
    @SerializedName("warehouse_address")
    @Expose
    private String warehouseAddress;
    @SerializedName("sgsttotal")
    @Expose
    private String sgsttotal;

    public String getDate(){
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            return dateFormat.format(originalFormat.parse(date));
        }catch (ParseException e){
         return date;
        }
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Object getCustomerName() {
        return customerName;
    }

    public void setCustomerName(Object customerName) {
        this.customerName = customerName;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getWarehouseCity() {
        return warehouseCity;
    }

    public void setWarehouseCity(String warehouseCity) {
        this.warehouseCity = warehouseCity;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getRoundoff() {
        return roundoff;
    }

    public void setRoundoff(String roundoff) {
        this.roundoff = roundoff;
    }

    public String getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(String amountPaid) {
        this.amountPaid = amountPaid;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }

    public String getCgsttotal() {
        return cgsttotal;
    }

    public void setCgsttotal(String cgsttotal) {
        this.cgsttotal = cgsttotal;
    }

    public ArrayList<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(ArrayList<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public String getWarehousePin() {
        return warehousePin;
    }

    public void setWarehousePin(String warehousePin) {
        this.warehousePin = warehousePin;
    }

    public String getWarehouseAddress() {
        return warehouseAddress;
    }

    public void setWarehouseAddress(String warehouseAddress) {
        this.warehouseAddress = warehouseAddress;
    }

    public String getSgsttotal() {
        return sgsttotal;
    }

    public void setSgsttotal(String sgsttotal) {
        this.sgsttotal = sgsttotal;
    }

}
