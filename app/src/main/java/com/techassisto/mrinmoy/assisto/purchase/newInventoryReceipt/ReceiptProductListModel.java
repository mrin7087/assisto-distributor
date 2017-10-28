package com.techassisto.mrinmoy.assisto.purchase.newInventoryReceipt;

import com.techassisto.mrinmoy.assisto.ProductInfo;
import com.techassisto.mrinmoy.assisto.PurchaseProductInfo;

/**
 * Created by sayantan on 25/10/17.
 */

public class ReceiptProductListModel {
    private PurchaseProductInfo mProduct;
    private String product_name;
    private int product_quantity;
    private double product_purchase_price;
    private double product_sales_price;
    private double product_mrp;
    private int disc_type;
    private int disc_type2;
    private double disc;
    private double disc_2;
    private double cgst_p;
    private double sgst_p;

//    public InvoiceProductListModel(String name, int quantity, double price) {
//        this.product_name = name;
//        this.product_quantity = quantity;
//        this.product_price = price;
//    }

    public ReceiptProductListModel(PurchaseProductInfo product) {
        mProduct = product;
        this.product_name = product.product_name;
        this.product_quantity = product.selectedQuantity;
        this.product_purchase_price = product.purchase_rate;
    }

    public PurchaseProductInfo getProduct() {
        return mProduct;
    }

    public String getName() {
        return product_name;
    }
    public int getQuantity() {
        return product_quantity;
    }
    public double getPurchasePrice() {
        return product_purchase_price;
    }


    public void setName(String name) {
        this.product_name = name;
    }
    public void setQuantity(int quantity) {
        this.product_quantity = quantity;
    }
    public void setPrice(double price) {
        this.product_purchase_price = price;
    }

//    public String getKey() {
//        return key;
//    }
//
//    public void setKey(String key) {
//        this.key = key;
//    }
}
