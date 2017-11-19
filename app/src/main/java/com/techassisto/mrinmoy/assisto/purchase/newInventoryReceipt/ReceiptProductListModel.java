package com.techassisto.mrinmoy.assisto.purchase.newInventoryReceipt;

import com.techassisto.mrinmoy.assisto.PurchaseProductInfo;

/**
 * Created by sayantan on 25/10/17.
 */

public class ReceiptProductListModel {
    private PurchaseProductInfo mProduct;
    private String product_name;
    private int product_quantity;
    private double product_purchase_price;
    private double tsp;
    private double mrp;
    private int disc_type;
    private int disc_type2;
    private double disc;
    private double disc_2;
    private double cgst_p;
    private double sgst_p;

    final String[] discounttypes = {"Nil" , "Percent" , "Value"};

    public ReceiptProductListModel(PurchaseProductInfo product) {
        mProduct = product;
        this.product_name = product.product_name;
        this.product_quantity = product.selectedQuantity;
        this.product_purchase_price = product.purchase_rate;
        this.tsp = product.tsp;
        this.mrp = product.mrp;
        this.disc = product.disc;
        this.disc_type = product.disc_type;
        this.disc_2 = product.disc_2;
        this.disc_type2 = product.disc_type_2;
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

    public double getTsp() {
        return tsp;
    }

    public double getMrp() {
        return mrp;
    }

    public String getDisc_type() {
        return  discounttypes[disc_type];
    }

    public String getDisc_type2() {
        return discounttypes[disc_type2];
    }

    public double getDisc() {
        return disc;
    }

    public double getDisc_2() {
        return disc_2;
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


}
