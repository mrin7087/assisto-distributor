package com.techassisto.mrinmoy.assisto.vendor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.Constants;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.R;

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
import java.util.List;

public class VendorLanding extends DashBoardActivity {
    private final static String TAG = "Assisto.VendorList";

    private Activity mActivity = null;
    private VendorAPITask mAuthTask = null;
    private View mProgressView = null;
    private ListView mListView = null;
    private JSONArray mVendorList = null;
    ArrayList<VendorListModel> mModelList;
    VendorListArrayAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_vendor_landing);

        mActivity = this;
        mProgressView = findViewById(R.id.apiget_progress);
        mListView = (ListView) findViewById(R.id.vendorlistview);
        mModelList = new ArrayList<VendorListModel>();
        mAdapter = new VendorListArrayAdapter(mActivity, mModelList);
        mListView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Vendor add action clicked!!", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                Intent intent = new Intent();
                intent.setClass(VendorLanding.this, VendorCreateActivity.class);
                startActivity(intent);
            }
        });

        //Toast.makeText(getApplicationContext(), "Vendor Landing page!!", Toast.LENGTH_SHORT).show();

        getVendors();
    }

    public int getLayoutResId() {
        return R.layout.activity_vendor_landing;
    }

    private void getVendors() {
        if (mAuthTask != null) {
            return;
        }

        showProgress(true);

        Log.i(TAG, "getVendors...");

        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            mAuthTask = new VendorAPITask(authToken);
            mAuthTask.execute((Void) null);
        } else {
            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
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

//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });

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
            //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    // AsyncTask to send requests
    public class VendorAPITask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.VendorAPITask";
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
            mAuthTask = null;
            showProgress(false);

            if (status == Constants.Status.OK) {
                Log.i(TAG, "Successfully received vendor data");
                populateVendorInfo();
            } else if (status == Constants.Status.ERR_INVALID){
                Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
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
            mModelList.clear();
            Log.i(TAG, "populate vendor info");
            for (int i = 0; i < mVendorList.length(); i++) {
                try {
                    JSONObject vendor = mVendorList.getJSONObject(i);
                    Log.i(TAG, "Vendor name:" + vendor.getString("name"));
                    Log.i(TAG, "Vendor key:" + vendor.getString("key"));

                    mModelList.add(new VendorListModel(vendor.getString("name"), vendor.getString("key")));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to populate vendor list, ex:" + e);
                }
            }
            Log.i(TAG, "Model created");

            mAdapter.notifyDataSetChanged();
            Log.i(TAG, "List adapter updated");
        }
    }
}
