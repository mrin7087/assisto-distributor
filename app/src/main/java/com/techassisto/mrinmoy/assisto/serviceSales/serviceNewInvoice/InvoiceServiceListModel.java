package com.techassisto.mrinmoy.assisto.serviceSales.serviceNewInvoice;

import com.techassisto.mrinmoy.assisto.ServiceInfo;

/**
 * Created by sayantan on 3/12/17.
 */

public class InvoiceServiceListModel {

    private ServiceInfo mService;
    private String service_name;
    private int service_quantity;
    private double service_price;
    private boolean is_tax_included;

    public InvoiceServiceListModel(ServiceInfo service) {
        mService = service;
        this.service_name= service.service_name;
        this.service_quantity = service.selectedQuantity;
        this.service_price = service.selectedRate;
        this.is_tax_included = service.selectedIsTaxIncluded;
    }

    public ServiceInfo getService() {
        return mService;
    }

    public String getName() {
        return service_name;
    }

    public int getQuantity() {
        return service_quantity;
    }

    public double getPrice() {
        return service_price;
    }

    public boolean is_tax_included() {
        return is_tax_included;
    }

    public void setName(String service_name) {
        this.service_name = service_name;
    }

    public void setQuantity(int service_quantity) {
        this.service_quantity = service_quantity;
    }

    public void setPrice(double service_price) {
        this.service_price = service_price;
    }
}
