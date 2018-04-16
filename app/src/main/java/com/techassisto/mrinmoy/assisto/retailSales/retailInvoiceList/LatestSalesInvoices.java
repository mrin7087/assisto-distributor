package com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sayantan on 10/8/17.
 */

public class LatestSalesInvoices {
    private String id;
    private String invoice_id;
//    private String date;
    private Date date;
    private String cgsttotal;
    private String sgsttotal;
    private String total;
    private String payment_name;
    public  LatestSalesInvoices (String  id, String invoice_id, String date, String cgsttotal, String sgsttotal, String total, String payment_name){
        this.id=id;
        this.invoice_id = invoice_id;
//        this.date = date;
        this.setDate(date);
        this.cgsttotal = cgsttotal;
        this.sgsttotal = sgsttotal;
        this.total = total;
        this.payment_name = payment_name;
    }

    public String getId() {return id;}

//    public String getDate() {return date;}

    public String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MM-yyyy")   ;
        return dateFormat.format(date);}

    public String getInvoice_id(){return invoice_id;}

    public String getCgsttotal() {
        return cgsttotal;
    }

    public String getSgsttotal() {
        return sgsttotal;
    }

    public String getTotal() {
        return total;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPayment_name(String payment_name) {
        this.payment_name = payment_name;
    }

    public String getPayment_name() {
        return payment_name;
    }

    public void setInvoice_id(String invoice_id) {
        this.invoice_id = invoice_id;
    }

//    public void setDate(String date) {
//        this.date = date;
//    }

    public void setDate(String date) {

        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd");
        try{
            this.date = dateFormat.parse(date);
        }catch (ParseException e){

        }
    }

    public void setCgsttotal(String cgsttotal) {
        this.cgsttotal = cgsttotal;
    }

    public void setSgsttotal(String sgsttotal) {
        this.sgsttotal = sgsttotal;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
