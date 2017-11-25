package com.techassisto.mrinmoy.assisto.retailSales;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.WarehouseInfo;
import com.techassisto.mrinmoy.assisto.retailSales.retailDashboard.RetailDashboardActivity;
import com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceList.InvoiceActivity;
import com.techassisto.mrinmoy.assisto.retailSales.retailNewInvoice.NewSalesInvoice;
import com.techassisto.mrinmoy.assisto.retailSales.retailNewInvoice.WarehouseAdapter;
import com.techassisto.mrinmoy.assisto.retailSales.retailReportDownload.RetailReportDownload;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.Constants;
import com.techassisto.mrinmoy.assisto.utils.TenantInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class RetailSalesLanding extends DashBoardActivity {
    private static final String TAG = "Assisto.RetailLanding";

    private Activity mActivity = null;

    private WarehouseAPITask mWarehouseAPITask = null;
    private View mMainView = null;
    private View mProgressView = null;
    private Spinner mWarehousesSpnr = null;

    private JSONArray mWarehouseList = null;

    private DownloadAPITask mDownloadAPITask = null;

    private int mWareHouseId = -1;
    private String mWarehouseAddress;
    private int mWarehouseState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_retailsales_landing);
        mActivity = this;

        mMainView = findViewById(R.id.create_invoice_layout);
        mProgressView = findViewById(R.id.apiget_progress);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();

        Button createInvoiceBtn = (Button) findViewById(R.id.createInvoiceBtn);
        createInvoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWareHouseId == - 1) {
                    Toast.makeText(getApplicationContext(),
                            "Select a warehouse to create Invoice!!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Warehouse Address in intent: "+mWarehouseAddress);
                    Intent intent = new Intent();
                    intent.putExtra("warehouseId", mWareHouseId);
                    intent.putExtra("warehouseAddress", mWarehouseAddress);
                    intent.putExtra("warehouseState", mWarehouseState);
                    intent.setClass(RetailSalesLanding.this, NewSalesInvoice.class);
                    startActivity(intent);
                }
            }
        });

        // Fetch Tenant Details
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String tenant = userPref.getString(Constants.UserPref.SP_TENANT, null);
        if (tenant != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            TenantInfo tenantInfo = gson.fromJson(tenant, TenantInfo.class);
            Log.i(TAG, "Tenant:" + tenantInfo.tenant_name + " First Name:" + tenantInfo.first_name);
        }
        Button viewInvoiceBtn = (Button) findViewById(R.id.viewInvoiceBtn);
        viewInvoiceBtn .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(RetailSalesLanding.this, InvoiceActivity.class);
                startActivity(intent);
            }
        });

        Button retailDashboardBtn = (Button) findViewById(R.id.viewRetailDashboardBtn);
        retailDashboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(RetailSalesLanding.this, RetailDashboardActivity.class);
                startActivity(intent);
            }
        });

        Button downloadReportsBtn = (Button) findViewById(R.id.downloadReportsBtn);
        downloadReportsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWareHouseId == - 1) {
                    Toast.makeText(getApplicationContext(),
                            "Select a warehouse to generate report!!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Warehouse Address in intent: "+mWarehouseAddress);
                    Intent intent = new Intent();
                    intent.putExtra("warehouseId", mWareHouseId);
                    intent.putExtra("warehouseAddress", mWarehouseAddress);
                    intent.putExtra("warehouseState", mWarehouseState);
                    intent.setClass(RetailSalesLanding.this, RetailReportDownload.class);
                    startActivity(intent);
                }
            }
        });

        getWarehouses();
//        getDownload();
    }

    public int getLayoutResId() {
        return R.layout.activity_retailsales_landing;
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

            mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
            mMainView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void getWarehouses() {
        if (mWarehouseAPITask != null) {
            return;
        }

        showProgress(true);

        Log.i(TAG, "get Warehoueses...");

        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            mWarehouseAPITask = new WarehouseAPITask(authToken);
            mWarehouseAPITask.execute((Void) null);
        } else {
            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
        }
    }

    // AsyncTask to send WAREHOUSE GET REQUEST
    public class WarehouseAPITask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.WarehouseTask";
        private static final String targetURL = Constants.SERVER_ADDR + APIs.warehouse_get;
        private final String mToken;

        WarehouseAPITask(String token) {
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
                // Save the warehouse details
                return parseWarehouseInfo(response.toString());

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
            mWarehouseAPITask = null;
            showProgress(false);

            if (status == Constants.Status.OK) {
                Log.i(TAG, "Successfully received warehouse data");
                populateWarehouseInfo();
            } else if (status == Constants.Status.ERR_INVALID){
                Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mWarehouseAPITask = null;
            showProgress(false);
        }

        private int parseWarehouseInfo(final String warehouses) {
            Log.i(TAG, "parse Warehouse Info");
            try {
                mWarehouseList = new JSONArray(warehouses);
                if (mWarehouseList.length() == 0) {
                    Log.i(TAG, "User has no warehouses registered");
                    mWarehouseList = null;
                    return Constants.Status.ERR_INVALID;
                }

            } catch (Exception e) {
                Log.e(TAG, "Failed to add warehouse data, ex:" + e);
                return Constants.Status.ERR_UNKNOWN;
            }

            return Constants.Status.OK;
        }

        private void populateWarehouseInfo() {
            // Populate the rates spinner
            mWarehousesSpnr = (Spinner) findViewById(R.id.warehouse_spinner);
            mWarehousesSpnr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    WarehouseInfo wh = (WarehouseInfo) parent.getItemAtPosition(position);
//                    Toast.makeText(getApplicationContext(),
//                            wh.name, Toast.LENGTH_SHORT).show();
                    mWareHouseId = wh.id;
                    mWarehouseAddress  = wh.address_1+ " "+wh.address_2+", "+wh.city;
                    mWarehouseState = Integer.parseInt(wh.state);
                    Log.i(TAG, "Warehouse Address in spinner: "+mWarehouseAddress);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Toast.makeText(getApplicationContext(),
                            "Nothing selected!!", Toast.LENGTH_SHORT).show();
                }
            });

            List<WarehouseInfo> list = new ArrayList<>();
            for (int i=0; i<mWarehouseList.length(); i++) {
                try {
                    JSONObject warehouse = mWarehouseList.getJSONObject(i);
                    Log.i(TAG, "Warehouse name:" + warehouse.getString("name"));
                    Log.i(TAG, "Warehouse ID:" + warehouse.getInt("id"));

                    Gson gson = new GsonBuilder().serializeNulls().create();
                    WarehouseInfo wh = gson.fromJson(warehouse.toString(), WarehouseInfo.class);

                    if (wh.name.length() == 0) {
                        if (wh.address_1.length() == 0) {
                            wh.name = "<Unknown>";
                        }
                        else{
                            wh.name = wh.address_1 + " " + wh.address_2;
                        }
                    }

                    //list.add(Integer.toString(warehouse.getInt("id")));
                    list.add(wh);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to populate vendor list, ex:" + e);
                }
            }
            WarehouseAdapter dataAdapter = new WarehouseAdapter(mActivity,
                    android.R.layout.simple_spinner_item, list);
            //dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mWarehousesSpnr.setAdapter(dataAdapter);
        }
    }

    private void getDownload() {
        if (mDownloadAPITask != null) {
            return;
        }

        showProgress(true);

        Log.i(TAG, "get Downloads...");

        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            mDownloadAPITask = new DownloadAPITask(authToken);
            mDownloadAPITask.execute((Void) null);
        } else {
            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
        }
    }

    // AsyncTask to send WAREHOUSE GET REQUEST
    public class DownloadAPITask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.DownloadTask";
        private String targetURL = Constants.SERVER_ADDR + APIs.retail_sales_eod_product;
        private final String mToken;

        DownloadAPITask(String token) {
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

            Log.i(TAG, "try to GET HTTP request");
            HttpURLConnection httpConnection = null;

//            String extStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                    .toString();

            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(folder, "EODReport.csv");
            try {
                file.createNewFile();
            } catch (IOException e1) {
                Log.i(TAG, "Error in creating pdf file: "+e1);
                e1.printStackTrace();
            }

            try{
                targetURL += ("?calltype=download_current");
                URL tagetUrl = new URL(targetURL);
                httpConnection = (HttpURLConnection) tagetUrl.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Authorization", "jwt " + mToken);
                httpConnection.setConnectTimeout(10000); //10secs
                httpConnection.connect();

//                httpConnection = (HttpURLConnection) tagetUrl.openConnection();
//                httpConnection.setRequestMethod("GET");
//                httpConnection.setRequestProperty("Authorization", "jwt " + mToken);
//
//                httpConnection.setConnectTimeout(10000); //10secs
//                httpConnection.connect();

                Log.i(TAG, "response code:" + httpConnection.getResponseCode());
                if (httpConnection.getResponseCode() != 200){
                    Log.e(TAG, "Failed : HTTP error code : " + httpConnection.getResponseCode());
                    return Constants.Status.ERR_INVALID;
                }



                FileOutputStream f = new FileOutputStream(file);
//                URL u = new URL(fileURL);
//                HttpURLConnection c = (HttpURLConnection) u.openConnection();
//                c.setRequestMethod("GET");
//                c.setDoOutput(true);
//                c.connect();

                InputStream in = httpConnection.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = in.read(buffer)) > 0) {
                    f.write(buffer, 0, len1);
                }
                f.close();

                return Constants.Status.OK;

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
            mDownloadAPITask = null;
            showProgress(false);

            if (status == Constants.Status.OK) {
                Log.i(TAG, "Successfully received warehouse data");
//                populateWarehouseInfo();
//                Toast.makeText(getApplicationContext(), "Downloaded", Toast.LENGTH_SHORT).show();
                showPdf();
            } else if (status == Constants.Status.ERR_INVALID){
                Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mDownloadAPITask = null;
            showProgress(false);
        }


    }

    public void showPdf()
    {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(folder, "EODReport.csv");

//        File file = new File(Environment.getExternalStorageDirectory()+"/pdf/Read.pdf");
        PackageManager packageManager = getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("text/csv");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "text/csv");
        startActivity(intent);
    }


//    public static void DownloadFile(String fileURL, File directory) {
//        try {
//            FileOutputStream f = new FileOutputStream(directory);
//            URL u = new URL(fileURL);
//            HttpURLConnection c = (HttpURLConnection) u.openConnection();
//            c.setRequestMethod("GET");
//            c.setDoOutput(true);
//            c.connect();
//
//            InputStream in = c.getInputStream();
//
//            byte[] buffer = new byte[1024];
//            int len1 = 0;
//            while ((len1 = in.read(buffer)) > 0) {
//            f.write(buffer, 0, len1);
//            }
//            f.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

}
