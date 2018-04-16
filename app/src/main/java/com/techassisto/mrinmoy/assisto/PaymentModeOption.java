package com.techassisto.mrinmoy.assisto;

/**
 * Created by sayantan on 10/2/18.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaymentModeOption {
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("default")
    @Expose
    private Boolean _default;
    @SerializedName("id")
    @Expose
    private int id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getDefault() {
        return _default;
    }

    public void setDefault(Boolean _default) {
        this._default = _default;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PaymentModeOption getObject(){
        return this;
    }

//    public List<PaymentModeOption> getResults() {
//        return results;
//    }
}
