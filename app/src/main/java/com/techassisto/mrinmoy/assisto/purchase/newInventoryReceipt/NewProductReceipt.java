package com.techassisto.mrinmoy.assisto.purchase.newInventoryReceipt;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.PurchaseProductInfo;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.Constants;

import org.json.JSONArray;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.techassisto.mrinmoy.assisto.RoundClass.round;

/**
 * Created by sayantan on 25/10/17.
 */

public class NewProductReceipt extends DashBoardActivity {
    private final static String TAG = "Assisto.PurProdRec";

    private Context mContext = null;
    private Activity mActivity = null;
    private ListView mListView = null;
    ArrayList<ReceiptProductListModel> mModelList;
    ReceiptProductListAdapter mAdapter = null;

    private View mProgressView = null;
    private View mInvoiceView = null;
    private TextView mInvoiceTotalView = null;
    private EditText mInvoiceRoundView = null;
    private ReceiptSaveTask mSubmitTask = null;

    private int mWarehouseId;
    private String mWarehouseAddress;
    private int mWarehouseState;
    private String mWarehouseStateName;

    private VendorAPITask mVendorAPITask = null;
    private JSONArray mVendorList = null;
    Map <String, String> mapVendor = new HashMap<>();
    ArrayList<String> mVendorAutCompleteList = new ArrayList<>();

    EditText date;
    DatePickerDialog datePickerDialog;

    private int mSavedInvoiceId = -1;

    //Printer related
//    private String mTarget = null;
//    private Printer mPrinter = null;
//
    private static final int ADD_PRODUCT_REQUEST = 1;
    private static final int ADD_VENDOR_REQUEST = 2;
//    private static final int ADD_PRINTER_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_new_sales_invoice);
        Log.i(TAG, "oncreate");

        mContext = this;

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                mWarehouseId = -1;
            } else {
                mWarehouseId = extras.getInt("warehouseId");
                mWarehouseState = extras.getInt("warehouseState");
                mWarehouseStateName = Constants.STATE_LIST[mWarehouseState];
                mWarehouseAddress = extras.getString("warehouseAddress");
//                mWarehouseAddress = (String) savedInstanceState.getSerializable("warehouseAddress");
                Log.i(TAG, "Ware Add in intent new: "+mWarehouseAddress);
            }
        } else {
            mWarehouseId = (int) savedInstanceState.getSerializable("warehouseId");
            mWarehouseAddress = (String) savedInstanceState.getSerializable("warehouseAddress");
            mWarehouseState= (int) savedInstanceState.getSerializable("warehouseState");
            mWarehouseStateName = Constants.STATE_LIST[mWarehouseState];
            Log.i(TAG, "Ware Add in intent saved: "+mWarehouseAddress);
        }
        Log.i(TAG, "Warehouse ID : " + mWarehouseId);

        mActivity = this;

        mInvoiceView = findViewById(R.id.invoiceView);
        mProgressView = findViewById(R.id.apisubmit_progress);
        mInvoiceTotalView = (TextView) findViewById(R.id.invoicetotalview);
        mInvoiceRoundView = (EditText) findViewById(R.id.invoiceroundview);

        mListView = (ListView) findViewById(R.id.invoicelistview);
        mModelList = new ArrayList<ReceiptProductListModel>();
        mAdapter = new ReceiptProductListAdapter(mActivity, mModelList);
        mAdapter.setOnItemDeletedListener(new ReceiptProductListAdapter.OnItemDeletedListener() {
            @Override
            public void onItemDeleted() {
                Log.i(TAG, "Received item deleted callback");
                double[] billTotals = calculateBillTotal("Not Round");
                double rounded_total = round(billTotals[0],0);
                double round_value = rounded_total - billTotals[0];
                mInvoiceTotalView.setText("Gross: " + String.format("%.02f", billTotals[0])+ ", Taxable: " + String.format("%.02f", billTotals[1]));
                mInvoiceRoundView.setText(String.format("%.02f", round_value));
            }
        });
        mListView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setAlpha((float) 0.7);
        CoordinatorLayout.LayoutParams params= (CoordinatorLayout.LayoutParams)
                fab.getLayoutParams();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(NewProductReceipt.this, AddPurchaseProduct.class);
                intent.putExtra("warehouseId", mWarehouseId);
                startActivityForResult(intent, ADD_PRODUCT_REQUEST);
            }
        });

        date = (EditText) findViewById(R.id.date);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(NewProductReceipt.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                date.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        // Used for vendor name autocomplete

        AutoCompleteTextView autocompleteVendor = (AutoCompleteTextView)findViewById(R.id.vendor_name);
        final ArrayAdapter<String> adapter = new ArrayAdapter<> (this,R.layout.select_dialog_lightbackground_smalltext, mVendorAutCompleteList);
        autocompleteVendor.setThreshold(2);
        autocompleteVendor.setAdapter(adapter);

        autocompleteVendor.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String vendorName = adapter.getItem(position).toString();
                String vendorId = mapVendor.get(vendorName);
                Toast.makeText(NewProductReceipt.this, vendorName, Toast.LENGTH_SHORT).show();
                TextView vendorIDView = (TextView) findViewById(R.id.vendor_id);
                vendorIDView.setText(vendorId);
            }
        });
        //Taking care of manual updation of round_off value

        mInvoiceRoundView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){

                    double[] billTotals = calculateBillTotal("Round");
                    double round_value = 0;
                    try {
                        round_value = Double.parseDouble(mInvoiceRoundView.getText().toString());
                    }catch (NumberFormatException e) {
                        round_value = 0;
                    }

                    mInvoiceTotalView.setText("Gross: " + String.format("%.02f", billTotals[0])+ ", Taxable: " + String.format("%.02f", billTotals[1]));
                    mInvoiceRoundView.setText(String.format("%.02f", round_value));

                }
            }
        });

        getVendors();

        // To display line item details

//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                ReceiptProductListModel item = (ReceiptProductListModel) parent.getAdapter().getItem(position);
//                Toast.makeText(getApplicationContext(), "Itm PP: ", Toast.LENGTH_SHORT).show();
//            }
//        });

    }

    private void getVendors() {
        if (mVendorAPITask != null) {
            return;
        }

        showProgress(true);

        Log.i(TAG, "getVendors...");

        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            mVendorAPITask = new VendorAPITask(authToken);
            mVendorAPITask.execute((Void) null);
        } else {
            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
        }
    }

    public class VendorAPITask extends AsyncTask<Void, Void, Integer> {
        private static final String targetURL = Constants.SERVER_ADDR + APIs.vendor_get;
        private final String mToken;

        VendorAPITask(String token) {
            mToken = token;
        }

        @Override
        protected Integer doInBackground(Void... uInfo) {
            Log.i(TAG, "doInBackground");

            //Compose the get Request
            String authHeader = "{\"authorization\": \"jwt " + mToken + "\"}";
            JSONObject authJson;
            try {
                authJson = new JSONObject(authHeader);
            } catch (Exception e) {
                Log.e(TAG, "Failed to make json ex:" + e);
                return Constants.Status.ERR_UNKNOWN;
            }

            StringBuffer response = new StringBuffer();
            Log.i(TAG, "auth header:" + authJson);

            Log.i(TAG, "try to POST HTTP request");
            HttpURLConnection httpConnection = null;
            try{
                URL tagetUrl = new URL(targetURL);
                httpConnection = (HttpURLConnection) tagetUrl.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Authorization", "jwt " + mToken);
                httpConnection.setConnectTimeout(10000); //10secs
                httpConnection.connect();

                Log.i(TAG, "response code:" + httpConnection.getResponseCode());
                if (httpConnection.getResponseCode() != 200){
                    Log.e(TAG, "Failed : HTTP error code : " + httpConnection.getResponseCode());
                    return Constants.Status.ERR_INVALID;
                }

                //Received Response
                InputStream is = httpConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));

                String line;
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    //response.append('\r');
                }
                rd.close();

                Log.i(TAG, response.toString());
                // Save the vendor details
                return parseVendorInfo(response.toString());

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
            mVendorAPITask = null;
            showProgress(false);

            if (status == Constants.Status.OK) {
                Log.i(TAG, "Successfully received vendor data");
                populateVendorInfo();
            } else if (status == Constants.Status.ERR_INVALID){
                Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Vendor details not fetched. Try again.", Toast.LENGTH_LONG).show();
            } else {
//                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Vendor details not fetched. Try again.", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mVendorAPITask = null;
            showProgress(false);
        }

        private int parseVendorInfo(final String vendors) {
            Log.i(TAG, "parse Vendor Info");
            try {
                mVendorList = new JSONArray(vendors);
                if (mVendorList.length() == 0) {
                    Log.i(TAG, "User has no vendors registered");
                    mVendorList = null;
                    return Constants.Status.ERR_INVALID;
                }

            } catch (Exception e) {
                Log.e(TAG, "Failed to add vendor data, ex:" + e);
                return Constants.Status.ERR_UNKNOWN;
            }

            return Constants.Status.OK;
        }

        private void populateVendorInfo() {
            Log.i(TAG, "populate vendor info");
            for (int i = 0; i < mVendorList.length(); i++) {
                try {
                    JSONObject vendor = mVendorList.getJSONObject(i);
                    Log.i(TAG, "Vendor name:" + vendor.getString("name"));
                    Log.i(TAG, "Vendor key:" + vendor.getString("key"));
                    Log.i(TAG, "Vendor ID:" + vendor.getString("id"));

                    mapVendor.put(vendor.getString("name"), vendor.getString("id"));
                    mVendorAutCompleteList.add(vendor.getString("name"));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to populate vendor list, ex:" + e);
                }
            }
            Log.i(TAG, "Map created");

            Log.i(TAG, "Autocomplete List updated");
        }
    }


    @Override
    public int getLayoutResId() {
        return R.layout.activity_new_product_purchase;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.   action_bar_save_receipt, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            //Toast.makeText(getApplicationContext(), "Save", Toast.LENGTH_SHORT).show();

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            double[] billTotals = calculateBillTotal("Not Round");
            double round_value;
            try {
                round_value = Double.parseDouble(mInvoiceRoundView.getText().toString());
            }catch (Exception e){
                round_value = 0;
            }
            double rounded_total = billTotals[0] + round_value;

            builder.setTitle("Save Invoice")
                    .setMessage("Save current invoice ?" +
                            "\nGross Value: "+String.format("%.02f", rounded_total))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            try {
                                saveReceipt();
                            } catch (ParseException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Oops... There were some error in saving!!", Toast.LENGTH_SHORT).show();
                            }
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

                return super.onOptionsItemSelected(item);
    }

    private void saveReceipt() throws ParseException {
        Log.i(TAG, "Save Invoice");

        if (mModelList.size() == 0) {
            Toast.makeText(getApplicationContext(), "Product list is empty!!", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i=0; i<mModelList.size(); i++) {
            Log.i(TAG, mModelList.get(i).getProduct().toString() + " qty: " + mModelList.get(i).getProduct().selectedQuantity);
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
            double totalTaxPercent;
            double totalTaxDivider;
            double taxTotal; //to find out total tax
            double purchase_disc_rate;
            double cgstTotal;
            double sgstTotal;
            double thisNonTaxTotal; //to store line total without tax
            double thisNonTaxEach; //to store item total without tax
            PurchaseProductInfo pInfo = mModelList.get(i).getProduct();
            //boolean isTax = pInfo.rate.get(0).is_tax_included;
            productArr[i] = new Product();
            productArr[i].product_id = Integer.toString(pInfo.product_id);
            productArr[i].product_name = pInfo.product_name;
            productArr[i].product_hsn = pInfo.product_hsn;
            productArr[i].quantity = pInfo.selectedQuantity;
            productArr[i].unit_id = pInfo.unit_id;
            productArr[i].unit = pInfo.unit;
            productArr[i].disc_type = pInfo.disc_type;
            productArr[i].disc = pInfo.disc;
            productArr[i].disc_type_2 = pInfo.disc_type_2;
            productArr[i].disc_2 = pInfo.disc_2;
            productArr[i].purchase = pInfo.purchase_rate;
            productArr[i].tsp= pInfo.tsp;
            productArr[i].mrp = pInfo.mrp;
            // TODO: Update product details part
//            productArr[i].is_tax = pInfo.rate.get(0).is_tax_included;
//            productArr[i].discount_amount = 0.0;
            productArr[i].cgst_p = pInfo.cgst;
            productArr[i].sgst_p = pInfo.sgst;
            double thisTotal = pInfo.purchase_rate * pInfo.selectedQuantity; //to store line total with tax

            purchase_disc_rate = thisTotal;
            if (pInfo.disc_type == 1){
                purchase_disc_rate = purchase_disc_rate-(pInfo.disc*thisTotal/100);
                thisTotal=(thisTotal)-(pInfo.disc*thisTotal/100);
            }
            else if(pInfo.disc_type == 2){
                purchase_disc_rate = purchase_disc_rate - pInfo.disc;
                thisTotal=(thisTotal - pInfo.disc);
            }
            if (pInfo.disc_type_2 == 1){
                purchase_disc_rate = purchase_disc_rate-(pInfo.disc_2*thisTotal/100);
                thisTotal=(thisTotal)-(pInfo.disc_2*thisTotal/100);
            }
            else if(pInfo.disc_type_2 == 2){
                purchase_disc_rate = purchase_disc_rate - pInfo.disc_2;
                thisTotal = (thisTotal - pInfo.disc_2);
            }

            cgstTotal=(purchase_disc_rate*pInfo.cgst)/100;
            sgstTotal=(purchase_disc_rate*pInfo.sgst)/100;
            taxTotal = cgstTotal + sgstTotal;

            productArr[i].taxable_total = round(thisTotal,2);

//            billTotal+=thisTotal+taxTotal;
//            billSubTotal+=purchase_disc_rate ;
            thisTotal = thisTotal+taxTotal;

            productArr[i].cgst_v = round(cgstTotal,2);
            productArr[i].sgst_v = round(sgstTotal,2);
//            productArr[i].taxable_total = thisNonTaxTotal;
//            thisNonTaxEach = round((thisNonTaxTotal/pInfo.selectedQuantity),2);
//            productArr[i].sales_after_tax = thisNonTaxEach;
            productArr[i].line_total = round(thisTotal,2);
            billCGSTTotal+=cgstTotal;
            billSGSTTotal+=sgstTotal;
//            Log.i(TAG, "Vendor Taxable Value: "+productArr[i].sales_after_tax);
        }

        InvoiceDetails newInvoice = new InvoiceDetails();
        Boolean proceed = true;
        try {
            EditText invoiceDateView = (EditText) findViewById(R.id.date);
            String invoiceDateString = invoiceDateView.getText().toString();
            Date date = new SimpleDateFormat("dd/MM/yyyy").parse(invoiceDateString);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            newInvoice.date = dateFormat.format(date);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Oops!! Please enter a valid date", Toast.LENGTH_SHORT).show();
            proceed = false;
        }

        EditText supplierInvoiceView = (EditText) findViewById(R.id.vendorInvoiceNo);
        newInvoice.supplier_invoice = supplierInvoiceView.getText().toString();
        if (newInvoice.supplier_invoice.length() < 1){
            Toast.makeText(getApplicationContext(), "Oops!! Please enter a vendor invoice no.", Toast.LENGTH_SHORT).show();
            proceed = false;
        }

        TextView vendorNameView = (TextView) findViewById(R.id.vendor_name);
        String vendorName = vendorNameView.getText().toString();
        TextView vendorIDView = (TextView) findViewById(R.id.vendor_id);
        newInvoice.vendor = vendorIDView.getText().toString();
        if (vendorName.length() < 1){
            Toast.makeText(getApplicationContext(), "Oops!! Please select a vendor", Toast.LENGTH_SHORT).show();
            proceed = false;
        }

        double[] billTotals = calculateBillTotal("Round");

        Log.i(TAG, "Date:" + newInvoice.date);
        newInvoice.bill_details = productArr;
        newInvoice.subtotal = billTotals[1];
        newInvoice.cgsttotal = round(billCGSTTotal,2);
        newInvoice.sgsttotal = round(billSGSTTotal,2);
        newInvoice.igsttotal = 0;
//        newInvoice.round_value;
        newInvoice.warehouse = mWarehouseId;
        double round_value;
        try {
            round_value = Double.parseDouble(mInvoiceRoundView.getText().toString());
        }catch (Exception e){
            round_value = 0;
        }
        newInvoice.round_value = round_value;

        newInvoice.total = billTotals[0];

        newInvoice.calltype = "mobilesave";

        if (proceed) {
            showProgress(true);
            //Get the auth token
            SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
            String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
            if (authToken != null) {
                Log.i(TAG, "Start task to save new invoice...");
                mSubmitTask = new ReceiptSaveTask(newInvoice, authToken);
                mSubmitTask.execute((Void) null);
            } else {
                showProgress(false);
                Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private double[] calculateBillTotal(String sender) {
        Log.i(TAG, "Remaining list size: " + mModelList.size());
        double billGrossTotal = 0;
        double billTaxableTotal = 0;
        double round_value = 0;
        if (sender  == "Round") {
            try {
                round_value = Double.parseDouble(mInvoiceRoundView.getText().toString());
            }catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Round value is not an integer."+
                            " Please set ot to zero if it needs to be ignored", Toast.LENGTH_LONG).show();
                round_value = 0;
            }
        }
        for (int i=0; i<mModelList.size(); i++) {
            double thisLineTotalWOTax = 0;
            double thisLineTotal = 0;
            double thisQuantity = 0;
            double thisCGSTP = 0;
            double thisSGSTP = 0;
            double thisCGSTV = 0;
            double thisSGSTV = 0;
            thisCGSTP = mModelList.get(i).getProduct().cgst;
            thisSGSTP = mModelList.get(i).getProduct().sgst;
            Log.i(TAG, mModelList.get(i).getProduct().toString() + " qty: " + mModelList.get(i).getProduct().selectedQuantity);
            thisQuantity = mModelList.get(i).getQuantity();
            thisLineTotalWOTax = round(mModelList.get(i).getPurchasePrice() * thisQuantity,2);
            if (mModelList.get(i).getProduct().disc_type == 1){
                thisLineTotalWOTax = (thisLineTotalWOTax)-(mModelList.get(i).getProduct().disc*thisLineTotalWOTax/100);
            }
            else if(mModelList.get(i).getProduct().disc_type == 2){
                thisLineTotalWOTax = (thisLineTotalWOTax- mModelList.get(i).getProduct().disc);
            }
            if (mModelList.get(i).getProduct().disc_type_2 == 1){
                thisLineTotalWOTax = (thisLineTotalWOTax)-(mModelList.get(i).getProduct().disc_2*thisLineTotalWOTax/100);
            }
            else if(mModelList.get(i).getProduct().disc_type_2 == 2){
                thisLineTotalWOTax = (thisLineTotalWOTax - mModelList.get(i).getProduct().disc_2);
            }
            thisLineTotalWOTax = round(thisLineTotalWOTax,2);
            thisCGSTV = round(thisLineTotalWOTax*thisCGSTP/100,2);
            thisSGSTV = round(thisLineTotalWOTax*thisSGSTP/100,2);
            Log.i(TAG, "Line CGST: " + thisCGSTV);
            Log.i(TAG, "Line SGST: " + thisSGSTV);
            thisLineTotal = round(thisLineTotalWOTax + (thisCGSTV + thisSGSTV),2);
            Log.i(TAG, "Line Taxable: " +thisLineTotalWOTax);
            Log.i(TAG, "Line Total: " +thisLineTotal);
            billGrossTotal += round(thisLineTotal,2);
            billTaxableTotal += thisLineTotalWOTax;
        }
        billGrossTotal = billGrossTotal + round_value;

        return new double[]{billGrossTotal, billTaxableTotal};
    }

    private void addProduct(String product) {
        Log.i(TAG, "Product: " + product);
        PurchaseProductInfo productInfo = new Gson().fromJson(product, PurchaseProductInfo.class);
        Log.i(TAG, "ProductInfo: " + productInfo);

        mModelList.add(new ReceiptProductListModel(productInfo));
        mAdapter.notifyDataSetChanged();

        Log.i(TAG, "List adapter updated");

        // Update Bill Total View
        double[] billTotals = calculateBillTotal("Not Round");
        double rounded_total = round(billTotals[0],0);
        double round_value = rounded_total - billTotals[0];
        Log.i(TAG, "Rounded Total: " + rounded_total);
        Log.i(TAG, "Original Total: " + billTotals[0]);
        Log.i(TAG, "Round Value: " + round_value);
        mInvoiceTotalView.setText("Gross: " + String.format("%.02f", rounded_total)+ ", Taxable: " + String.format("%.02f", billTotals[1]));
        mInvoiceRoundView.setText(String.format("%.02f", round_value));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_PRODUCT_REQUEST) {
            Log.i(TAG, "onActivityResult: ADD_PRODUCT_REQUEST, resultCode: " + resultCode);
            if (resultCode == Activity.RESULT_OK) {
                String product = data.getStringExtra("product");
                if (product != null) {
                    addProduct(product);
                }
            }
        }


    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mInvoiceView.setVisibility(show ? View.GONE : View.VISIBLE);
            mInvoiceView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mInvoiceView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mInvoiceView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private class Product {
        String product_id;
        String product_name;
        String product_hsn;
        String unit;
        int quantity;
        int unit_id;
        double purchase;
        double tsp;
        double mrp;
        int disc_type;
        double disc;
        int disc_type_2;
        double disc_2;
        double cgst_p;
        double cgst_v;
        double sgst_p;
        double sgst_v;
        double taxable_total;
        double line_total;
    }

    private class InvoiceDetails {
        String date;
        int warehouse;
        String vendor;
        String supplier_invoice;
        Product[] bill_details;
        double subtotal;
        double cgsttotal;
        double sgsttotal;
        double igsttotal;
        double round_value;
        double total;
        String calltype;
    }


    /**
     * Represents an asynchronous task used to create
     * a new Sales Invoice.
     */
    public class ReceiptSaveTask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.ReceiptSaveTask";
        //TODO: Change target url
        private static final String targetURL = Constants.SERVER_ADDR + APIs.purchase_receipt_save;

        private final InvoiceDetails mInvoice;
        private final String mToken;
//        private final boolean mPrintInvoice;

        ReceiptSaveTask(InvoiceDetails invoice, String authToken) {
            mInvoice = invoice;
            mToken = authToken;
//            mPrintInvoice = printInvoice;
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
                Log.i(TAG, "Successfully Created New Invoice");
                Toast.makeText(getApplicationContext(), "Successfully Saved New Invoice!!", Toast.LENGTH_SHORT).show();

                // Clear the data
                mModelList.clear();
                mAdapter.notifyDataSetChanged();
                mInvoiceTotalView.setText("Total: 0.00");
                EditText supplierInvoiceView = (EditText) findViewById(R.id.vendorInvoiceNo);
                supplierInvoiceView.setText("");
//                AutoCompleteTextView autocompleteVendor = (AutoCompleteTextView)findViewById(R.id.vendor_name);
//                autocompleteVendor.setText("");

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
}


