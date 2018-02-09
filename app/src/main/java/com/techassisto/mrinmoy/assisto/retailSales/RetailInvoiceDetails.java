package com.techassisto.mrinmoy.assisto.retailSales;

import com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceDetails.RetailInvoiceLineDetails;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by sayantan on 15/8/17.
 */

public class RetailInvoiceDetails {

    public String invoice_id;
    public String id;
    public Date date;
    public String subtotal;
    public String cgsttotal;
    public String sgsttotal;
    public String total;
    public String roundoff;
    public List<RetailInvoiceLineDetails> line_items;

    public List<RetailInvoiceLineDetails> getLine_items() {
        return line_items;
    }

    public String toString(){
        String retailInvoiceDetails = "InvoiceDetails:";
        retailInvoiceDetails += " invoiceNo: " + invoice_id;
        retailInvoiceDetails += " date: "+ date;
        retailInvoiceDetails += " cgst: " + cgsttotal;
        retailInvoiceDetails += " sgst: " + sgsttotal;
        retailInvoiceDetails += " total: " + total;

        return retailInvoiceDetails;
    }

    public String getInvoice_id() {
        return invoice_id;
    }

    public String getDate() {

        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MM-yyyy");
        return dateFormat.format(date);
    }

    public String getSubtotal() {
        return subtotal;
    }

    public String getCgsttotal() {
        return cgsttotal;
    }

    public String getSgsttotal() {
        return sgsttotal;
    }

    public String getTotal() {
        return total;
    }

    public String getRoundoff() {
        return roundoff;
    }

    public String getId() {
        return id;
    }
}
