package com.techassisto.mrinmoy.assisto.retailSales.retailDashboard;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sayantan on 26/8/17.
 */

public class RetailSalesSummary {

    private Date date;
    private String total;

    public String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MM-yyyy")   ;
        return dateFormat.format(date);}

    public void setDate(String date) {

        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd");
        try{
            this.date = dateFormat.parse(date);
        }catch (ParseException e){

        }
    }

    public String getTotal() {
        return total;
    }

    public double getDoubleTotal() {
        try{
            return Double.valueOf(total);
        }catch (Exception e){
            return 0.0;
        }
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
