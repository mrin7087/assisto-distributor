package com.techassisto.mrinmoy.assisto.utilDeclaration;

import com.google.gson.JsonArray;

import java.util.List;

/**
 * Created by sayantan on 4/12/17.
 */

public class ServiceInfo {

    public class ServiceRate {
        public String tentative_sales_rate;
        public boolean is_tax_included;
    }

    public int service_id;
    public String service_name;
    public String service_hsn;
    public int unit_id;
    public String unit;
    public double sgst;
    public double cgst;
    public int quantity;
    public List<ServiceRate> rate;

    public double selectedRate;
    public boolean selectedIsTaxIncluded;
    public int selectedQuantity;
    public JsonArray salespersons;

    @Override
    public String toString() {
        String serviceInfo = "ServiceInfo:";
        serviceInfo += " id: " + service_id;
        serviceInfo += " name: " + service_name;
        serviceInfo += " hsn: " + service_hsn;
        serviceInfo += " sgst: " + sgst;
        serviceInfo += " cgst: " + cgst;
        serviceInfo += " rates: [";
        if (rate != null) {
            for (int i=0; i<rate.size(); i++) {
                serviceInfo += " rate" + (i+1) + ": " + rate.get(i).tentative_sales_rate;
            }
        }
        serviceInfo += "]";

        return serviceInfo;
    }
}
