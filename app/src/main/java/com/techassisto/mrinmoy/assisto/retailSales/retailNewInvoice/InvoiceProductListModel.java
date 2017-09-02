package com.techassisto.mrinmoy.assisto.retailSales.retailNewInvoice;

import com.techassisto.mrinmoy.assisto.ProductInfo;

/**
 * Created by Mrinmoy on 6/25/2017.
 */

public class InvoiceProductListModel {
    private ProductInfo mProduct;
    private String product_name;
    private int product_quantity;
    private double product_price;

//    public InvoiceProductListModel(String name, int quantity, double price) {
//        this.product_name = name;
//        this.product_quantity = quantity;
//        this.product_price = price;
//    }

    public InvoiceProductListModel(ProductInfo product) {
        mProduct = product;
        this.product_name = product.product_name;
        this.product_quantity = product.selectedQuantity;
        this.product_price = product.selectedRate;
    }

    public ProductInfo getProduct() {
        return mProduct;
    }

    public String getName() {
        return product_name;
    }
    public int getQuantity() {
        return product_quantity;
    }
    public double getPrice() {
        return product_price;
    }

    public void setName(String name) {
        this.product_name = name;
    }
    public void setQuantity(int quantity) {
        this.product_quantity = quantity;
    }
    public void setPrice(double price) {
        this.product_price = price;
    }

//    public String getKey() {
//        return key;
//    }
//
//    public void setKey(String key) {
//        this.key = key;
//    }
}
