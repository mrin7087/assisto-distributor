package com.techassisto.mrinmoy.assisto.serviceSales.serviceNewInvoice;

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
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.utilDeclaration.PaymentModeInfo;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.utilDeclaration.ServiceInfo;
import com.techassisto.mrinmoy.assisto.epsonPrinter.PrinterDiscoveryActivity;
import com.techassisto.mrinmoy.assisto.epsonPrinter.ShowMsg;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.Constants;
import com.techassisto.mrinmoy.assisto.utils.TenantInfo;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.techassisto.mrinmoy.assisto.utilDeclaration.RoundClass.round;

/**
 * Created by sayantan on 3/12/17.
 */

public class NewSalesInvoice extends DashBoardActivity implements ReceiveListener {

    private final static String TAG = "Assisto.NewSalesInvoice";

    private Context mContext = null;
    private Activity mActivity = null;
    private ListView mListView = null;
    ArrayList<InvoiceServiceListModel> mModelList;
    InvoiceServiceListAdapter mAdapter = null;

    private View mProgressView = null;
    private View mInvoiceView = null;
    private TextView mInvoiceTotalView = null;
    private InvoiceSaveTask mSubmitTask = null;

    private int mWarehouseId;
    private String mWarehouseAddress;
    private int mWarehouseState;
    private String mWarehouseStateName;

    private InvoiceDetails mCurrentInvoice = null;
    private String mSavedInvoiceId = null;

    //Printer related
    private String mTarget = null;
    private Printer mPrinter = null;

    private static final int ADD_PRODUCT_REQUEST = 1;
    private static final int ADD_PRINTER_REQUEST = 2;

    //Payment Mode related
    private PaymentModeAPITask mPaymentModeAPITask = null;
    private Spinner mPaymentModeSpnr = null;
    private JSONArray mPaymentModeList = null;
    private int mPaymentModeId = -1;
    private String mPaymentModeName = null;

    //Salespersons related
    private JSONArray mSalespersonsList = null;
    private SalespersonsAPITask mSalespersonsAPITask = null;

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
                Log.i(TAG, "Warehouse Address in intent new: "+mWarehouseAddress);
            }
        } else {
            mWarehouseId = (int) savedInstanceState.getSerializable("warehouseId");
            mWarehouseAddress = (String) savedInstanceState.getSerializable("warehouseAddress");
            mWarehouseState= (int) savedInstanceState.getSerializable("warehouseState");
            mWarehouseStateName = Constants.STATE_LIST[mWarehouseState];
            Log.i(TAG, "Warehouse Address in intent saved: "+mWarehouseAddress);
        }
        Log.i(TAG, "Warehouse ID : " + mWarehouseId);

        mActivity = this;

        mInvoiceView = findViewById(R.id.invoiceView);
        mProgressView = findViewById(R.id.apisubmit_progress);
        mInvoiceTotalView = (TextView) findViewById(R.id.invoicetotalview);

        mListView = (ListView) findViewById(R.id.invoicelistview);
        mModelList = new ArrayList<InvoiceServiceListModel>();
        mAdapter = new InvoiceServiceListAdapter(mActivity, mModelList);
        mAdapter.setOnItemDeletedListener(new InvoiceServiceListAdapter.OnItemDeletedListener() {
            @Override
            public void onItemDeleted() {
                Log.i(TAG, "Received item deleted callback");
                double billTotal = calculateBillTotal();
                double rounded_total = round(billTotal,0);
                double round_value = rounded_total - billTotal;
                mInvoiceTotalView.setText("Total: " + String.format("%.02f", rounded_total));
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
                intent.setClass(NewSalesInvoice.this, AddService.class);
                intent.putExtra("warehouseId", mWarehouseId);
                if (mSalespersonsList == null){
                    Log.i(TAG, "Json array null detected");
                    String need_list = "[]";
                    intent.putExtra("salespersons", need_list);
                }
                else{
                    intent.putExtra("salespersons", mSalespersonsList.toString());
                }
                Log.i(TAG, "Json array: "+mSalespersonsList);
//                intent.putExtra("salespersons", mSalespersonsList.toString());
                startActivityForResult(intent, ADD_PRODUCT_REQUEST);
            }
        });

        getPaymentMode();
        getSalespersons();
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
            double billTotal = calculateBillTotal();
            double rounded_total = round(billTotal,0);
            double round_value = rounded_total - billTotal;
            builder.setTitle("Save Invoice")
                    .setMessage("Save current invoice ?" +
                            "\nPayment Mode: "+mPaymentModeName+ "\nTotal: "+String.format("%.02f", rounded_total))
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
            double billTotal = calculateBillTotal();
            double rounded_total = round(billTotal,0);
            double round_value = rounded_total - billTotal;
            builder.setTitle("Save Invoice and Print")
                    .setMessage("Save current invoice and Print?"+
                            "\nPayment Mode: "+mPaymentModeName+ "\nTotal: "+String.format("%.02f", rounded_total))
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
            Toast.makeText(getApplicationContext(), "Invoice is empty!!", Toast.LENGTH_SHORT).show();
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
            Log.i(TAG, mModelList.get(i).getService().toString() + " qty: " + mModelList.get(i).getService().selectedQuantity);
        }

        // Create the data to be submitted in API format
        Service serviceArr[] = new Service[mModelList.size()];
        Log.i(TAG, "serviceArr size:" + serviceArr.length);

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
            double thisNonTaxEach; //to store item total without tax
            ServiceInfo sInfo = mModelList.get(i).getService();
//            JsonObject salespersons = new JsonObject();
//            JsonArray salesperson_array = new JsonArray();
//            JsonObject salesperson = new JsonObject();
//            salesperson.addProperty("id", 3);
//            salesperson.addProperty("contrib", "0.5");
//            salesperson_array.add(salesperson);
//
////            salesperson = new JsonObject(); //Since the same key is used, reinitializing the object is not needed
//
//            salesperson.addProperty("id", 4);
//            salesperson.addProperty("contrib", "0.3");
//            salesperson_array.add(salesperson);
//
//            salesperson.addProperty("id", 5);
//            salesperson.addProperty("contrib", "0.2");
//            salesperson_array.add(salesperson);

            //boolean isTax = sInfo.rate.get(0).is_tax_included;
            serviceArr[i] = new Service();
            serviceArr[i].service_id = Integer.toString(sInfo.service_id);
            serviceArr[i].service_name = sInfo.service_name;
            serviceArr[i].service_hsn = sInfo.service_hsn;
            serviceArr[i].quantity = sInfo.selectedQuantity;
//            serviceArr[i].inventory = sInfo.inventory;
            serviceArr[i].unit_id = sInfo.unit_id;
            serviceArr[i].unit = sInfo.unit;
            serviceArr[i].sales = sInfo.selectedRate;
            serviceArr[i].is_tax = sInfo.selectedIsTaxIncluded;
            serviceArr[i].discount_amount = 0.0;
            serviceArr[i].cgst_p = sInfo.cgst;
            serviceArr[i].sgst_p = sInfo.sgst;
            serviceArr[i].salespersons = sInfo.salespersons;
            double thisTotal = sInfo.selectedRate * sInfo.selectedQuantity; //to store line total with tax
            if (serviceArr[i].is_tax) {

                totalTaxPercent= sInfo.cgst + sInfo.sgst;
                totalTaxDivider=(100+totalTaxPercent)/100;
                taxTotal=round(thisTotal-thisTotal/totalTaxDivider,2);
                cgstTotal=round(taxTotal/2,2);
                sgstTotal=round(taxTotal/2,2);
//                billTotal+=round(thisTotal,2);
//                billSubTotal+=thisTotal-taxTotal;
                thisNonTaxTotal = round(thisTotal-taxTotal, 2);
            }
            else{
                cgstTotal=round((thisTotal*sInfo.cgst)/100,2);
                sgstTotal=round((thisTotal*sInfo.sgst)/100,2);
                taxTotal = cgstTotal + sgstTotal;
                thisNonTaxTotal = thisTotal;
//                billTotal+=thisTotal+taxTotal;
//                billSubTotal+=thisNonTaxTotal ;
                thisTotal = thisTotal+taxTotal;
            }
            serviceArr[i].cgst_v = cgstTotal;
            serviceArr[i].sgst_v = sgstTotal;
            serviceArr[i].taxable_total = thisNonTaxTotal;
            thisNonTaxEach = round((thisNonTaxTotal/sInfo.selectedQuantity),2);
            serviceArr[i].sales_before_tax = thisNonTaxEach;
            serviceArr[i].line_total = thisTotal;
            billSubTotal+= round(serviceArr[i].sales_before_tax * serviceArr[i].quantity,2);
            billCGSTTotal+=cgstTotal;
            billSGSTTotal+=sgstTotal;

//            Log.i(TAG, "Vendor Taxable Value: "+serviceArr[i].sales_before_tax);

            // TODO Consider discount and multiple sales rate.

        }

        billTotal = round(billCGSTTotal + billSGSTTotal + billSubTotal,2);

        double rounded_total = round(billTotal,0);
        double round_value = rounded_total - billTotal;

        InvoiceDetails newInvoice = new InvoiceDetails();
        newInvoice.date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Log.i(TAG, "Date:" + newInvoice.date);
        newInvoice.bill_details = serviceArr;
        newInvoice.subtotal = billSubTotal;
        newInvoice.cgsttotal = billCGSTTotal;
        newInvoice.sgsttotal=billSGSTTotal;
        newInvoice.total = rounded_total;
        newInvoice.roundoff = round_value;
        newInvoice.warehouse = mWarehouseId;
        newInvoice.paymentmode = mPaymentModeId;
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
//            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again. Please check if sufficient quantity is available.", Toast.LENGTH_LONG).show();

            // REDIRECT TO LOGIN PAGE
        }
    }

    private class Service {
        String service_id;
        String service_name;
        String service_hsn;
        String unit;
//        boolean inventory;
        int quantity;
        int unit_id;
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
        JsonArray salespersons;
    }

    private class InvoiceDetails {
        String customer_phone;
        String customer_name;
        String customer_address;
        String customer_email;
        String date;
        int warehouse;
        int paymentmode;

        Service[] bill_details;
        double subtotal;
        double cgsttotal;
        double sgsttotal;
        double total;
        double roundoff;
        String calltype;
    }

    private double calculateBillTotal() {
        Log.i(TAG, "Remaining list size: " + mModelList.size());
        double billCGSTTotal = 0;
        double billSGSTTotal = 0;
        double billSubTotal = 0;
        double billTotal = 0;
        for (int i=0; i<mModelList.size(); i++) {
            double cgst_p = 0;
            double sgst_p = 0;
            double lineTaxableTotal = 0;
            double line_tax = 0;
            double line_qty = 0;
            double cgst_v = 0;
            double sgst_v = 0;
            double totalTaxPercent = 0;
            double totalTaxDivider = 0;
            double preTaxEachRate = 0;

            Log.i(TAG, mModelList.get(i).getService().toString() + " qty: " + mModelList.get(i).getService().selectedQuantity);
            boolean is_tax_included = mModelList.get(i).getService().selectedIsTaxIncluded;
            Log.i(TAG, "Is tax included: " + mModelList.get(i).getService().selectedIsTaxIncluded);
            line_qty = mModelList.get(i).getQuantity();
            if (is_tax_included) {
                double thisTotal = mModelList.get(i).getService().selectedRate * line_qty;
                totalTaxPercent= mModelList.get(i).getService().cgst + mModelList.get(i).getService().sgst;
                totalTaxDivider=(100+totalTaxPercent)/100;
                line_tax=round(thisTotal-thisTotal/totalTaxDivider,2);
                cgst_v=round(line_tax/2,2);
                sgst_v=round(line_tax/2,2);
                lineTaxableTotal = round(thisTotal-line_tax, 2);
                preTaxEachRate = round((lineTaxableTotal/line_qty),2);
//                billTotal += mModelList.get(i).getPrice() * mModelList.get(i).getQuantity();
            }
            else{
                cgst_p = mModelList.get(i).getService().cgst;
                sgst_p = mModelList.get(i).getService().sgst;
                preTaxEachRate = mModelList.get(i).getPrice();
                cgst_v = round((preTaxEachRate * line_qty)*cgst_p/100,2);
                sgst_v = round((preTaxEachRate * line_qty)*sgst_p/100,2);
                billTotal += preTaxEachRate * line_qty + cgst_v + sgst_v;
            }
            billCGSTTotal+= cgst_v;
            billSGSTTotal+= sgst_v;
            billSubTotal+= round(preTaxEachRate*line_qty,2);

        }
        billTotal = round(billCGSTTotal + billSGSTTotal + billSubTotal,2);
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

    private void addService(String service) {
        Log.i(TAG, "Service: " + service);
        ServiceInfo serviceInfo = new Gson().fromJson(service, ServiceInfo.class);
        Log.i(TAG, "ServiceInfo: " + serviceInfo);
        Log.i(TAG, "ServiceInfo Is tax: " + serviceInfo.selectedIsTaxIncluded);

        mModelList.add(new InvoiceServiceListModel(serviceInfo));
        mAdapter.notifyDataSetChanged();

        Log.i(TAG, "List adapter updated");

        // Update Bill Total View
        double billTotal = calculateBillTotal();
        double rounded_total = round(billTotal,0);
        double round_value = rounded_total - billTotal;
        if (round_value == 0) {
            mInvoiceTotalView.setText("Total: " + String.format("%.02f", rounded_total));
        }
        else{
            mInvoiceTotalView.setText("Round-off: " + String.format("%.02f", round_value) + " ,Total: " + String.format("%.02f", rounded_total)  );
        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");

        if (mModelList.size() == 0) {
            Log.i(TAG, "Invoice is Empty.. return");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_PRODUCT_REQUEST) {
            Log.i(TAG, "onActivityResult: ADD_PRODUCT_REQUEST, resultCode: " + resultCode);
            if (resultCode == Activity.RESULT_OK) {
                String service = data.getStringExtra("service");
                if (service != null) {
                    addService(service);
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

    public class InvoiceSaveTask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.ReturnSaveTask";
        private static final String targetURL = Constants.SERVER_ADDR + APIs.service_invoice_save;

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

                mPaymentModeSpnr.setSelection(0);

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
            ShowMsg.showException(e, "activity_connect_barcode_printer", mContext);
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
            TenantInfo tenantInfo = null;
            SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
            String tenant = userPref.getString(Constants.UserPref.SP_TENANT, null);
            if (tenant != null) {
                Gson gson = new GsonBuilder().serializeNulls().create();
                tenantInfo = gson.fromJson(tenant, TenantInfo.class);
                Log.i(TAG, "Tenant:" + tenantInfo.tenant_name + " First Name:" + tenantInfo.first_name);
            }


            // SERVICE DETAILS
            textData.append("---------------------------------------\n");
            textData.append("Tax Invoice                         Date\n");
//            textData.append("1707090001                     21-07-2017\n");
            textData.append(mSavedInvoiceId+"                     "+mCurrentInvoice.date+"\n");
            textData.append(tenantInfo.tenant_name+"\n"); //Tenant Name
            textData.append(mWarehouseAddress+ "\n");   //Warehouse Address
            textData.append(mWarehouseStateName+"\n");  //Warehouse State
//            textData.append("         GSTIN:19AWPKJ14741017B78Z     \n");
            if (TextUtils.isEmpty(tenantInfo.tenant_gst)){

            }
            else {
                textData.append("         GSTIN:" + tenantInfo.tenant_gst + "\n");
            }
            textData.append("Item\n");
            textData.append("HSN   Qty   Unit   Dcnt   Rate\n");
            textData.append("CGST%  CGST AMT   SGST%  SGST AMT  Total\n");
            textData.append("---------------------------------------\n");
            method = "addText";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.TRUE, Printer.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            for (int i=0; i < mCurrentInvoice.bill_details.length ; i++) {
                Service service = mCurrentInvoice.bill_details[i];

                //Line 1
                textData.append(service.service_name + "\n");
                if (TextUtils.isEmpty(service.service_hsn)) {
                    textData.append("     ");
                }
                else {
                    textData.append(service.service_hsn + " ");
                }
                textData.append(service.quantity + "  ");
                textData.append(service.unit + "  ");
                textData.append(String.format("%.2f",service.discount_amount )+ "  ");
//                double item_rate=(product.taxable_total)/(product.quantity);
                textData.append(String.format("%.2f",service.sales_before_tax) );  //This should br the total before tax is added
                textData.append("\n");

                //Line 2
                textData.append("  " );
                textData.append(service.cgst_p  + "  ");
                textData.append(String.format("%.2f",service.cgst_v) + "  ");
                textData.append(service.sgst_p  + "  ");
                textData.append(String.format("%.2f",service.sgst_v) + "  ");
                textData.append(String.format("%.2f",service.line_total) ); //This is the line total
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
            if (mCurrentInvoice.cgsttotal > 0) {
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                textData.append("CGST: ");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());

                mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
                textData.append(String.format("%.2f", mCurrentInvoice.cgsttotal) + "\n");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
            }

            // SGST TOTAL
            if (mCurrentInvoice.sgsttotal > 0) {
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                textData.append("SGST: ");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());

                mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
                textData.append(String.format("%.2f", mCurrentInvoice.sgsttotal) + "\n");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
            }

            // ROUND OFF
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            textData.append("Round off: ");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
            textData.append(String.format("%.2f",mCurrentInvoice.roundoff )+ "\n");
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

            //This is used to print barcode of invoice id.
            int barcodeWidth=2;
            int barcodeHeight=100;
            method = "addFeedLine";
            mPrinter.addFeedLine(2);
            method = "addBarcode";
//            mPrinter.addBarcode(String.valueOf(mSavedInvoiceId),
//                    Printer.BARCODE_CODE128,
//                    Printer.HRI_BELOW,
//                    Printer.FONT_A,
//                    barcodeWidth,
//                    barcodeHeight);

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

    private void getPaymentMode() {
        if (mPaymentModeAPITask != null) {
            return;
        }

//        showProgress(true);

        Log.i(TAG, "get Payment Modes...");

        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            mPaymentModeAPITask = new PaymentModeAPITask(authToken);
            mPaymentModeAPITask.execute((Void) null);
        } else {
//            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
        }
    }

    public class PaymentModeAPITask extends AsyncTask<Void, Void, Integer>  {

        private static final String TAG = "Assisto.GetPaymentMode";
        private static final String targetURL = Constants.SERVER_ADDR + APIs.retail_payment_mode_get;
        private final String mToken;

        PaymentModeAPITask(String token) {
            this.mToken = token;
        }

        @Override
        protected Integer doInBackground(Void... params) {
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
                // Save the warehouse details
                return parsePaymentModeInfo(response.toString());

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
            mPaymentModeAPITask = null;
//            showProgress(false);

            if (status == Constants.Status.OK) {
                Log.i(TAG, "Successfully received warehouse data");
                populatePaymentModeInfo();
            } else if (status == Constants.Status.ERR_INVALID){
                Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mPaymentModeAPITask = null;
//            showProgress(false);
        }

        private int parsePaymentModeInfo(final String paymentModes){
            Log.i(TAG, "parse Payment Mode Info");
            try {
                mPaymentModeList = new JSONArray(paymentModes);
                if (mPaymentModeList.length() == 0) {
                    Log.i(TAG, "User has no payment mode registered");
                    mPaymentModeList = null;
                    return Constants.Status.ERR_INVALID;
                }

            } catch (Exception e) {
                Log.e(TAG, "Failed to add payment mode data, ex:" + e);
                return Constants.Status.ERR_UNKNOWN;
            }

            return Constants.Status.OK;
        }

        private void  populatePaymentModeInfo(){

            // Populate the rates spinner
            mPaymentModeSpnr = (Spinner) findViewById(R.id.paymentMode_spinner);
            mPaymentModeSpnr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    PaymentModeInfo pminfo = (PaymentModeInfo) parent.getItemAtPosition(position);
//                    Toast.makeText(getApplicationContext(),
//                            wh.name, Toast.LENGTH_SHORT).show()
                    mPaymentModeId = pminfo.id;
                    mPaymentModeName = pminfo.name;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Toast.makeText(getApplicationContext(),
                            "Nothing selected!!", Toast.LENGTH_SHORT).show();
                }
            });

            List<PaymentModeInfo> list = new ArrayList<>();
            for (int i=0; i<mPaymentModeList.length(); i++) {
                try {
                    JSONObject paymentModes = mPaymentModeList.getJSONObject(i);
                    Log.i(TAG, "Mode name:" + paymentModes.getString("name"));
                    Log.i(TAG, "Mode ID:" + paymentModes.getInt("id"));

                    Gson gson = new GsonBuilder().serializeNulls().create();
                    PaymentModeInfo pm = gson.fromJson(paymentModes.toString(), PaymentModeInfo.class);

                    //list.add(Integer.toString(warehouse.getInt("id")));
                    list.add(pm);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to populate payment mode list, ex:" + e);
                }
            }
            for (PaymentModeInfo pminfo: list){
                Log.i(TAG, "Payment Mode Array: "+pminfo.name);
            }
            PaymentModeAdapter dataAdapter = new PaymentModeAdapter(mActivity,android.R.layout.simple_spinner_item, list);
//            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            mPaymentModeSpnr.setAdapter(dataAdapter);

        }
    }


    private void getSalespersons() {
        if (mSalespersonsAPITask != null) {
            return;
        }

//        showProgress(true);

        Log.i(TAG, "get Salespersons...");

        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            mSalespersonsAPITask = new SalespersonsAPITask(authToken);
            mSalespersonsAPITask.execute((Void) null);
        } else {
//            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Could not fetch salespersons. Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
        }
    }

    public class SalespersonsAPITask extends AsyncTask<Void, Void, Integer>  {

        private static final String TAG = "Assisto.GetPaymentMode";
        private static final String targetURL = Constants.SERVER_ADDR + APIs.service_salespersons_get;
        private final String mToken;

        SalespersonsAPITask(String token) {
            this.mToken = token;
        }

        @Override
        protected Integer doInBackground(Void... params) {
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
                // Save the warehouse details
                return parseSalespersonsInfo(response.toString());

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
            mSalespersonsAPITask = null;
//            showProgress(false);

            if (status == Constants.Status.OK) {
                Log.i(TAG, "Successfully received salesperson data");
            } else if (status == Constants.Status.ERR_INVALID){
                Toast.makeText(getApplicationContext(), "Oops!! Could not fetch salesperson data. Something went wrong. Try Again", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mSalespersonsAPITask = null;
//            showProgress(false);
        }

        private int parseSalespersonsInfo(final String salespersons){
            Log.i(TAG, "parse Salespersons Info");
            try {
                mSalespersonsList = new JSONArray(salespersons);
                if (mSalespersonsList.length() == 0) {
                    Log.i(TAG, "User has no salesperson registered");
                    mSalespersonsList = null;
                    return Constants.Status.ERR_INVALID;
                }

            } catch (Exception e) {
                Log.e(TAG, "Failed to load salespersons data, ex:" + e);
                return Constants.Status.ERR_UNKNOWN;
            }

            return Constants.Status.OK;
        }

    }


}
