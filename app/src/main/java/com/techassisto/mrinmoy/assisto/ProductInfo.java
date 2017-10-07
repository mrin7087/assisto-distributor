package com.techassisto.mrinmoy.assisto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mrinmoy on 6/27/2017.
 */

public class ProductInfo {
    public class ProductRate {
        public String tentative_sales_rate;
        public boolean is_tax_included;
    }

    public int product_id;
    public String product_name;
    public String product_hsn;
    public int unit_id;
    public String unit;
    public double sgst;
    public double cgst;
    public int quantity;
    public List<ProductRate> rate;
    public boolean inventory;

    public double selectedRate;
    public boolean selectedIsTaxIncluded;
    public int selectedQuantity;

    @Override
    public String toString() {
        String productInfo = "ProductInfo:";
        productInfo += " id: " + product_id;
        productInfo += " name: " + product_name;
        productInfo += " hsn: " + product_hsn;
        productInfo += " sgst: " + sgst;
        productInfo += " cgst: " + cgst;
        productInfo += " rates: [";
        if (rate != null) {
            for (int i=0; i<rate.size(); i++) {
                productInfo += " rate" + (i+1) + ": " + rate.get(i).tentative_sales_rate;
            }
        }
        productInfo += "]";

        return productInfo;
    }
}