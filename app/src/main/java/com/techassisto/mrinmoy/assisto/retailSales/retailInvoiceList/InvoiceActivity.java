package com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.techassisto.mrinmoy.assisto.BarcodeFiles.ScanActivity;
import com.techassisto.mrinmoy.assisto.CodeScannerActivity;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceDetails.RetailInvoiceDetailsActivity;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

public class InvoiceActivity extends DashBoardActivity {

    private static final String TAG = "Assisto.InvoiceList";
    private Activity mActivity = null;
    private RetailInvoiceAPITask mRetailInvoiceAPITask = null;
    private View mProgressView = null;
    private ListView mListView = null;
    private JSONArray mInvoiceList = null;
    ArrayList <LatestSalesInvoices> mModelList;
    RetailInvoiceListArrayAdapter mAdapter = null;
    ArrayList <String> mInvoiceNo = new ArrayList<String>();
    private static final int SCAN_PRODUCT_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_invoice);
        mActivity = this;
        mProgressView = findViewById(R.id.apiget_progress);
        mListView = (ListView) findViewById(R.id.retailinvoicelistview);
        mModelList = new ArrayList<LatestSalesInvoices>();
        mAdapter = new RetailInvoiceListArrayAdapter(mActivity, mModelList);
        mListView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();

        getInvoices();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String invoice_no = mInvoiceNo.get(position);
//                Log.i(TAG, "INvoice No: "+ invoice_no);
//                Toast.makeText(getApplicationContext(), "Invoice No: "+mInvoiceNo.get(position), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(InvoiceActivity.this, RetailInvoiceDetailsActivity.class);
                intent.putExtra("invoice_no", mInvoiceNo.get(position));
                Log.i(TAG, "About to start activity ");
                startActivity(intent);
            }
        });

        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.search_invoice);
                String invoice_no = editText.getText().toString();
                Intent intent = new Intent(InvoiceActivity.this, RetailInvoiceDetailsActivity.class);
                intent.putExtra("invoice_no", invoice_no);
                startActivity(intent);
            }
        });

//        Button scanBtn = (Button) findViewById(R.id.scan_button);
//        scanBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(InvoiceActivity.this, CodeScannerActivity.class);
//                startActivityForResult(intent, SCAN_PRODUCT_REQUEST);
//            }
//        });

        Button scanBtn = (Button) findViewById(R.id.scan_button);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(AddProduct.this, CodeScannerActivity.class);
//                startActivityForResult(intent, SCAN_PRODUCT_REQUEST);
                Intent intent = new Intent(InvoiceActivity.this, ScanActivity.class);
                startActivityForResult(intent, SCAN_PRODUCT_REQUEST);
            }
        });


    }

    public int getLayoutResId() {
        return R.layout.activity_invoice;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_PRODUCT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
//                String invoice_no = data.getStringExtra("barcode");
//                Toast.makeText(getApplicationContext(), "Fetching Invoice Details: " + invoice_no, Toast.LENGTH_SHORT).show();
                Barcode barcodedata = data.getParcelableExtra("barcode");
                String invoice_no = barcodedata.displayValue;
                Toast.makeText(getApplicationContext(), "Fetching Product Details for Invoice: " + invoice_no, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(InvoiceActivity.this, RetailInvoiceDetailsActivity.class);
                intent.putExtra("invoice_no", invoice_no);
                startActivity(intent);
            }
        }
    }

    public void getInvoices(){
        if (mRetailInvoiceAPITask != null){
            return;
        }

        showProgress(true);

        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            mRetailInvoiceAPITask = new RetailInvoiceAPITask (authToken);
            mRetailInvoiceAPITask.execute((Void) null);
        }
        else{
            showProgress(false);
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

    // AsyncTask to send requests

    public class RetailInvoiceAPITask extends AsyncTask<Void, Void, Integer>{

        private static final String targetURL = Constants.SERVER_ADDR + APIs.retail_invoice_list +"?calltype=all_invoices" ;
//        private static final String targetURL = Constants.SERVER_ADDR + APIs.vendor_get;
        private final String mToken;

        RetailInvoiceAPITask(String token){mToken = token;}


        @Override
        protected Integer doInBackground(Void... params) {

            String authHeader = "{\"authorization\": \"jwt " + mToken + "\"}";
            JSONObject authJson;

            try {
                authJson = new JSONObject(authHeader);
            } catch (Exception e) {
//                Log.e(TAG, "Failed to make json ex:" + e);
                return Constants.Status.ERR_UNKNOWN;
            }

            StringBuffer response = new StringBuffer();
            HttpURLConnection httpConnection = null;

            try{
                URL targetUrl = new URL(targetURL);
                httpConnection = (HttpURLConnection) targetUrl.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Authorization", "jwt "+mToken);
                httpConnection.setConnectTimeout(10000);
                httpConnection.connect();

                if (httpConnection.getResponseCode() != 200){
                    return Constants.Status.ERR_INVALID;
                }

                InputStream is= httpConnection.getInputStream();
                BufferedReader rd= new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line =rd.readLine())!= null){
                    response.append(line);
                }

                rd.close();

                return parseInvoiceInfo(response.toString());
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
//            super.onPostExecute(status);
            mRetailInvoiceAPITask = null;
            showProgress(false);
            if (status == Constants.Status.OK){
                populateInvoiceInfo();
            }
            else if (status == Constants.Status.ERR_UNKNOWN){
                Toast.makeText(getApplicationContext(), "Oops!! No Sales Invoice Exist. Try Again.", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(), "Oops!! Something went wrong.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mRetailInvoiceAPITask = null;
            showProgress(false);
        }

        private int parseInvoiceInfo (String invoices){
            try{
                mInvoiceList = new JSONArray(invoices);
                if (mInvoiceList.length() == 0){
                    Toast.makeText(getApplicationContext(), "No Retail Invoices registered", Toast.LENGTH_SHORT).show();
                    mInvoiceList = null;
                    return Constants.Status.ERR_INVALID;
                }
            }catch (Exception e){
                return Constants.Status.ERR_UNKNOWN;
            }

            return Constants.Status.OK;
        }

        private void populateInvoiceInfo(){
            mModelList.clear();
            for (int i = 0; i<mInvoiceList.length(); i++){
                try{
                    JSONObject invoice = mInvoiceList.getJSONObject(i);
                    mModelList.add(new LatestSalesInvoices(invoice.getString("id"), invoice.getString("invoice_id"), invoice.getString("date"), invoice.getString("cgsttotal"), invoice.getString("sgsttotal"), invoice.getString("total")));
                    mInvoiceNo.add(invoice.getString("invoice_id"));
                }catch (Exception e){

                }
            }

            mAdapter.notifyDataSetChanged();
        }
    }

}
