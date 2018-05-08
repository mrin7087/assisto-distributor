package com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceDetails;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.epsonPrinter.PrinterDiscoveryActivity;
import com.techassisto.mrinmoy.assisto.epsonPrinter.ShowMsg;
import com.techassisto.mrinmoy.assisto.retailSales.RetailInvoiceDetails;
import com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceList.InvoiceActivity;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.Constants;
import com.techassisto.mrinmoy.assisto.utils.TenantInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.techassisto.mrinmoy.assisto.utilDeclaration.RoundClass.round;

public class RetailInvoiceDetailsActivity extends DashBoardActivity implements ReceiveListener {

    private Context mContext = null;
    private static final String TAG = "Assisto.InvoiceDetails";
    private Activity mActivity = null;
    private InvoiceDetailsAPITask mInvoiceDetailsAPITask  = null;
    private InvoiceDeleteAPITask mInvoiceDeleteAPITask = null;
    private View mProgressView = null;
    private ListView mListView = null;
    private JSONObject mProductDetails = null;
    private JSONObject mInvoiceDelete = null;
    private RetailInvoiceDetails retailInvoiceDetails;
    ArrayList<RetailInvoiceLineDetails> mModelList;
    RetailInvoiceAdapter mAdapter = null;
    String invoice_no;
    String STATUS = "NORMAL";
    int EDIT_PRODUCT = 1;
    private double totalQuantityReturned = 0.0;

    private InvoiceSaveTask mSubmitTask = null;

    private int mSavedInvoiceId = -1;

    //Printer related
    private String mTarget = null;
    private Printer  mPrinter = null;
    private static final int ADD_PRINTER_REQUEST = 2;
//    ADD_PRINTER_REQUEST

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_retail_invoice_details);

        mContext = this;

        Intent intent = this.getIntent();
        invoice_no=intent.getStringExtra("invoice_no");
        mActivity = this;
        mProgressView = findViewById(R.id.apiget_progress);
        mModelList = new ArrayList<RetailInvoiceLineDetails>();
        mListView = (ListView) findViewById(R.id.retailinvoicedetails);
        mAdapter = new RetailInvoiceAdapter(mActivity, mModelList);
        Log.i(TAG, "In Details Activity");
        mListView.setAdapter(mAdapter);
        Log.i(TAG, "In Function");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();

        getDetails();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (STATUS == "EDIT") {
                    RetailInvoiceLineDetails lineItem = mModelList.get(position);
//                    Toast.makeText(getApplicationContext(), "Service HSN: " + lineItem.getProduct_hsn(), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.setClass(RetailInvoiceDetailsActivity.this, EditProduct.class);
                    intent.putExtra("Service Name", lineItem.getProduct_name());
                    intent.putExtra("Original Qty", lineItem.getOriginal_qty());
                    intent.putExtra("Revised Qty", lineItem.getQuantity());
                    intent.putExtra("Line Position", String.valueOf(position));
                    startActivityForResult(intent, EDIT_PRODUCT);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please select edit from menu", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Get the printer address
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        mTarget = userPref.getString(Constants.UserPref.SP_PRINTER, null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PRODUCT){
            if (resultCode == Activity.RESULT_OK) {
                String quantity = data.getStringExtra("Revised Qty");
                String position = data.getStringExtra("Line Position");
                if (Float.parseFloat(quantity) == 0){
                    mModelList.remove(Integer.parseInt(position));
                    mAdapter.notifyDataSetChanged();
                    Log.i(TAG, "Position to delete: "+Integer.parseInt(position));
                }
                else {
                    RetailInvoiceLineDetails lineItem = mModelList.get(Integer.valueOf(position));
                    lineItem.setQuantity(quantity);
                    mAdapter.notifyDataSetChanged();
                }
                TextView invoiceTotalView = (TextView) findViewById(R.id.invoicetotalview);
                TextView invoiceRoundView = (TextView) findViewById(R.id.invoiceroundview);
                double billTotal = calculateBillTotal("Normal");
                double roundedTotal = round(billTotal,0);
                double roundedValue = roundedTotal - billTotal;
                invoiceTotalView.setText("Total: "+String.format("%.02f", roundedTotal));
                invoiceRoundView.setText("Round Off: "+String.format("%.02f", roundedValue));
                mAdapter.notifyDataSetChanged();
            }
        }
        else if (requestCode == ADD_PRINTER_REQUEST) {
            Log.i(TAG, "onActivityResult: ADD_PRINTER_REQUEST, resultCode: " + resultCode);
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    String target = data.getStringExtra(getString(R.string.title_target));
                    Toast.makeText(getApplicationContext(), target, Toast.LENGTH_SHORT).show();

                    if (target != null) {
                        mTarget = target;
                        SharedPreferences.Editor editor = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE).edit();
                        editor.putString(Constants.UserPref.SP_PRINTER, mTarget);
                        editor.commit();
                        Log.i(TAG, "mTarget : " + mTarget);
                    }
                }
            }
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_retail_invoice_details;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (STATUS == "NORMAL"){
            getMenuInflater().inflate(R.menu.action_bar_invoice_delete, menu);
        }
        else{
            getMenuInflater().inflate(R.menu.action_bar_invoice_edit, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_delete){
//            Toast.makeText(getApplicationContext(), "Clicked", Toast.LENGTH_SHORT).show();
            if (totalQuantityReturned == 0.0) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                builder.setTitle("DELETE INVOICE")
                        .setMessage("Are you sure you want to delete this invoice?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (retailInvoiceDetails.id != null) {
                                    //                        Toast.makeText(getApplicationContext(), "Yes: "+retailInvoiceDetails.id, Toast.LENGTH_SHORT).show();
                                    deleteInvoice();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), "Invoice not deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }else{
                Toast.makeText(getApplicationContext(), "Cannot delete invoice. Once sales return is made against an invoice, it cannot be deleted.", Toast.LENGTH_LONG).show();
            }
            return true;
        }
        else if (item.getItemId() == R.id.action_edit){
            if (totalQuantityReturned == 0.0) {
                STATUS = "EDIT";
                invalidateOptionsMenu();
                TextView invoiceTotalView = (TextView) findViewById(R.id.invoicetotalview);
                invoiceTotalView.setText("Total: " + retailInvoiceDetails.getTotal());
                invoiceTotalView.setVisibility(View.VISIBLE);

                TextView invoiceRoundView = (TextView) findViewById(R.id.invoiceroundview);
                invoiceRoundView.setText("Round: " + retailInvoiceDetails.getRoundoff());
                invoiceRoundView.setVisibility(View.VISIBLE);

                TextView invoice_noview = (TextView) findViewById(R.id.invoice_no);
                invoice_noview.setVisibility(View.GONE);
                TextView dateview = (TextView) findViewById(R.id.date);
                dateview.setVisibility(View.GONE);
                TextView cgstview = (TextView) findViewById(R.id.cgst);
                cgstview.setVisibility(View.GONE);
                TextView sgstview = (TextView) findViewById(R.id.sgst);
                sgstview.setVisibility(View.GONE);
                TextView totalview = (TextView) findViewById(R.id.billtotal);
                totalview.setText("Original Total: " + retailInvoiceDetails.getTotal());
                TextView roundview = (TextView) findViewById(R.id.billround);
                roundview.setVisibility(View.GONE);
            }else{
                Toast.makeText(getApplicationContext(), "Cannot edit invoice. Once sales return is made against an invoice, it cannot be edited.", Toast.LENGTH_LONG).show();
            }


        }
        else if ((item.getItemId() == R.id.action_update)){
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            double billTotal = calculateBillTotal("Normal");
            double rounded_total = round(billTotal,0);
            builder.setTitle("Update Invoice")
                    .setMessage("Update current invoice ?" +
                            "\nTotal: "+String.format("%.02f", rounded_total))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            updateInvoice();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return true;
        }
        else if ((item.getItemId() == R.id.action_update_pay_mode)){
            Intent intent = new Intent();
            intent.setClass(RetailInvoiceDetailsActivity.this, EditPaymentMode.class);
            intent.putExtra("Invoice ID", retailInvoiceDetails.id);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.action_add_printer) {
            Log.i(TAG, "Add Printer");

            Intent intent = new Intent();
            intent.setClass(this, PrinterDiscoveryActivity.class);
            startActivityForResult(intent, ADD_PRINTER_REQUEST);
        }
        else if (item.getItemId() == R.id.action_print) {
            Log.i(TAG, "Printing old invoice");
            if (mTarget == null) {
                Log.i(TAG, "Target is NULL.. return");
                Snackbar.make(findViewById(R.id.fab), "Add a Printer first to print!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            else {
                printInvoice();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //    private void updateInvoice(boolean printInvoice) {
    private void updateInvoice(){
        Log.i(TAG, "Save Invoice");

        if (mModelList.size() == 0) {
            Toast.makeText(getApplicationContext(), "Invoice is empty!!", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i=0; i<mModelList.size(); i++) {
            Log.i(TAG, mModelList.get(i).getProduct_name() + " qty: " + mModelList.get(i).getQuantity());
        }

        // Create the data to be submitted in API format
        Product productArr[] = new Product[mModelList.size()];
        Log.i(TAG, "productArr size:" + productArr.length);

        // Invoice Calculation
        double billTotal = 0;
        double billCGSTTotal= 0;
        double billSGSTTotal= 0;
        double billSubTotal = 0;
        for (int  i=0; i<mModelList.size(); i++) {
            Log.i(TAG, "Item Unit: "+mModelList.get(i).getUnit_id());
            double totalTaxPercent;
            double totalTaxDivider;
            double taxTotal; //to find out total tax
            double cgstTotal;
            double sgstTotal;
            double thisNonTaxTotal; //to store line total without tax
            double thisNonTaxEach; //to store item total without tax
//            ProductInfo pInfo = mModelList.get(i).getProduct();
            //boolean isTax = pInfo.rate.get(0).is_tax_included;
            productArr[i] = new Product();
            productArr[i].product_id = mModelList.get(i).getProduct_id();
            productArr[i].product_name = mModelList.get(i).getProduct_name();
            productArr[i].product_hsn = mModelList.get(i).getProduct_hsn();
            productArr[i].quantity = Double.parseDouble(mModelList.get(i).getQuantity());
//            productArr[i].inventory = mModelList.get(i).;
            productArr[i].unit_id = Float.parseFloat(mModelList.get(i).getUnit_id());
            productArr[i].unit_name = mModelList.get(i).getUnit();
            productArr[i].unit_multi = Double.parseDouble(mModelList.get(i).getUnit_multi());
            productArr[i].sales = Double.parseDouble(mModelList.get(i).getSales_price());
            productArr[i].is_tax = mModelList.get(i).getIs_tax_included();
            productArr[i].discount_amount = Double.parseDouble(mModelList.get(i).getDiscount_amount());
            productArr[i].cgst_p = Double.parseDouble(mModelList.get(i).getCgst_percent());
            productArr[i].sgst_p = Double.parseDouble(mModelList.get(i).getSgst_percent());
            double thisTotal = productArr[i].sales * productArr[i].quantity; //to store line total with tax
//            if (productArr[i].is_tax) {
//
//                totalTaxPercent= productArr[i].cgst_p+ productArr[i].sgst_p;
//                totalTaxDivider=(100+totalTaxPercent)/100;
//                taxTotal=round(thisTotal-thisTotal/totalTaxDivider,2);
//                cgstTotal=round(taxTotal/2,2);
//                sgstTotal=round(taxTotal/2,2);
////                billTotal+=round(thisTotal,2);
////                billSubTotal+=thisTotal-taxTotal;
//                thisNonTaxTotal = round(thisTotal-taxTotal, 2);
//            }
//            else{
                cgstTotal=round((thisTotal*productArr[i].cgst_p)/100,2);
                sgstTotal=round((thisTotal*productArr[i].sgst_p)/100,2);
                taxTotal = cgstTotal + sgstTotal;
                thisNonTaxTotal = thisTotal;
//                billTotal+=thisTotal+taxTotal;
//                billSubTotal+=thisNonTaxTotal ;
                thisTotal = thisTotal+taxTotal;
//            }
            productArr[i].cgst_v = cgstTotal;
            productArr[i].sgst_v = sgstTotal;
            productArr[i].taxable_total = thisNonTaxTotal;
            thisNonTaxEach = round((thisNonTaxTotal/productArr[i].quantity),2);
            productArr[i].sales_before_tax = thisNonTaxEach;
            productArr[i].line_total = thisTotal;
            billSubTotal+= round(productArr[i].sales_before_tax* productArr[i].quantity,2);
            billCGSTTotal+=cgstTotal;
            billSGSTTotal+=sgstTotal;

//            Log.i(TAG, "Vendor Taxable Value: "+productArr[i].sales_after_tax);

            // TODO Consider discount and multiple sales rate.

        }

        billTotal = round(billCGSTTotal + billSGSTTotal + billSubTotal,2);

        double rounded_total = round(billTotal,0);
        double round_value = rounded_total - billTotal;

        InvoiceDetails oldInvoice = new InvoiceDetails();
        oldInvoice.invoice_pk = retailInvoiceDetails.getId();
        oldInvoice.bill_details = productArr;
        oldInvoice.subtotal = billSubTotal;
        oldInvoice.cgsttotal = billCGSTTotal;
        oldInvoice.sgsttotal=billSGSTTotal;
        oldInvoice.total = rounded_total;
        oldInvoice.roundoff = round_value;
//        newInvoice.warehouse = mWarehouseId;
//        newInvoice.paymentmode = mPaymentModeId;
        oldInvoice.calltype = "mobileedit";

        showProgress(true);
        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            Log.i(TAG, "Start task to save new invoice...");
            mSubmitTask = new InvoiceSaveTask(oldInvoice, authToken);
            mSubmitTask.execute((Void) null);
        } else {
            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
        }
    }

    private double calculateBillTotal(String sender) {
        Log.i(TAG, "Remaining list size: " + mModelList.size());
        double billGrossTotal = 0;

        for (int i=0; i<mModelList.size(); i++) {
            double thisLineTotal = 0;
            double thisQuantity = 0;
            double thisCGSTP = 0;
            double thisSGSTP = 0;
            double thisCGSTV = 0;
            double thisSGSTV = 0;
            double thisTotalSalesPrice = 0;
//            boolean is_tax_included;
            thisCGSTP = Double.parseDouble(mModelList.get(i).getCgst_percent());
            thisSGSTP = Double.parseDouble(mModelList.get(i).getSgst_percent());
            thisQuantity = Double.parseDouble(mModelList.get(i).getQuantity());
//            is_tax_included = mModelList.get(i).getIs_tax_included();

//            if (is_tax_included){
//                thisLineTotalWOTax = Double.parseDouble(mModelList.get(i).getSales_price()) * thisQuantity;
//            }
//            else{
            thisTotalSalesPrice = round(Double.parseDouble(mModelList.get(i).getSales_price()) * thisQuantity,2);
            thisCGSTV = round(thisTotalSalesPrice*thisCGSTP/100,2);
            thisSGSTV = round(thisTotalSalesPrice*thisSGSTP/100,2);
            thisLineTotal = round(thisTotalSalesPrice + thisCGSTV + thisSGSTV,2);
//            }
            mModelList.get(i).setCgst_value(String.valueOf(thisCGSTV));
            mModelList.get(i).setSgst_value(String.valueOf(thisSGSTV));
            mModelList.get(i).setLine_total(String.valueOf(thisLineTotal));

            Log.i(TAG, "Line CGST: " + thisCGSTV);
            Log.i(TAG, "Line SGST: " + thisSGSTV);
            Log.i(TAG, "Line Total: " +thisLineTotal);
            billGrossTotal += thisLineTotal;
        }
//        billGrossTotal = round(billGrossTotal,0);

//        return new double[]{billGrossTotal, billTaxableTotal};
        return (billGrossTotal);
    }

    private class Product {
        String product_id;
        String product_name;
        String product_hsn;
        boolean inventory;
        double quantity;
        float unit_id;
        String unit_name;
        double unit_multi;
        double sales;
        double sales_before_tax;
        boolean is_tax;
        double discount_amount;
        double cgst_p;
        double cgst_v;
        double sgst_p;
        double sgst_v;
        double taxable_total;
        double line_total;
    }

    private class InvoiceDetails {
//        String customer_phone;
//        String customer_name;
//        String customer_address;
//        String customer_email;

        Product[] bill_details;
        String invoice_pk;
        double subtotal;
        double cgsttotal;
        double sgsttotal;
        double total;
        double roundoff;
        String calltype;
    }



    public void getDetails(){

        Log.i(TAG, "In Get Details");
        if (mInvoiceDetailsAPITask != null){
            return;
        }

        showProgress(true);

        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            mInvoiceDetailsAPITask= new InvoiceDetailsAPITask (authToken);
            mInvoiceDetailsAPITask.execute((Void) null);
        }
        else{
            showProgress(false);
            Log.i(TAG, "Progress Error Toast");
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
    }

    public class InvoiceDetailsAPITask extends AsyncTask <Void, Void, Integer>{

        private String targetURL = Constants.SERVER_ADDR + APIs.retail_invoice_no_details+ "?invoice_no=" + invoice_no;
        private final String mToken;

        InvoiceDetailsAPITask(String token){mToken = token;}

        @Override
        protected Integer doInBackground(Void... params) {

            Log.i(TAG, "In do in background");
            String authHeader = "{\"authorization\": \"jwt " + mToken + "\"}";
            JSONObject authJson;
            try{
                authJson = new JSONObject(authHeader);
            }catch (Exception e){
                return Constants.Status.ERR_UNKNOWN;
            }

            Log.i(TAG, "URL: "+targetURL);

            StringBuffer response = new StringBuffer();
            HttpURLConnection httpConnection = null;

            try {
                URL targetUrl = new URL(targetURL);
                httpConnection = (HttpURLConnection) targetUrl.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Authorization", "jwt " + mToken);
                httpConnection.setConnectTimeout(10000);
                httpConnection.connect();

                Log.i(TAG, "response code:" + httpConnection.getResponseCode());
                if (httpConnection.getResponseCode() != 200) {
                    return Constants.Status.ERR_INVALID;
                }

                InputStream is= httpConnection.getInputStream();
                BufferedReader rd= new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line =rd.readLine())!= null){
                    response.append(line);
                }

                rd.close();

                Log.i(TAG, "response: " + response);

                return parseDetailsInfo(response.toString());
            }catch (MalformedURLException e) {
                e.printStackTrace();
                return Constants.Status.ERR_NETWORK;

            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return Constants.Status.ERR_NETWORK;
            }

            catch (IOException e) {
                e.printStackTrace();
                return Constants.Status.ERR_UNKNOWN;
            }finally {

                if(httpConnection != null) {
                    httpConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(final Integer status) {
            mInvoiceDetailsAPITask = null;
            showProgress(false);
            Log.i(TAG, "Status: "+status);
            if (status == Constants.Status.OK){
                populateDetailsInfo();
            }
            else{
                Log.i(TAG, "Status Error Toast");
                Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onCancelled() {
            mInvoiceDetailsAPITask = null;
            showProgress(false);
        }

        private int parseDetailsInfo(String details){
            Log.i(TAG, "In Parse String Details");
            try{
                mProductDetails = new JSONObject(details);
                if (mProductDetails.length() == 0){
                    Log.i(TAG, "Vendor Details Length: "+ mProductDetails.length());
//                    Toast.makeText(getApplicationContext(), "No Invoices found", Toast.LENGTH_SHORT).show();
                    mProductDetails = null;
                    return Constants.Status.ERR_INVALID;
                }
            }catch (Exception e){
                return Constants.Status.ERR_UNKNOWN;
            }

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mAdapter.notifyDataSetChanged();
//                }
//            });

            return Constants.Status.OK;
        }

        private void populateDetailsInfo(){
            mModelList.clear();
//            Gson gson = new GsonBuilder().serializeNulls().create();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").serializeNulls().create();
            retailInvoiceDetails= gson.fromJson(mProductDetails.toString(), RetailInvoiceDetails.class);
            List<RetailInvoiceLineDetails> allItems = retailInvoiceDetails.getLine_items();

            //Set Invoice Details
            TextView invoiceView = (TextView) findViewById(R.id.invoice_no);
            invoiceView.setText(invoice_no);
            TextView dateView = (TextView) findViewById(R.id.date);
            dateView.setText(retailInvoiceDetails.getDate());
            TextView cgstView = (TextView) findViewById(R.id.cgst);
            cgstView.setText(retailInvoiceDetails.getCgsttotal());
            TextView sgstView = (TextView) findViewById(R.id.sgst);
            sgstView.setText(retailInvoiceDetails.getSgsttotal());
            TextView billtotalView = (TextView) findViewById(R.id.billtotal);
            billtotalView.setText(retailInvoiceDetails.getTotal());
            TextView billroundView = (TextView) findViewById(R.id.billround);
            billroundView.setText(retailInvoiceDetails.getRoundoff());


            Log.i(TAG, "In Populate Details Info");
            for (int i=0; i<allItems.size(); i++){
                try{
                    mModelList.add(new RetailInvoiceLineDetails(allItems.get(i)));
                    try {
                        totalQuantityReturned+= Double.parseDouble(allItems.get(i).getQuantity_returned());
                    }catch (Exception e){

                    }
                }catch (Exception e){

                }
            }

            mAdapter.notifyDataSetChanged();
        }
    }


    public void deleteInvoice(){

        Log.i(TAG, "In Delete Invoice");
        if (mInvoiceDeleteAPITask  != null){
            return;
        }

        showProgress(true);

        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            mInvoiceDeleteAPITask = new InvoiceDeleteAPITask (authToken);
            mInvoiceDeleteAPITask.execute((Void) null);
        }
        else{
            showProgress(false);
            Log.i(TAG, "Progress Error Toast");
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();
        }
    }

    public class InvoiceDeleteAPITask extends AsyncTask <Void, Void, Integer>{

        String invoice_id=retailInvoiceDetails.id;

        private String targetURL = Constants.SERVER_ADDR + APIs.retail_invoice_delete ;
        private final String mToken;

        InvoiceDeleteAPITask (String authToken){
            mToken = authToken;
        }

        @Override
        protected Integer doInBackground(Void... params) {

            Log.i(TAG, "In do in background");
            String authHeader = "{\"authorization\": \"jwt " + mToken + "\"}";
            JSONObject authJson;
            try{
                authJson = new JSONObject(authHeader);
            }catch (Exception e){
                return Constants.Status.ERR_UNKNOWN;
            }

            Log.i(TAG, "URL: "+targetURL);

            StringBuffer response = new StringBuffer();
            HttpURLConnection httpConnection = null;

            try {
                targetURL += ("?invoice_id=" + invoice_id);
                targetURL += ("&calltype=delete");
                URL targetUrl = new URL(targetURL);
                httpConnection = (HttpURLConnection) targetUrl.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Authorization", "jwt " + mToken);
                httpConnection.setConnectTimeout(10000);
                httpConnection.connect();

                Log.i(TAG, "response code:" + httpConnection.getResponseCode());
                if (httpConnection.getResponseCode() != 200) {
                    return Constants.Status.ERR_INVALID;
                }

                InputStream is= httpConnection.getInputStream();
                BufferedReader rd= new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line =rd.readLine())!= null){
                    response.append(line);
                }

                rd.close();
                return parseDetailsInfo(response.toString());
            }catch (MalformedURLException e) {
                e.printStackTrace();
                return Constants.Status.ERR_NETWORK;

            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return Constants.Status.ERR_NETWORK;
            }

            catch (IOException e) {
                e.printStackTrace();
                return Constants.Status.ERR_UNKNOWN;
            }finally {

                if(httpConnection != null) {
                    httpConnection.disconnect();
                }
            }
        }



        @Override
        protected void onPostExecute(final Integer status) {
            mInvoiceDeleteAPITask = null;
            showProgress(false);
            Log.i(TAG, "Status: "+status);
            if (status == Constants.Status.OK){
                checkDeleteStatus();
            }
            else{
                Log.i(TAG, "Status Error Toast");
                Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onCancelled() {
            mInvoiceDetailsAPITask = null;
            showProgress(false);
        }

        private int parseDetailsInfo(String details){
            Log.i(TAG, "In Parse String Details");
            try{
                mInvoiceDelete = new JSONObject(details);
                if (mInvoiceDelete.length() == 0){
                    Log.i(TAG, "Invoice delete Length: "+ mInvoiceDelete.length());
//                    Toast.makeText(getApplicationContext( ), "No Invoices found", Toast.LENGTH_SHORT).show();
                    mInvoiceDelete = null;
                    return Constants.Status.ERR_INVALID;
                }
            }catch (Exception e){
                return Constants.Status.ERR_UNKNOWN;
            }

            return Constants.Status.OK;
        }

        private void checkDeleteStatus(){
//            Toast.makeText(getApplicationContext(), "Checking delete status", Toast.LENGTH_SHORT).show();
            try {
                String is_delete = mInvoiceDelete.getString("success");
                if (is_delete.length()>0){
                    Toast.makeText(getApplicationContext(), "Invoice is deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setClass(RetailInvoiceDetailsActivity.this, InvoiceActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Kindly try again.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Hmm!! Could not delete. Note that if sales return is made against an invoice, that invoice cannot be deleted.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }


    }


    public class InvoiceSaveTask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.InvoiceEditTask";
        private static final String targetURL = Constants.SERVER_ADDR + APIs.retail_invoice_edit;

        private final InvoiceDetails mInvoice;
        private final String mToken;

        InvoiceSaveTask(InvoiceDetails invoice, String authToken) {
            mInvoice = invoice;
            mToken = authToken;
        }

        @Override
        protected Integer doInBackground(Void... uInfo) {
            Log.i(TAG, "doInBackground");
            StringBuffer response = new StringBuffer();
            Gson gson = new Gson();
            String invoiceJson = gson.toJson(mInvoice);
            Log.i(TAG, "Invoice details:" + invoiceJson);
//            Log.i(TAG, "Token: " + mToken);

            Log.i(TAG, "try to POST HTTP request");
            HttpURLConnection httpConnection = null;
            try {
                URL targetUrl = new URL(targetURL);
                httpConnection = (HttpURLConnection) targetUrl.openConnection();

                httpConnection.setDoOutput(true);
                httpConnection.setRequestProperty("Authorization", "jwt " + mToken);
                httpConnection.setRequestMethod("POST");
                httpConnection.setRequestProperty("Content-Type", "application/json");
                httpConnection.setConnectTimeout(10000); //10secs
                httpConnection.connect();

                OutputStream outputStream = httpConnection.getOutputStream();
                outputStream.write(invoiceJson.getBytes());
                outputStream.flush();


                //Process Request
                Log.i(TAG, "response code:" + httpConnection.getResponseCode());
                if (httpConnection.getResponseCode() != 200) {
                    Log.e(TAG, "Failed : HTTP error code : " + httpConnection.getResponseCode());
                    return Constants.Status.ERR_INVALID;
                }

                //Received Response
                InputStream is = httpConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    //response.append('\r');
                }
                rd.close();

                Log.i(TAG, response.toString());
                try {
                    JSONObject jsonObj = new JSONObject(response.toString());
                    mSavedInvoiceId = jsonObj.getInt("id");
                    Log.i(TAG, "Saved Invoice id:" + mSavedInvoiceId);
                } catch (Exception e) {
                    return Constants.Status.ERR_INVALID;
                }
                return Constants.Status.OK;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return Constants.Status.ERR_NETWORK;

            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return Constants.Status.ERR_NETWORK;
            } catch (IOException e) {
                e.printStackTrace();
                return Constants.Status.ERR_UNKNOWN;
            } finally {

                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(final Integer status) {
            mSubmitTask = null;
            showProgress(false);

            if (status == Constants.Status.OK) {
                Log.i(TAG, "Successfully Updated Invoice");
                Toast.makeText(getApplicationContext(), "Successfully Updated Invoice!!", Toast.LENGTH_SHORT).show();

//                mModelList.clear();
//                mAdapter.notifyDataSetChanged();

                Intent intent = new Intent();
                intent.setClass(RetailInvoiceDetailsActivity.this, InvoiceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

//                mCurrentInvoice = mInvoice;

            } else if (status == Constants.Status.ERR_INVALID) {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mSubmitTask = null;
            showProgress(false);
        }
    }

    private void printInvoice() {
        Log.i(TAG, "printInvoice");
        Log.i(TAG, "mCurrentInvoice not null!!");

        boolean success = runPrintReceiptSequence();

        Log.i(TAG, "runPrintReceiptSequence, res: " + success);

        if (success == false) {
            Toast.makeText(getApplicationContext(), "Data saved, but failed to print", Toast.LENGTH_LONG).show();
        }

    }

    private boolean runPrintReceiptSequence() {
        if (!initializeObject()) {
            Log.i(TAG, "initializeObject failed!!");
            return false;
        }

        if (!createReceiptData()) {
            Log.i(TAG, "createReceiptData failed!!");
            finalizeObject();
            return false;
        }

        if (!printData()) {
            Log.i(TAG, "printData failed!!");
            finalizeObject();
            return false;
        }

        return true;
    }

    private boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();

        dispPrinterWarnings(status);

        if (!isPrintable(status)) {
            ShowMsg.showMsg(makeErrorMessage(status), mContext);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "sendData", mContext);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        return true;
    }

    private boolean initializeObject() {
        try {
            mPrinter = new Printer(Printer.TM_M10, Printer.MODEL_ANK, mContext);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "Printer", mContext);
            return false;
        }

        mPrinter.setReceiveEventListener(this);

        return true;
    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.clearCommandBuffer();

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }

    private boolean connectPrinter() {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            return false;
        }

        try {
            mPrinter.connect(mTarget.toString(), Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "activity_connect_barcode_printer", mContext);
//            Toast.makeText(getApplicationContext(), "Oops!! Could -not activity_connect_barcode_printer printer. Invoice is saved.", Toast.LENGTH_LONG).show();
            SharedPreferences.Editor editor = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE).edit();
            editor.putString(Constants.UserPref.SP_PRINTER, null);
            editor.commit();
            mTarget = null;
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
            ShowMsg.showException(e, "beginTransaction", mContext);
        }

        if (isBeginTransaction == false) {
            try {
                mPrinter.disconnect();
            }
            catch (Epos2Exception e) {
                // Do nothing
                return false;
            }
        }

        return true;
    }

    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        try {
            mPrinter.endTransaction();
        }
        catch (final Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showException(e, "endTransaction", mContext);
                }
            });
        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showException(e, "disconnect", mContext);
                }
            });
        }

        finalizeObject();
    }

    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        }
        else if (status.getOnline() == Printer.FALSE) {
            return false;
        }
        else {
            //print available
        }

        return true;
    }

    private boolean createReceiptData() {
        String method = "";
        StringBuilder textData = new StringBuilder();

        if (mPrinter == null) {
            return false;
        }

        try {
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

            method = "addFeedLine";
            mPrinter.addFeedLine(1);

            TenantInfo tenantInfo = null;
            SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
            String tenant = userPref.getString(Constants.UserPref.SP_TENANT, null);
            if (tenant != null) {
                Gson gson = new GsonBuilder().serializeNulls().create();
                tenantInfo = gson.fromJson(tenant, TenantInfo.class);
                Log.i(TAG, "Tenant:" + tenantInfo.tenant_name + " First Name:" + tenantInfo.first_name);
            }
            Double totalDiscount = 0.0;
            String invoiceDateString = retailInvoiceDetails.getDate();
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(invoiceDateString);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String printDate = dateFormat.format(date);

            // PRODUCT DETAILS
            textData.append("---------------------------------------\n");
            textData.append("Tax Invoice                         Date\n");
//            textData.append("1707090001                     21-07-2017\n");
            textData.append(invoice_no+"                     "+printDate+"\n");
            textData.append(tenantInfo.tenant_name+"\n"); //Tenant Name
            textData.append(retailInvoiceDetails.warehouse_address+","+retailInvoiceDetails.warehouse_city+ "\n");   //Warehouse Address
//            textData.append(mWarehouseAddress+ "\n");   //Warehouse Address
//            textData.append(mWarehouseStateName+"\n");  //Warehouse State
//            textData.append("         GSTIN:19A   WPKJ14741017B78Z     \n");
            if (TextUtils.isEmpty(tenantInfo.tenant_gst)){

            }
            else {
                textData.append(" GSTIN:" + tenantInfo.tenant_gst + "\n");
            }
            textData.append("Item\n");
            //Original Text
//            textData.append("HSN   Qty   Unit   Dcnt   Rate\n");
//            textData.append("CGST%  CGST AMT   SGST%  SGST AMT  Total\n");
            //Revised Text
            textData.append("HSN   Qty   Unit   Dcnt   GST   Total\n");
            textData.append("---------------------------------------\n");
            method = "addText";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.TRUE, Printer.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            for (int i=0; i < retailInvoiceDetails.getLine_items().size() ; i++) {
                RetailInvoiceLineDetails line_detail = retailInvoiceDetails.getLine_items().get(i);

                //Line 1
                textData.append(line_detail.getProduct_name() + "\n");
                if (TextUtils.isEmpty(line_detail.getProduct_hsn())) {
                    textData.append("     ");
                }
                else {
                    textData.append(line_detail.getProduct_hsn() + " ");
                }
                textData.append(line_detail.getQuantity() + "  ");
                textData.append(line_detail.getUnit() + "  ");
                //Original Text
//                textData.append(String.format("%.2f",product.discount_amount )+ "  ");
//                textData.append(String.format("%.2f",product.sales_after_tax) );
//                textData.append(String.format("%.2f",product.discount_amount * product.quantity)+ "  ");
//                Double zero_discount = 0.00;
                textData.append(String.format("%.2f",Double.parseDouble(line_detail.getDiscount_amount()) * Double.parseDouble(line_detail.getQuantity()))+ "  ");
                textData.append(Double.parseDouble(line_detail.getCgst_percent())*2  + "  ");
                textData.append(String.format("%.2f",Double.parseDouble(line_detail.getLine_total())) );
                textData.append("\n");
                totalDiscount+= Double.parseDouble(line_detail.getDiscount_amount()) * Double.parseDouble(line_detail.getQuantity());
            }

            textData.append("---------------------------------------\n");
            method = "addText";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            // SUBTOTAL
            textData.append("SUBTOTAL: ");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
            textData.append(String.format("%.2f",Double.parseDouble(retailInvoiceDetails.getSubtotal()))+ "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            // CGST TOTAL
            if (Double.parseDouble(retailInvoiceDetails.getCgsttotal()) > 0) {
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                textData.append("CGST: ");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());

                mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
                textData.append(String.format("%.2f", Double.parseDouble(retailInvoiceDetails.getCgsttotal())) + "\n");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
            }

            // SGST TOTAL
            if (Double.parseDouble(retailInvoiceDetails.getSgsttotal()) > 0) {
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                textData.append("SGST: ");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());

                mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
                textData.append(String.format("%.2f", Double.parseDouble(retailInvoiceDetails.getSgsttotal())) + "\n");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
            }

            // ROUND OFF
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            textData.append("Round off: ");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
            textData.append(String.format("%.2f",Double.parseDouble(retailInvoiceDetails.getRoundoff()))+ "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            // TOTAL
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            method = "addTextSize";
            mPrinter.addTextSize(2, 2);
            method = "addText";
            textData.append("TOTAL: ");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
            textData.append(String.format("%.2f",Double.parseDouble(retailInvoiceDetails.getTotal()))+ "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            // TOTAL DISCOUNT
            if (totalDiscount > 0.0) {
                textData.append("\n");
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                method = "addTextSize";
                mPrinter.addTextSize(1, 1);
                mPrinter.addTextFont(Printer.FONT_B);
                mPrinter.addTextStyle(0, 0, 1, 4);
                method = "addText";
                textData.append("YOUR TOTAL SAVINGS: ");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());

                mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
                textData.append(String.format("%.2f",totalDiscount)+ "\n");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
            }



            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            method = "addFeedLine";
            mPrinter.addFeedLine(1);

            textData.append("---------------------------------------\n");

            //This is used to print barcode of invoice id.
            int barcodeWidth=2;
            int barcodeHeight=100;
            method = "addFeedLine";
            mPrinter.addFeedLine(2);
            method = "addBarcode";
            mPrinter.addBarcode(String.valueOf(invoice_no),
                    Printer.BARCODE_CODE39,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            ShowMsg.showException(e, method, mContext);
            return false;
        }

        textData = null;

        return true;
    }

    private String makeErrorMessage(PrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += getString(R.string.handlingmsg_err_autocutter);
            msg += getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }

    private void dispPrinterWarnings(PrinterStatusInfo status) {
        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += getString(R.string.handlingmsg_warn_battery_near_end);
        }

        Toast.makeText(getApplicationContext(), warningsMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        Log.i(TAG, "onPtrReceive Code: "+code);
        if (status == null){
            Log.i(TAG, "onPtrReceive status is null");
        }
        else{
            Log.i(TAG, "onPtrReceive status not null");
        }
        Log.i(TAG, "onPtrReceive printjobid: "+printJobId);

//        if (printJobId == null ){
//            return;
//        }
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult(code, makeErrorMessage(status), mContext);

                dispPrinterWarnings(status);

                //updateButtonState(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                    }
                }).start();
            }
        });
    }


}



