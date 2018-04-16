package com.techassisto.mrinmoy.assisto.retailSales.retailDailyReport;

/**
 * Created by sayantan on 11/2/18.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DailySalesPayment {

    @SerializedName("payment_mode_id")
    @Expose
    private Long paymentModeId;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("payment_mode_name")
    @Expose
    private String paymentModeName;

    public Long getPaymentModeId() {
        return paymentModeId;
    }

    public String getPaymentModeIdString() {
        return String.valueOf(paymentModeId);
    }

    public void setPaymentModeId(Long paymentModeId) {
        this.paymentModeId = paymentModeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPaymentModeName() {
        return paymentModeName;
    }

    public void setPaymentModeName(String paymentModeName) {
        this.paymentModeName = paymentModeName;
    }

}