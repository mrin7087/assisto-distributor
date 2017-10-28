package com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceDetails;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceList.InvoiceActivity;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.Constants;

import org.json.JSONException;
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
import java.util.List;

public class RetailInvoiceDetailsActivity extends DashBoardActivity {

    private static final String TAG = "Assisto.InvoiceDetails";
    private Activity mActivity = null;
    private InvoiceDetailsAPITask mInvoiceDetailsAPITask  = null;
    private InvoiceDeleteAPITask mInvoiceDeleteAPITask = null;
    private View mProgressView = null;
    private ListView mListView = null;
    private JSONObject mProductDetails = null;
    private JSONObject mInvoiceDelete = null;
    private  RetailInvoiceDetails retailInvoiceDetails;
    ArrayList<RetailInvoiceLineDetails> mModelList;
    RetailInvoiceAdapter mAdapter = null;
    String invoice_no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_retail_invoice_details);
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

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_retail_invoice_details;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_invoice_delete, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_delete){
//            Toast.makeText(getApplicationContext(), "Clicked", Toast.LENGTH_SHORT).show();
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
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "No", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            TextView invoice = (TextView) findViewById(R.id.invoice_no);
            invoice.setText(invoice_no);
            TextView date = (TextView) findViewById(R.id.date);
            date.setText(retailInvoiceDetails.getDate());
            TextView cgst = (TextView) findViewById(R.id.cgst);
            cgst.setText(retailInvoiceDetails.getCgsttotal());
            TextView sgst = (TextView) findViewById(R.id.sgst);
            sgst.setText(retailInvoiceDetails.getSgsttotal());
            TextView billtotal = (TextView) findViewById(R.id.billtotal);
            billtotal.setText(retailInvoiceDetails.getTotal());


            Log.i(TAG, "In Populate Details Info");
            for (int i=0; i<allItems.size(); i++){
                try{
//                    JSONObject detail = mProductDetails.getJSONObject(i);
                    mModelList.add(new RetailInvoiceLineDetails(allItems.get(i).getProduct_name(), allItems.get(i).getSales_price(), allItems.get(i).getQuantity(), allItems.get(i).getCgst_value(), allItems.get(i).getSgst_value(), allItems.get(i).getLine_total()));
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
                    Log.i(TAG, "Vendor Details Length: "+ mInvoiceDelete.length());
//                    Toast.makeText(getApplicationContext(), "No Invoices found", Toast.LENGTH_SHORT).show();
                    mInvoiceDelete = null;
                    return Constants.Status.ERR_INVALID;
                }
            }catch (Exception e){
                return Constants.Status.ERR_UNKNOWN;
            }

            return Constants.Status.OK;
        }

        private void checkDeleteStatus(){
            try {
                String is_delete = mInvoiceDelete.getString("success");
                if (is_delete.length()>0){
                    Toast.makeText(getApplicationContext(), "Invoice is deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setClass(RetailInvoiceDetailsActivity.this, InvoiceActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

}



