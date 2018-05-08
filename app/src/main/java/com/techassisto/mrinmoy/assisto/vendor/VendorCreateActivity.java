package com.techassisto.mrinmoy.assisto.vendor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class VendorCreateActivity extends DashBoardActivity {
    private static final String TAG = "Assisto.VendorCreate";

    private View mProgressView = null;
    private View mSubmitFormView = null;

    private VendorSubmitTask mSubmitTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_vendor_create);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);

        mProgressView = findViewById(R.id.apisubmit_progress);
        mSubmitFormView = findViewById(R.id.vendor_create_form);

        Button submitBtn = (Button) findViewById(R.id.submit_button);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitVendorData();
            }
        });
    }

    /**
     * Attempt to Submit and create New Vendor
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void submitVendorData() {
        if (mSubmitTask != null) {
            return;
        }

        EditText vendorNameView = (EditText) findViewById(R.id.vendor_name);
        EditText vendorIDView = (EditText) findViewById(R.id.vendor_id);

        // Reset errors.
        vendorNameView.setError(null);
        vendorIDView.setError(null);

        // Store values at the time of the login attempt.
        String vendorName = vendorNameView.getText().toString();
        String vendorID = vendorIDView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid Vendor ID
        if (TextUtils.isEmpty(vendorID)) {
            vendorIDView.setError(getString(R.string.error_field_required));
            focusView = vendorIDView;
            cancel = true;
        }

        // Check for a valid Vendor Name
        if (TextUtils.isEmpty(vendorName)) {
            vendorNameView.setError(getString(R.string.error_field_required));
            focusView = vendorNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            VendorInfo vendorInfo = new VendorInfo();
            vendorInfo.name = vendorName;
            vendorInfo.key = vendorID;
            vendorInfo.calltype = "newvendor";

            showProgress(true);
            //Get the auth token
            SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
            String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
            if (authToken != null) {
                Log.i(TAG, "Start task to create new vendor...");
                mSubmitTask = new VendorSubmitTask(vendorInfo, authToken);
                mSubmitTask.execute((Void) null);
            } else {
                showProgress(false);
                Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

                // REDIRECT TO LOGIN PAGE
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

            mSubmitFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mSubmitFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSubmitFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mSubmitFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public int getLayoutResId() {
        return R.layout.activity_vendor_create;
    }

    /**
     * Represents an asynchronous task used to create
     * a new vendor.
     */
    public class VendorSubmitTask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.VSubmitTask";
        private static final String targetURL = Constants.SERVER_ADDR + APIs.vendor_new_post;

        private final VendorInfo mVendorInfo;
        private final String mToken;

        VendorSubmitTask(VendorInfo vendorInfo, String authToken) {
            mVendorInfo = vendorInfo;
            mToken = authToken;
        }

        @Override
        protected Integer doInBackground(Void... uInfo) {
            Log.i(TAG, "doInBackground");
            StringBuffer response = new StringBuffer();
            Gson gson = new Gson();
            String vendorInfoJson = gson.toJson(mVendorInfo);
            Log.i(TAG, "VendorInfo:" + vendorInfoJson);
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
//                OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
//                osw.write(vendorInfoJson);
//                osw.flush();
//                osw.close();
                outputStream.write(vendorInfoJson.getBytes());
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
                Log.i(TAG, "Successfully Created New Vendor");
                Toast.makeText(getApplicationContext(), "Successfully Created New Vendor!!", Toast.LENGTH_SHORT).show();
                redirectToVendorLanding();
            } else if (status == Constants.Status.ERR_INVALID) {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
                mSubmitFormView.requestFocus();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
                mSubmitFormView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mSubmitTask = null;
            showProgress(false);
        }
    }

    private void redirectToVendorLanding() {
        Intent intent = new Intent();
        intent.setClass(VendorCreateActivity.this, VendorLanding.class);
        startActivity(intent);
    }

    private class VendorInfo {
//        {"id":4,"name":"Test Vendor4","key":"testvend4","address_1":null,"address_2":null,
//            "state":null,"city":null,"pin":null,"phone_no":null,"cst":null,"tin":null,"gst":null,"details":null}
        String calltype = null;
        String name = null;
        String key = null;
        String address_1 = null;
        String address_2 = null;
        String state = null;
        String city = null;
        String pin = null;
        String phone_no = null;
        String cst = null;
        String tin = null;
        String gst = null;
        String details = null;
    }
}
