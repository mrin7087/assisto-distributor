package com.techassisto.mrinmoy.assisto.retailSales.retailDashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.R;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RetailDashboardActivity extends DashBoardActivity {

    LineGraphSeries<DataPoint> series;
    private static final String TAG = "Assisto.RetailDashboard";
    private RetailDetailsAPITask mRetailDetailsAPITask = null;
    private View mProgressView = null;
    private JSONArray mSalesDetails = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "In dashboard activity");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();
//        setContentView(R.layout.activity_retail_dashboard);
        getSalesSummary();

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_retail_dashboard;
    }

    public void getSalesSummary(){
        Log.i(TAG, "In Sales Summary Function");
        if (mRetailDetailsAPITask != null){
            return;
        }
//        showProgress(true);

        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            mRetailDetailsAPITask = new RetailDetailsAPITask (authToken);
            mRetailDetailsAPITask.execute((Void) null);
        }
        else{
//            showProgress(false);
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

    public class RetailDetailsAPITask extends AsyncTask<Void, Void, Integer>{
        private String targetURL = Constants.SERVER_ADDR + APIs.retail_sales_summary_graph;
        private final String mToken;

        RetailDetailsAPITask(String token){mToken = token;
            Log.i(TAG, "Details API Called");}

        @Override
        protected Integer doInBackground(Void... params) {
            Log.i(TAG, "Target URL: "+targetURL);
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
                targetURL+= "?days=";;
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
            mRetailDetailsAPITask = null;
//            showProgress(false);
            if (status == Constants.Status.OK){
                populateDetailsInfo();
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
            mRetailDetailsAPITask = null;
//            showProgress(false);
        }

        private int parseDetailsInfo (String details){
            Log.i(TAG, "In parsedetailinfo");
            try{
                mSalesDetails = new JSONArray(details);
                if (mSalesDetails.length() == 0){
                    Toast.makeText(getApplicationContext(), "No Retail Sales Details found.", Toast.LENGTH_SHORT).show();
                    mSalesDetails = null;
                    return Constants.Status.ERR_INVALID;
                }
            }catch (Exception e){
                return Constants.Status.ERR_UNKNOWN;
            }

            return Constants.Status.OK;
        }

        private void populateDetailsInfo(){
//            mModelList.clear();
            GraphView graph =(GraphView) findViewById(R.id.salesGraph);
            graph.removeAllSeries();
            series = new LineGraphSeries<DataPoint>();
            Date minDate = new Date();
            Date maxDate = new Date();
            for (int i = 0; i<mSalesDetails.length(); i++){
                try{
                    SimpleDateFormat originalDateFormat = new SimpleDateFormat( "yyyy-MM-dd");
                    Calendar cal = Calendar.getInstance();
//                    SimpleDateFormat revisedDateFormat = new SimpleDateFormat( "dd-MM-yyyy");
                    Date original_date;
                    JSONObject summary = mSalesDetails.getJSONObject(i);
                    double total = Double.valueOf(summary.getString("total"));
                    original_date = originalDateFormat.parse(summary.getString("date"));
                    if (i == 0){
                        minDate = original_date;
                    }
                    else if(i == mSalesDetails.length()-1){
                        maxDate = original_date;
                    }
                    cal.setTime(original_date);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
//                    Date revised_date = revisedDateFormat.format(original_date);
                    series.appendData(new DataPoint(day, total), true, 500);


                }catch (Exception e){

                }
            }
            series.setThickness(8);
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(15f);
            int colorPrimaryDark = Color.parseColor("#0381ad");
;
            series.setColor(colorPrimaryDark);
//            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
//            graph.getGridLabelRenderer().setNumHorizontalLabels(3);
//            graph.getViewport().setMinX(minDate.getTime());
//            graph.getViewport().setMaxX(maxDate.getTime());
//            graph.getViewport().setXAxisBoundsManual(true);
//            graph.getGridLabelRenderer().setHumanRounding(false);
            graph.getGridLabelRenderer().setNumHorizontalLabels(3);
            graph.getGridLabelRenderer().setNumVerticalLabels(4);
//            graph.getViewport().setScalableY(true);
//            graph.getViewport().setScrollable(true);
            graph.addSeries(series);

            series.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Toast.makeText(getApplicationContext(), "Total Sales On: "+(int)dataPoint.getX()+" is: Rs."+dataPoint.getY(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}
