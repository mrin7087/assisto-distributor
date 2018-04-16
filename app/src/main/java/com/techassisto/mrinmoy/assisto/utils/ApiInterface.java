package com.techassisto.mrinmoy.assisto.utils;

/**
 * Created by sayantan on 10/2/18.
 */

import com.techassisto.mrinmoy.assisto.PaymentModeOption;
import com.techassisto.mrinmoy.assisto.retailSales.SalesInvoiceDetail;
import com.techassisto.mrinmoy.assisto.retailSales.retailDailyReport.DailySalesPayment;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {
    static final String retailPaymentModeGETUrl   = APIs.retail_payment_mode_get;
    static final String retail_invoice_no_details = APIs.retail_invoice_no_details;
    static final String retail_daily_sales_report = APIs.retail_daily_sales_report;


    @GET(retailPaymentModeGETUrl)
    Call<List<PaymentModeOption>> getRetailPaymentMode(@Header("Authorization") String authorization);

    @GET(retail_invoice_no_details)
    Call<SalesInvoiceDetail> getRtailInvoiceDetail(@Query("invoice_no") String invoice_no, @Header("Authorization") String authorization);

    @GET(retail_daily_sales_report)
    Call<List<DailySalesPayment>> geRetailDailySalesReport(@Query("calltype") String calltype, @Query("date") String date, @Header("Authorization") String authorization);

//    @GET("movie/{id}")
//    Call<MoviesResponse> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);
}
