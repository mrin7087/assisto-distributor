package com.techassisto.mrinmoy.assisto.utilDeclaration;

/**
 * Created by sayantan on 16/4/18.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ProductBarcodePrint {
    @SerializedName("rate")
    @Expose
    private String rate;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("sku")
    @Expose
    private String sku;
    @SerializedName("barcode")
    @Expose
    private String barcode;

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}

