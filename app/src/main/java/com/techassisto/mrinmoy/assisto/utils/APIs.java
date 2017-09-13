package com.techassisto.mrinmoy.assisto.utils;

/**
 * Created by Mrinmoy on 6/7/2017.
 */

public class APIs {
    // GET Token
    public static final String get_token                  = "getthetoken/";

    // TENANT Details
    public static final String tenant_get                  = "tenant-user-metadata/";

    // Vendor APIs
    public static final String vendor_get                  = "master/vendor/getdata/";
    public static final String vendor_new_post             = "master/vendor/getdata/";

    // Warehouse get
    public static final String warehouse_get               = "master/warehouse/getdata/";

    // Retail Payment get
    public static final String payment_mode_get               = "retailsales/paymentmode/";

    // Invoice Retail Sales APIs
    public static final String product_barcode_get         = "retailsales/invoice/api/getproductbarcode";
    public static final String product_autocomplete_get    = "retailsales/invoice/api/getproduct";
    public static final String product_id_get              = "retailsales/invoice/api/getproduct/details";
    public static final String retail_invoice_save         = "retailsales/invoice/save/";
    public static final String retail_invoice_list         = "retailsales/invoicelist/listall/";
    public static final String retail_invoice_no_details   = "retailsales/invoice/invoicenodetails/";

    public static final String retail_invoice_delete       = "retailsales/invoice/delete/";

    public static final String retail_sales_summary_graph  = "retailsales/salessummarygraph/";
}
