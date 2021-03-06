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

    // Try Download with Current Inventory
    public static final String current_inventory           = "inventory/getcurrentdata/";

    // Retail Payment get
    public static final String retail_payment_mode_get = "retailsales/paymentmode/";

    // Invoice Retail Sales APIs
    public static final String product_barcode_get         = "retailsales/invoice/api/getproductbarcode";
    public static final String product_autocomplete_get    = "retailsales/invoice/api/getproduct";
    public static final String product_id_get              = "retailsales/invoice/api/getproduct/details";
    public static final String retail_invoice_save         = "retailsales/invoice/save/";
    public static final String retail_invoice_list         = "retailsales/invoicelist/listall/app/";
    public static final String retail_invoice_no_details   = "retailsales/invoice/invoicenodetails/";

    public static final String retail_invoice_delete       = "retailsales/invoice/delete/";
    public static final String retail_invoice_edit         = "retailsales/invoiceedit/save/";

    public static final String retail_sales_summary_graph  = "retailsales/salessummarygraph/";

    public static final String retail_sales_eod_product    = "retailsales/eodsales/data/";

    public static final String retail_daily_sales_report   = "retailsales/retail-daily-sales/data/";

    public static final String retail_sales_return         = "retailsales/invoice/salesreturn/save/";

    // Purchase Receipt APIs
    public static final String purchase_product_autocomplete_get  = "purchase/receipt/api/getproduct";
    public static final String purchase_product_id_get       = "purchase/receipt/api/getproduct/details";
    public static final String purchase_product_barcode_get  = "purchase/receipt/api/getproduct/barcode";
    public static final String purchase_receipt_save         = "purchase/receipt/save/";
    public static final String vendor_autocomplete_get       = "master/vendor/autocomplete";


//    public static final String service_barcode_get         = "retailsales/invoice/api/getproductbarcode";
    public static final String service_autocomplete_get      = "servicesales/invoice/api/getproduct";
    public static final String service_id_get                = "servicesales/invoice/api/getservice/details";
    public static final String service_invoice_save          = "servicesales/invoice/save/";
    public static final String service_salespersons_get      = "servicesales/getsalesusers/";

    public static final String service_userreport_download   = "servicesales/user-wise-service/data/";

    //Product Detail API
    public static final String product_print_barcode         = "master/product/productbarcodeprint/";
}
