package com.techassisto.mrinmoy.assisto.retailSales.retailSalesReturn;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.techassisto.mrinmoy.assisto.BarcodeFiles.ScanActivity;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.epsonPrinter.PrinterDiscoveryActivity;
import com.techassisto.mrinmoy.assisto.epsonPrinter.ShowMsg;
import com.techassisto.mrinmoy.assisto.retailSales.LineItem;
import com.techassisto.mrinmoy.assisto.retailSales.SalesInvoiceDetail;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.ApiClient;
import com.techassisto.mrinmoy.assisto.utils.ApiInterface;
import com.techassisto.mrinmoy.assisto.utils.Constants;
import com.techassisto.mrinmoy.assisto.utils.TenantInfo;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.techassisto.mrinmoy.assisto.RoundClass.round;

/**
 * Created by sayantan on 11/2/18.
 */

public class RetailSalesReturnActivity extends DashBoardActivity implements ReceiveListener {

    private static final String TAG = "Assisto.RetSalesReturn";
    private Activity mActivity = null;
    private Context mContext = null;
//    private InvoiceDetailsAPITask mInvoiceDetailsAPITask  = null;
//    private InvoiceDeleteAPITask mInvoiceDeleteAPITask = null;
    private View mProgressView = null;
    private ListView mListView = null;
    private SalesInvoiceDetail mInvoiceDetail = null;
//    private RetailInvoiceDetails retailInvoiceDetails;
    ArrayList<LineItem> mLineItemList = new ArrayList<LineItem>();
    ArrayList<LineItem> mLineItemPrint = new ArrayList<LineItem>();
    RetailInvoiceListAdapter mAdapter = null;
    String invoice_no;
    private LinearLayout searchLayout;
    private LinearLayout detailLayout;
    private RelativeLayout returnDetailsLayout;

    private ReturnSaveTask mSubmitTask = null;
    private String mSavedInvoiceId = "-1";
    private boolean RETURN_ACTIVE = false;

    private static final int RETURN_PRODUCT = 1;
    private static final int ADD_PRINTER_REQUEST = 2;
    private static final int SCAN_PRODUCT_REQUEST = 3;

    //Printer related
    private String mTarget = null;
    private Printer mPrinter = null;
    private InvoiceDetails newInvoice = new InvoiceDetails();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();
        mActivity = this;
        mContext = this;
        mListView = (ListView) findViewById(R.id.retailinvoicedetails);
        mProgressView = findViewById(R.id.apiget_progress);
//        mAdapter = new RetailInvoiceListAdapter(mActivity, mLineItemList);
//        mListView.setAdapter(mAdapter);


        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.search_invoice);
                try {
                    invoice_no = editText.getText().toString();
                    if (invoice_no.trim().length() > 0){
                        getInvoiceDetail();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Hmm!! Kindly enter an invoice no", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Hmm!! Kindly enter an invoice no", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LineItem lineItem = mLineItemList.get(position);
//                    Toast.makeText(getApplicationContext(), "Service HSN: " + lineItem.getProduct_hsn(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setClass(RetailSalesReturnActivity.this, ReturnProductActivity.class);
                intent.putExtra("Product Name", lineItem.getProductName());
                intent.putExtra("Original Qty", lineItem.getQuantityAvailable());
                intent.putExtra("Revised Qty", lineItem.getCurrentQuantityReturned());
                intent.putExtra("Line Position", String.valueOf(position));
                startActivityForResult(intent, RETURN_PRODUCT);
            }
        });

        Button scanBtn = (Button) findViewById(R.id.scan_button);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(AddProduct.this, CodeScannerActivity.class);
//                startActivityForResult(intent, SCAN_PRODUCT_REQUEST);
                Intent intent = new Intent(RetailSalesReturnActivity.this, ScanActivity.class);
                startActivityForResult(intent, SCAN_PRODUCT_REQUEST);
            }
        });


        //Get the printer address
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        mTarget = userPref.getString(Constants.UserPref.SP_PRINTER, null);
    }
    @Override
    public int getLayoutResId() {
        return R.layout.activity_retail_invoice_return;
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

    public void getInvoiceDetail(){

        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);
            String authorization = "jwt " + authToken;
            Call<SalesInvoiceDetail> call = apiService.getRtailInvoiceDetail(invoice_no, authorization);
            call.enqueue(new Callback<SalesInvoiceDetail>() {
                @Override
                public void onResponse(Call<SalesInvoiceDetail> call, Response<SalesInvoiceDetail> response) {
//                    List<SalesInvoiceDetail> modesList = new ArrayList<>();
//                    for (int i = 0; i < response.body().size(); i++) {
//                        modesList.add(response.body().get(i));
//                    }
                    try {
                        searchLayout = (LinearLayout) findViewById(R.id.search_layout);
                        detailLayout = (LinearLayout) findViewById(R.id.details_layout);
                        searchLayout.setVisibility(View.GONE);
                        detailLayout.setVisibility(View.VISIBLE);

                        mInvoiceDetail = response.body();

                        TextView invoiceView = (TextView) findViewById(R.id.invoice_no);
                        invoiceView.setText(invoice_no);

                        TextView dateView = (TextView) findViewById(R.id.date);
                        dateView.setText(mInvoiceDetail.getDate());

                        TextView cgstView = (TextView) findViewById(R.id.cgst);
                        cgstView.setText(mInvoiceDetail.getCgsttotal());
                        TextView sgstView = (TextView) findViewById(R.id.sgst);
                        sgstView.setText(mInvoiceDetail.getSgsttotal());
                        TextView billtotalView = (TextView) findViewById(R.id.billtotal);
                        billtotalView.setText(mInvoiceDetail.getTotal());
                        TextView billroundView = (TextView) findViewById(R.id.billround);
                        billroundView.setText(mInvoiceDetail.getRoundoff());

                        mLineItemList = mInvoiceDetail.getLineItems();

                        for (int i = 0; i < mLineItemList.size(); i++) {
                            Log.i(TAG, "Line Detail:" + mLineItemList.get(i).getProductName());
                        }
                        mAdapter = new RetailInvoiceListAdapter(mActivity, mLineItemList);
                        mListView.setAdapter(mAdapter);
//                    mAdapter.notifyDataSetChanged();

                        Log.i(TAG, "Invoice Detail:" + mLineItemList);
//                    PaymentModeAdapter dataAdapter = new PaymentModeAdapter(mActivity, android.R.layout.simple_spinner_item, modesList);
//                    mPaymentModeSpnr.setAdapter(dataAdapter);
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(), "Oopss!! Couldn't get the invoice. Kindly check whether the invoice no is correct", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SalesInvoiceDetail> call, Throwable t) {
                    // Log error here since request failed
                    Toast.makeText(getApplicationContext(), "Oopss!! Couldn't get the invoice. Kindly check whether the invoice no is correct", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, t.toString());
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RETURN_PRODUCT){
            if (resultCode == Activity.RESULT_OK) {
                String quantity = data.getStringExtra("Return Quantity");
                String position = data.getStringExtra("Line Position");
                LineItem lineItem = mLineItemList.get(Integer.valueOf(position));
                lineItem.setCurrentQuantityReturned(quantity);
                mAdapter.notifyDataSetChanged();
                RETURN_ACTIVE = true;

                returnDetailsLayout = (RelativeLayout) findViewById(R.id.return_details_layout);
                returnDetailsLayout.setVisibility(View.VISIBLE);

                TextView returnTotalView = (TextView) findViewById(R.id.return_total);
                TextView returnRoundView = (TextView) findViewById(R.id.return_round_off);
                double billTotal = calculateBillTotal();
                double roundedTotal = round(billTotal,0);
                double roundedValue = roundedTotal - billTotal;
                returnTotalView.setText("Total: "+String.format("%.02f", roundedTotal));
                returnRoundView.setText("Round Off: "+String.format("%.02f", roundedValue));
                mAdapter.notifyDataSetChanged();
            }
        }
        else if (requestCode == SCAN_PRODUCT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
//                String barcode = data.getStringExtra("barcode");
                Barcode barcodedata = data.getParcelableExtra("barcode");
                String barcode = barcodedata.displayValue;
                Toast.makeText(getApplicationContext(), "Fetching Product Details: " + barcode, Toast.LENGTH_LONG).show();
                invoice_no = barcode;
//                getProduct(barcode, true);
                getInvoiceDetail();
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.action_bar_create_invoice, menu);
        return true;
    }

    private class Product {
        String line_item_id;
        String product_id;
        double quantity;
        double return_price;
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
        String date;
//        int warehouse;
        String invoiceid;
        Product[] bill_details;
        double subtotal;
        double cgsttotal;
        double sgsttotal;
        double total;
        double roundoff;
        String calltype;
    }


    private void saveInvoice(boolean printInvoice) {
        Log.i(TAG, "Save Invoice");

        if (mLineItemList.size() == 0) {
            Toast.makeText(getApplicationContext(), "Sales Return is empty!!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (printInvoice) {
            if (mTarget == null) {
                Log.i(TAG, "Target is NULL.. return");
                Snackbar.make(findViewById(R.id.fab), "Add a Printer first to print!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
        }

        // Create the data to be submitted in API format
        Product productArr[] = new Product[mLineItemList.size()];
        Log.i(TAG, "productArr size:" + productArr.length);

        // Invoice Calculation
        double billTotal = 0;
        double billCGSTTotal= 0;
        double billSGSTTotal= 0;
        double billSubTotal = 0;
        for (int  i=0; i<mLineItemList.size(); i++) {
            LineItem lineItem = mLineItemList.get(i);
            productArr[i] = new Product();
            productArr[i].line_item_id = String.valueOf(lineItem.getId());
            productArr[i].product_id = Long.toString(lineItem.getProductId());
            productArr[i].quantity = Double.parseDouble(lineItem.getCurrentQuantityReturned());
            productArr[i].return_price = Double.parseDouble(lineItem.getSalesPrice());
            productArr[i].cgst_p = Double.parseDouble(lineItem.getCgstPercent());
            productArr[i].sgst_p = Double.parseDouble(lineItem.getSgstPercent());

            productArr[i].cgst_v = Double.parseDouble(lineItem.getReturnCgstValue());
            productArr[i].sgst_v = Double.parseDouble(lineItem.getReturnSgstValue());
            productArr[i].taxable_total = Double.parseDouble(lineItem.getReturn_line_before_tax());
            productArr[i].line_total = Double.parseDouble(lineItem.getReturn_line_total());
            billSubTotal+= round(Double.parseDouble(lineItem.getReturn_line_before_tax()),2);
            billCGSTTotal+=Double.parseDouble(lineItem.getReturnCgstValue());
            billSGSTTotal+=Double.parseDouble(lineItem.getReturnSgstValue());

//            Log.i(TAG, "Vendor Taxable Value: "+productArr[i].sales_after_tax);

            // TODO Consider discount and multiple sales rate.

        }

        billTotal = round(billCGSTTotal + billSGSTTotal + billSubTotal,2);

        double rounded_total = round(billTotal,0);
        double round_value = rounded_total - billTotal;


        newInvoice.date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Log.i(TAG, "Date:" + newInvoice.date);
        newInvoice.invoiceid = String.valueOf(mInvoiceDetail.getId());
        newInvoice.bill_details = productArr;
        newInvoice.subtotal = round(billSubTotal,2);
        newInvoice.cgsttotal = round(billCGSTTotal,2);
        newInvoice.sgsttotal= round(billSGSTTotal,2);
        newInvoice.total = round(rounded_total,2);
        newInvoice.roundoff = round_value;
        newInvoice.calltype = "mobilesave";

        showProgress(true);

        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            Log.i(TAG, "Start task to save new invoice...");
            mSubmitTask = new ReturnSaveTask(newInvoice, authToken, printInvoice);
            mSubmitTask.execute((Void) null);
        } else {
            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again. Please check if sufficient quantity is available.", Toast.LENGTH_LONG).show();
            // REDIRECT TO LOGIN PAGE
        }
    }

    /**
     * Represents an asynchronous task used to create
     * a new Sales Invoice.
     */
    public class ReturnSaveTask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.ReturnSaveTask";
        private static final String targetURL = Constants.SERVER_ADDR + APIs.retail_sales_return;

        private final InvoiceDetails mInvoice;
        private final String mToken;
        private final boolean mPrintInvoice;

        ReturnSaveTask(InvoiceDetails invoice, String authToken, boolean printInvoice) {
            mInvoice = invoice;
            mToken = authToken;
            mPrintInvoice = printInvoice;
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
                    mSavedInvoiceId = jsonObj.getString("id");
//                    BitInteger x = BigInteger("2.54");
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
                Log.i(TAG, "Successfully Created New Invoice");
                Toast.makeText(getApplicationContext(), "Successfully Saved Sales Return!!", Toast.LENGTH_SHORT).show();

                // Clear the data and set details layout to invisible and search layout to visible.
                searchLayout = (LinearLayout) findViewById(R.id.search_layout);
                detailLayout = (LinearLayout) findViewById(R.id.details_layout);
                returnDetailsLayout = (RelativeLayout) findViewById(R.id.return_details_layout);
                searchLayout.setVisibility(View.VISIBLE);
                detailLayout.setVisibility(View.GONE);
                returnDetailsLayout.setVisibility(View.GONE);
                if (mPrintInvoice) {
                    mLineItemPrint = mLineItemList;
                }
                mLineItemList.clear();
                mAdapter.notifyDataSetChanged();
                RETURN_ACTIVE = false;
                TextView invoiceView = (TextView) findViewById(R.id.invoice_no);
                invoiceView.setText(R.string.view_invoice_no);

                TextView dateView = (TextView) findViewById(R.id.date);
                dateView.setText(R.string.view_invoice_date);

                TextView cgstView = (TextView) findViewById(R.id.cgst);
                cgstView.setText(R.string.view_invoice_cgst);
                TextView sgstView = (TextView) findViewById(R.id.sgst);
                sgstView.setText(R.string.view_invoice_sgst);
                TextView billtotalView = (TextView) findViewById(R.id.billtotal);
                billtotalView.setText(R.string.view_invoice_total);
                TextView billroundView = (TextView) findViewById(R.id.billround);
                billroundView.setText(R.string.view_invoice_round);

                if (mPrintInvoice) {
                    printInvoice();
                }

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

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");

        if (RETURN_ACTIVE) {
//            Log.i(TAG, "Invoice is Empty.. return");
//            finish();
//            return;
//        }

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle("Discard Sales Return")
                    .setMessage("Are you sure you want to discard current sales return ?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            //super.onBackPressed();
        }else{
            Log.i(TAG, "Invoice is Empty.. return");
            finish();
            return;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceType")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            if (RETURN_ACTIVE) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                double billTotal = calculateBillTotal();
                double rounded_total = round(billTotal, 0);
                double round_value = rounded_total - billTotal;

                builder.setTitle("Save Sales Return")
                        .setMessage("Save Sales Return ?" +
                                "\nTotal Return value: " + String.format("%.02f", rounded_total))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                saveInvoice(false);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(R.drawable.ic_action_alert)
                        .create()
                        .show();
            }else{
                Toast.makeText(getApplicationContext(), "Hmm!! Kindly generate a sales return to save data.", Toast.LENGTH_SHORT).show();
            }

            return true;
        }

        else if (id == R.id.action_save_and_print) {
            if (RETURN_ACTIVE) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                double billTotal = calculateBillTotal();
                double rounded_total = round(billTotal,0);
                double round_value = rounded_total - billTotal;

                builder.setTitle("Save Invoice and Print")
                        .setMessage("Save Sales Return ?" +
                                "\nTotal Return value: " + String.format("%.02f", rounded_total))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                saveInvoice(true);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(R.drawable.ic_action_alert)
                        .create()
                        .show();
                }else{
                Toast.makeText(getApplicationContext(), "Hmm!! Kindly generate a sales return to save data.", Toast.LENGTH_SHORT).show();
            }

            return true;
        }
//
        else if (id == R.id.action_add_printer) {
            Log.i(TAG, "Add Printer");

            Intent intent = new Intent();
            intent.setClass(this, PrinterDiscoveryActivity.class);
            startActivityForResult(intent, ADD_PRINTER_REQUEST);
        }

        return super.onOptionsItemSelected(item);
    }

    private double calculateBillTotal() {
        Log.i(TAG, "Remaining list size: " + mLineItemList.size());
        double billReturnTotal = 0;

        for (int i=0; i<mLineItemList.size(); i++) {
            double thisLineReturnTotal = 0;
            double thisQuantity = 0;
            double thisCGSTP = 0;
            double thisSGSTP = 0;
            double thisCGSTV = 0;
            double thisSGSTV = 0;
            double thisTotalSalesPrice = 0;
            thisCGSTP = Double.parseDouble(mLineItemList.get(i).getCgstPercent());
            thisSGSTP = Double.parseDouble(mLineItemList.get(i).getSgstPercent());
            thisQuantity = Double.parseDouble(mLineItemList.get(i).getCurrentQuantityReturned());
            thisTotalSalesPrice = round(Double.parseDouble(mLineItemList.get(i).getSalesPrice()) * thisQuantity,2);
            thisCGSTV = round(thisTotalSalesPrice*thisCGSTP/100,2);
            thisSGSTV = round(thisTotalSalesPrice*thisSGSTP/100,2);
            thisLineReturnTotal = round(thisTotalSalesPrice + thisCGSTV + thisSGSTV,2);
            mLineItemList.get(i).setReturn_line_before_tax(String.valueOf(thisTotalSalesPrice));
            mLineItemList.get(i).setReturn_line_total(String.valueOf(thisLineReturnTotal));
            mLineItemList.get(i).setReturnCgstValue(String.valueOf(thisCGSTV));
            mLineItemList.get(i).setReturnSgstValue(String.valueOf(thisSGSTV));

            Log.i(TAG, "Line CGST: " + thisCGSTV);
            Log.i(TAG, "Line SGST: " + thisSGSTV);
            Log.i(TAG, "Line Total: " +thisLineReturnTotal);
            billReturnTotal += thisLineReturnTotal;
        }
        return (billReturnTotal);
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
            ShowMsg.showException(e, "connect", mContext);
//            Toast.makeText(getApplicationContext(), "Oops!! Could -not connect printer. Invoice is saved.", Toast.LENGTH_LONG).show();
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
            String invoiceDateString = newInvoice.date;
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(invoiceDateString);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String printDate = dateFormat.format(date);

            // PRODUCT DETAILS
            textData.append("---------------------------------------\n");
            textData.append("Sales Return                         Date\n");
//            textData.append("1707090001                     21-07-2017\n");
            textData.append(mSavedInvoiceId+"                     "+printDate+"\n");
            textData.append(tenantInfo.tenant_name+"\n"); //Tenant Name
            textData.append(mInvoiceDetail.getWarehouseAddress()+","+mInvoiceDetail.getWarehouseCity()+ "\n");   //Warehouse Address
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
            textData.append("HSN    Qty    Unit     GST    Total\n");
            textData.append("---------------------------------------\n");
            method = "addText";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.TRUE, Printer.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            for (int i=0; i < mLineItemPrint.size() ; i++) {
                LineItem line_detail = mInvoiceDetail.getLineItems().get(i);

                //Line 1
                textData.append(line_detail.getProductName() + "\n");
                if (TextUtils.isEmpty(line_detail.getProductHsn())) {
                    textData.append("     ");
                }
                else {
                    textData.append(line_detail.getProductHsn() + " ");
                }
                textData.append(line_detail.getQuantity() + "  ");
                textData.append(line_detail.getUnit() + "  ");
                //Original Text
//                textData.append(String.format("%.2f",product.discount_amount )+ "  ");
//                textData.append(String.format("%.2f",product.sales_after_tax) );
//                textData.append(String.format("%.2f",product.discount_amount * product.quantity)+ "  ");
//                Double zero_discount = 0.00;
                textData.append(Double.parseDouble(line_detail.getCgstPercent())*2  + "  ");
                textData.append(String.format("%.2f",Double.parseDouble(line_detail.getReturn_line_total())) );
                textData.append("\n");
            }

            mLineItemPrint.clear();

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
            textData.append(String.format("%.2f",newInvoice.subtotal)+ "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            // CGST TOTAL
            if (newInvoice.cgsttotal > 0) {
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                textData.append("CGST: ");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());

                mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
                textData.append(String.format("%.2f", newInvoice.cgsttotal) + "\n");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
            }

            // SGST TOTAL
            if (newInvoice.sgsttotal > 0) {
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                textData.append("SGST: ");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());

                mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
                textData.append(String.format("%.2f", newInvoice.sgsttotal) + "\n");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
            }

            // ROUND OFF
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            textData.append("Round off: ");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
            textData.append(String.format("%.2f",newInvoice.roundoff)+ "\n");
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
            textData.append(String.format("%.2f",newInvoice.total)+ "\n");
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
            mPrinter.addBarcode(mSavedInvoiceId,
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
