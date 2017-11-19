package com.techassisto.mrinmoy.assisto.vendor;

/**
 * Created by Mrinmoy on 6/8/2017.
 */

public class VendorListModel {
    private String name;
    private String key;
    private String id;

    public VendorListModel(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public VendorListModel(String name, String key, String id) {
        this.name = name;
        this.key = key;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
