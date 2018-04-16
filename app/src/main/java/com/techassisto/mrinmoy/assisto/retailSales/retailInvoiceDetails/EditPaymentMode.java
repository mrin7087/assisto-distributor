package com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceDetails;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.techassisto.mrinmoy.assisto.PaymentModeOption;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.retailSales.PaymentModeAdapter;
import com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceList.InvoiceActivity;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.ApiClient;
import com.techassisto.mrinmoy.assisto.utils.ApiInterface;
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
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sayantan on 15/3/18.
 */

public class EditPaymentMode extends AppCompatActivity {

    private static final String TAG = "Assisto.UpdatePayMode";
    private static final int SCAN_PRODUCT_REQUEST = 1;

    private Activity mActivity = null;
    private Spinner mPaymentModeSpnr = null;
    String invoiceId;
    String originalMode;
    private int mPaymentModeId = -1;
    private String mPaymentModeName = null;
    private PayModeUpdateTask mPayModeUpdateTask = null;
    private Button updateBtn = null;

//    private ProductInfo mProduct;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sales_pay_mode);

        Intent intent = this.getIntent();
        invoiceId = intent.getStringExtra("Invoice ID");
//        originalMode = intent.getStringExtra("Original Mode");

        mActivity = this;

        getPaymentMode();

        updateBtn = (Button) findViewById(R.id.update_button);

        mPaymentModeSpnr = (Spinner) findViewById(R.id.paymentMode_spinner);
        mPaymentModeSpnr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PaymentModeOption pminfo = (PaymentModeOption) parent.getItemAtPosition(position);
                mPaymentModeId = pminfo.getId();
                mPaymentModeName = pminfo.getName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(),
                        "Payment Mode not selected!!", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void getPaymentMode() {

        Log.i(TAG, "get Payment Modes...");

        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);
            String authorization = "jwt "+authToken;
            Call<List<PaymentModeOption>> call = apiService.getRetailPaymentMode(authorization);
            call.enqueue(new Callback<List<PaymentModeOption>>() {
                @Override
                public void onResponse(Call<List<PaymentModeOption>>call, Response<List<PaymentModeOption>> response) {
                    mPaymentModeSpnr = (Spinner) findViewById(R.id.paymentMode_spinner);
                    List<PaymentModeOption> modesList = new ArrayList<>();
                    for (int i=0; i<response.body().size(); i++) {
                        modesList.add(response.body().get(i));
                    }

                    for (int i=0; i<modesList.size(); i++) {
                        Log.i(TAG, "Pay Mode name:" + modesList.get(i).getName());
                    }

                    Log.i(TAG, "Pay Modes:" + response.body());
                    PaymentModeAdapter dataAdapter = new PaymentModeAdapter(mActivity,android.R.layout.simple_spinner_item, modesList);
                    mPaymentModeSpnr.setAdapter(dataAdapter);
                }

                @Override
                public void onFailure(Call<List<PaymentModeOption>>call, Throwable t) {
                    // Log error here since request failed
                    Log.e(TAG, t.toString());
                }
            });

        } else {
//            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
        }
    }

    private class InvoiceDetails {
        String invoice_id;
        int revised_mode_id;
        String calltype;
    }


    public void updatePayment(View v) {

        InvoiceDetails oldInvoice = new InvoiceDetails();
        oldInvoice.invoice_id = invoiceId;
        oldInvoice.revised_mode_id = mPaymentModeId;
        oldInvoice.calltype = "change_payment_mode";

        updateBtn.setEnabled(false);

        if (mPaymentModeId == -1){
            Toast.makeText(getApplicationContext(), "Ughh!! Please select a payment mode.", Toast.LENGTH_SHORT).show();
        }
        else {
            //Get the auth token
            SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
            String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
            if (authToken != null) {
                Log.i(TAG, "Start task to save new invoice...");
                mPayModeUpdateTask = new PayModeUpdateTask(oldInvoice, authToken);
                mPayModeUpdateTask.execute((Void) null);
            } else {
                //            showProgress(false);
                Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

                // REDIRECT TO LOGIN PAGE
            }
        }

    }

    public class PayModeUpdateTask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.InvoiceEditTask";
        private static final String targetURL = Constants.SERVER_ADDR + APIs.retail_invoice_edit;

        private final InvoiceDetails mInvoice;
        private final String mToken;

        PayModeUpdateTask(InvoiceDetails invoice, String authToken) {
            mInvoice = invoice;
            mToken = authToken;
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
            mPayModeUpdateTask = null;
//            showProgress(false);

            if (status == Constants.Status.OK) {
                Log.i(TAG, "Successfully Updated Invoice");
                Toast.makeText(getApplicationContext(), "Successfully Updated Payment Mode!!", Toast.LENGTH_SHORT).show();

//                mModelList.clear();
//                mAdapter.notifyDataSetChanged();

                Intent intent = new Intent();
                intent.setClass(EditPaymentMode.this, InvoiceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            } else if (status == Constants.Status.ERR_INVALID) {
                updateBtn.setEnabled(true);
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            } else {
                updateBtn.setEnabled(true);
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mPayModeUpdateTask = null;
            updateBtn.setEnabled(true);
//            showProgress(false);
        }
    }

    public void cancelUpdate(View view){
        super.onBackPressed();
    }
}
