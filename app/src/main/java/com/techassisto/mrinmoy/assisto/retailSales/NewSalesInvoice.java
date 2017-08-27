package com.techassisto.mrinmoy.assisto.retailSales;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.gson.Gson;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.ProductInfo;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.epsonPrinter.PrinterDiscoveryActivity;
import com.techassisto.mrinmoy.assisto.epsonPrinter.ShowMsg;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.Constants;

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

public class NewSalesInvoice extends DashBoardActivity implements ReceiveListener{
    private final static String TAG = "Assisto.NewSalesInvoice";

    private Context mContext = null;
    private Activity mActivity = null;
    private ListView mListView = null;
    ArrayList<InvoiceProductListModel> mModelList;
    InvoiceProductListAdapter mAdapter = null;

    private View mProgressView = null;
    private View mInvoiceView = null;
    private TextView mInvoiceTotalView = null;
    private InvoiceSaveTask mSubmitTask = null;

    private int mWarehouseId;

    private InvoiceDetails mCurrentInvoice = null;
    private int mSavedInvoiceId = -1;

    //Printer related
    private String mTarget = null;
    private Printer  mPrinter = null;

    private static final int ADD_PRODUCT_REQUEST = 1;
    private static final int ADD_PRINTER_REQUEST = 2;

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
            }
        } else {
            mWarehouseId = (int) savedInstanceState.getSerializable("warehouseId");
        }
        Log.i(TAG, "Warehouse ID : " + mWarehouseId);

        mActivity = this;

        mInvoiceView = findViewById(R.id.invoiceView);
        mProgressView = findViewById(R.id.apisubmit_progress);
        mInvoiceTotalView = (TextView) findViewById(R.id.invoicetotalview);

        mListView = (ListView) findViewById(R.id.invoicelistview);
        mModelList = new ArrayList<InvoiceProductListModel>();
        mAdapter = new InvoiceProductListAdapter(mActivity, mModelList);
        mAdapter.setOnItemDeletedListener(new InvoiceProductListAdapter.OnItemDeletedListener() {
            @Override
            public void onItemDeleted() {
                Log.i(TAG, "Received item deleted callback");
                double billTotal = calculateBillTotal();
                mInvoiceTotalView.setText("Total: " + String.format("%.02f", billTotal));
            }
        });
        mListView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(NewSalesInvoice.this, AddProduct.class);
                intent.putExtra("warehouseId", mWarehouseId);
                startActivityForResult(intent, ADD_PRODUCT_REQUEST);
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_new_sales_invoice;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_create_invoice, menu);
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
            builder.setTitle("Save Invoice")
                    .setMessage("Save current invoice ?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            saveInvoice(false);
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

        else if (id == R.id.action_save_and_print) {
            //Toast.makeText(getApplicationContext(), "Save", Toast.LENGTH_SHORT).show();

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle("Save Invoice and Print")
                    .setMessage("Save current invoice and Print?")
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
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            return true;
        }

        else if (id == R.id.action_add_printer) {
            Log.i(TAG, "Add Printer");

            Intent intent = new Intent();
            intent.setClass(this, PrinterDiscoveryActivity.class);
            startActivityForResult(intent, ADD_PRINTER_REQUEST);
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveInvoice(boolean printInvoice) {
        Log.i(TAG, "Save Invoice");

        if (mModelList.size() == 0) {
            Toast.makeText(getApplicationContext(), "Product list is empty!!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (printInvoice) {
            mCurrentInvoice = null;
            // Check if target is selected
            if (mTarget == null) {
                Log.i(TAG, "Target is NULL.. return");
                Snackbar.make(findViewById(R.id.fab), "Add a Printer first to print!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
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
            double cgstTotal;
            double sgstTotal;
            double thisNonTaxTotal; //to store line total without tax
            ProductInfo pInfo = mModelList.get(i).getProduct();
            //boolean isTax = pInfo.rate.get(0).is_tax_included;
            productArr[i] = new Product();
            productArr[i].product_id = Integer.toString(pInfo.product_id);
            productArr[i].product_name = pInfo.product_name;
            productArr[i].product_hsn = pInfo.product_hsn;
            productArr[i].quantity = pInfo.selectedQuantity;
            productArr[i].inventory = pInfo.inventory;
            productArr[i].unit_id = pInfo.unit_id;
            productArr[i].unit = pInfo.unit;
            productArr[i].sales = pInfo.selectedRate;
            productArr[i].is_tax = pInfo.rate.get(0).is_tax_included;
            productArr[i].discount_amount = 0.0;
            productArr[i].cgst_p = pInfo.cgst;
            productArr[i].sgst_p = pInfo.sgst;
            double thisTotal = pInfo.selectedRate * pInfo.selectedQuantity; //to store line total with tax
            if (productArr[i].is_tax) {
                totalTaxPercent= pInfo.cgst + pInfo.sgst;
                totalTaxDivider=(100+totalTaxPercent)/100;
                taxTotal=thisTotal-thisTotal/totalTaxDivider;
                cgstTotal=taxTotal/2;
                sgstTotal=taxTotal/2;
                billTotal+=thisTotal;
                billSubTotal+=thisTotal-taxTotal;
                thisNonTaxTotal = thisTotal-taxTotal;
            }
            else{
                cgstTotal=(thisTotal*pInfo.cgst)/100;
                sgstTotal=(thisTotal*pInfo.sgst)/100;
                taxTotal = cgstTotal + sgstTotal;
                thisNonTaxTotal = thisTotal;
                billTotal+=thisTotal+taxTotal;
                billSubTotal+=thisNonTaxTotal ;
                thisTotal = thisTotal+taxTotal;
            }
            productArr[i].cgst_v = cgstTotal;
            productArr[i].sgst_v = sgstTotal;
            productArr[i].taxable_total = thisNonTaxTotal;
            productArr[i].line_total = thisTotal;
            billCGSTTotal+=cgstTotal;
            billSGSTTotal+=sgstTotal;

            // TODO
            // Consider discount and multiple sales rate.

            //TODO:Update warehouse choosing options

        }
        InvoiceDetails newInvoice = new InvoiceDetails();
        newInvoice.date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Log.i(TAG, "Date:" + newInvoice.date);
        newInvoice.bill_details = productArr;
        newInvoice.subtotal = billSubTotal;
        newInvoice.cgsttotal = billCGSTTotal;
        newInvoice.sgsttotal=billSGSTTotal;
        newInvoice.total = billTotal;
        newInvoice.warehouse = mWarehouseId;
        newInvoice.calltype = "mobilesave";

        showProgress(true);
        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            Log.i(TAG, "Start task to save new invoice...");
            mSubmitTask = new InvoiceSaveTask(newInvoice, authToken, printInvoice);
            mSubmitTask.execute((Void) null);
        } else {
            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
        }
    }

    private double calculateBillTotal() {
        Log.i(TAG, "Remaining list size: " + mModelList.size());
        double billTotal = 0;
        for (int i=0; i<mModelList.size(); i++) {
            Log.i(TAG, mModelList.get(i).getProduct().toString() + " qty: " + mModelList.get(i).getProduct().selectedQuantity);
            billTotal += mModelList.get(i).getPrice() * mModelList.get(i).getQuantity();
        }

        return billTotal;
    }

    private void printInvoice() {
        Log.i(TAG, "printInvoice");
        if (mCurrentInvoice == null) {
            return;
        }

        Log.i(TAG, "mCurrentInvoice not null!!");

        boolean success = runPrintReceiptSequence();

        Log.i(TAG, "runPrintReceiptSequence, res: " + success);

        if (success == false) {
            Toast.makeText(getApplicationContext(), "Data saved, but failed to print", Toast.LENGTH_LONG).show();
        }

        mCurrentInvoice = null;
    }

    private void addProduct(String product) {
        Log.i(TAG, "Product: " + product);
        ProductInfo productInfo = new Gson().fromJson(product, ProductInfo.class);
        Log.i(TAG, "ProductInfo: " + productInfo);

        mModelList.add(new InvoiceProductListModel(productInfo));
        mAdapter.notifyDataSetChanged();

        Log.i(TAG, "List adapter updated");

        // Update Bill Total View
        double billTotal = calculateBillTotal();
        mInvoiceTotalView.setText("Total: " + String.format("%.02f", billTotal));
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

        else if (requestCode == ADD_PRINTER_REQUEST) {
            Log.i(TAG, "onActivityResult: ADD_PRINTER_REQUEST, resultCode: " + resultCode);
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    String target = data.getStringExtra(getString(R.string.title_target));
                    Toast.makeText(getApplicationContext(), target, Toast.LENGTH_SHORT).show();

                    if (target != null) {
                        mTarget = target;
                        Log.i(TAG, "mTarget : " + mTarget);
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");

        if (mModelList.size() == 0) {
            Log.i(TAG, "Product List Empty.. return");
            finish();
            return;
        }

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Discard Invoice")
                .setMessage("Are you sure you want to discard current invoice ?")
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
        boolean inventory;
        int quantity;
        int unit_id;
        double sales;
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
        String customer_phone;
        String customer_name;
        String customer_address;
        String customer_email;
        String date;
        int warehouse;

        Product[] bill_details;
        double subtotal;
        double cgsttotal;
        double sgsttotal;
        double total;
        String calltype;
    }

    /**
     * Represents an asynchronous task used to create
     * a new Sales Invoice.
     */
    public class InvoiceSaveTask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.InvoiceSaveTask";
        private static final String targetURL = Constants.SERVER_ADDR + APIs.retail_invoice_save;

        private final InvoiceDetails mInvoice;
        private final String mToken;
        private final boolean mPrintInvoice;

        InvoiceSaveTask(InvoiceDetails invoice, String authToken, boolean printInvoice) {
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
            Log.i(TAG, "Token: " + mToken);

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

                mCurrentInvoice = mInvoice;

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

            //TODO : INVOICE Header - Tax Invoice No., Date, GSTIN, Tenant Name, Warehouse Address
            // INVOICE ID - mSavedInvoiceId
            // Date - mCurrentInvoice.date

            //TODO : HSN Code

            // PRODUCT DETAILS
            textData.append("---------------------------------------\n");
            textData.append("Tax Invoice                         Date\n");
            textData.append("1707090001                     21-07-2017\n");
            textData.append("           Sample Distributor          \n"); //Tenant Name
            textData.append(" 45B Anath Nath Deblane, Kolkata-700037 \n");   //Warehouse Address
            textData.append("                 West Bengal          \n");  //Warehouse State
            textData.append("         GSTIN:19AWPKJ14741017B78Z     \n");
            textData.append("Item\n");
            textData.append("HSN   Qty   Unit   Dcnt   Rate\n");
            textData.append("CGST%  CGST AMT   SGST%  SGST AMT  Total\n");
            textData.append("---------------------------------------\n");
            method = "addText";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.TRUE, Printer.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            for (int i=0; i < mCurrentInvoice.bill_details.length ; i++) {
                Product product = mCurrentInvoice.bill_details[i];

                //Line 1
                textData.append(product.product_name + "\n");
                textData.append(product.product_hsn+ " ");
                textData.append(product.quantity + "  ");
                textData.append(product.unit + "  ");
                textData.append(String.format("%.2f",product.discount_amount )+ "  ");
                double item_rate=(product.taxable_total)/(product.quantity);
                textData.append(String.format("%.2f",item_rate) );  //This should br the total before tax is added
                textData.append("\n");

                //Line 2
                textData.append("  " );
                textData.append(product.cgst_p  + "  ");
                textData.append(String.format("%.2f",product.cgst_v) + "  ");
                textData.append(product.sgst_p  + "  ");
                textData.append(String.format("%.2f",product.sgst_v) + "  ");
                textData.append(String.format("%.2f",product.line_total) ); //This is the line total
                textData.append("\n");
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
            textData.append(String.format("%.2f",mCurrentInvoice.subtotal)+ "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            // CGST TOTAL
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            textData.append("CGST: ");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
            textData.append(String.format("%.2f",mCurrentInvoice.cgsttotal )+ "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            // SGST TOTAL
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            textData.append("SGST: ");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
            textData.append(String.format("%.2f",mCurrentInvoice.sgsttotal )+ "\n");
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
            textData.append(String.format("%.2f",mCurrentInvoice.total)+ "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            method = "addFeedLine";
            mPrinter.addFeedLine(1);

            textData.append("---------------------------------------\n");

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
