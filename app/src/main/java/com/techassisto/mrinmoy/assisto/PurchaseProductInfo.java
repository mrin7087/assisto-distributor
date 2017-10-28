package com.techassisto.mrinmoy.assisto;

import java.util.List;

/**
 * Created by sayantan on 25/10/17.
 */

public class PurchaseProductInfo {

    public int product_id;
    public String product_name;
    public String product_hsn;
    public int unit_id;
    public String unit;
    public double sgst;
    public double cgst;
    public int quantity;
    public double purchase_rate;
    public double tsp;
    public double mrp;
    public int disc_type;
    public int disc_type2;
    public double disc;
    public double disc_2;

    public double selectedRate;
    public int selectedQuantity;

    @Override
    public String toString() {
        String productInfo = "ProductInfo:";
        productInfo += " id: " + product_id;
        productInfo += " name: " + product_name;
        productInfo += " hsn: " + product_hsn;
        productInfo += " sgst: " + sgst;
        productInfo += " cgst: " + cgst;
        productInfo += " purchase: " + purchase_rate;
        productInfo += "]";

        return productInfo;
    }
}
