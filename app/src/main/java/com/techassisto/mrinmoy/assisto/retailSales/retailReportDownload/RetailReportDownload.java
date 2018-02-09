package com.techassisto.mrinmoy.assisto.retailSales.retailReportDownload;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.Constants;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by sayantan on 20/11/17.
 */

public class RetailReportDownload extends DashBoardActivity {
    private static final String TAG = "Assisto.RetailDownload";

    private Activity mActivity = null;

//    private DownloadEODAPITask mDownloadEODAPITask = null;

    private View mMainView = null;
    private View mProgressView = null;
    private View mCurrentView = null;
    EditText eodDownloadDate;
    DatePickerDialog datePickerDialog;

    private EODDownloadAPITask mEODDownloadAPITask = null;

    private int mWarehouseId = -1;
    private String mWarehouseAddress;
    private int mWarehouseState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_retailsales_landing);
        mActivity = this;

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                mWarehouseId = -1;
            } else {
                mWarehouseId = extras.getInt("warehouseId");
//                mWarehouseState = extras.getInt("warehouseState");
//                mWarehouseAddress = extras.getString("warehouseAddress");
//                mWarehouseAddress = (String) savedInstanceState.getSerializable("warehouseAddress");
//                Log.i(TAG, "Warehouse Address in intent new: "+mWarehouseAddress);
            }
        } else {
            mWarehouseId = (int) savedInstanceState.getSerializable("warehouseId");
            mWarehouseAddress = (String) savedInstanceState.getSerializable("warehouseAddress");
            mWarehouseState= (int) savedInstanceState.getSerializable("warehouseState");
//            Log.i(TAG, "Warehouse Address in intent saved: "+mWarehouseAddress);
        }

        mMainView = findViewById(R.id.button_list_layout);
        mProgressView = findViewById(R.id.apiget_progress);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();

        eodDownloadDate = (EditText) findViewById(R.id.eodDownloadDate);
        eodDownloadDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(RetailReportDownload.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                eodDownloadDate.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        Button dateEODReportBtn = (Button) findViewById(R.id.dateEODReportBtn);

        dateEODReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainView.setVisibility(View.GONE);
                findViewById(R.id.eod_download).setVisibility(View.VISIBLE);
            }
        });

        Button downloadEODReportBtn = (Button) findViewById(R.id.downloadEODReportBtn);

        downloadEODReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    EditText invoiceDateView = (EditText) findViewById(R.id.eodDownloadDate);
                    String invoiceDateString = invoiceDateView.getText().toString();
                    Date date_DateFormat = new SimpleDateFormat("dd/MM/yyyy").parse(invoiceDateString);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String date = dateFormat.format(date_DateFormat);
                    getDownload(date);
                }catch (Exception e){
                    Log.i(TAG, "Date exception: "+e);
                    Toast.makeText(getApplicationContext(), "Oops!! Please enter a valid date", Toast.LENGTH_SHORT).show();
                }
            }
        });




    }


    public int getLayoutResId() {
        return R.layout.activity_retail_report_download;
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

    private void getDownload(String date) {
        if (mEODDownloadAPITask != null) {
            return;
        }

        showProgress(true);

        Log.i(TAG, "get Downloads...");

        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            mEODDownloadAPITask = new EODDownloadAPITask(authToken,date );
            mEODDownloadAPITask.execute((Void) null);
        } else {
            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
        }
    }

    // AsyncTask to send WAREHOUSE GET REQUEST
    public class EODDownloadAPITask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.DownloadTask";
        private String targetURL = Constants.SERVER_ADDR + APIs.retail_sales_eod_product;
        private final String mToken;
        private final String mDate;

        ProgressDialog dialog = new ProgressDialog(RetailReportDownload.this);



        EODDownloadAPITask(String token, String date) {
            mToken = token;
            mDate = date;
        }

        @Override
        protected void onPreExecute() {
            //set message of the dialog
            dialog.setMessage("Loading...");
            dialog.setIndeterminate(true);
            //show dialog
            dialog.show();
//            super.onPreExecute();
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
                targetURL += ("?calltype=eod_download_csv");
                targetURL += ("&warehouse_id=" + Integer.toString(mWarehouseId));
                targetURL += ("&date=" + mDate);
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



                FileOutputStream f = new FileOutputStream(file);

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
            mEODDownloadAPITask = null;
            showProgress(false);

            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }

            if (status == Constants.Status.OK) {
                Log.i(TAG, "Successfully received warehouse data");

                mMainView.setVisibility(View.VISIBLE);
                findViewById(R.id.eod_download).setVisibility(View.INVISIBLE);
                showPdf();
            } else if (status == Constants.Status.ERR_INVALID){
                Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mEODDownloadAPITask = null;
            showProgress(false);
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }

        }


    }

    public void showPdf()
    {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(folder, "EODReport.csv");
        MediaScannerConnection.scanFile(RetailReportDownload.this, new String[] { file.toString() }, null, null);

//        File file = new File(Environment.getExternalStorageDirectory()+"/pdf/Read.pdf");
        PackageManager packageManager = getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("text/csv");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
//        Uri uri = FileProvider.getUriForFile(RetailReportDownload.this, BuildConfig.APPLICATION_ID + ".provider",fileImagePath);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, "text/csv");
        startActivity(intent);
    }
}
