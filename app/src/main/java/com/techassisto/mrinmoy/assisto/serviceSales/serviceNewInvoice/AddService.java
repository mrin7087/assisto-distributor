package com.techassisto.mrinmoy.assisto.serviceSales.serviceNewInvoice;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.ServiceInfo;
import com.techassisto.mrinmoy.assisto.UserListInfo;
import com.techassisto.mrinmoy.assisto.serviceSales.SalespersonAdapter;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.Constants;

import org.json.JSONArray;
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

/**
 * Created by sayantan on 4/12/17.
 */

public class AddService extends AppCompatActivity {

    private static final String TAG = "Assisto.AddService";
    private static final int SCAN_SERVICE_REQUEST = 1;

    private Activity mActivity = null;
    private GetServiceAPITask mServiceAPITask = null;
    private View mProgressView = null;
    private View mAddServiceFormView = null;
    private Button mSubmitBtn = null;
    private CheckBox mCustomRateChkbox = null;
    private CheckBox mCustomIsTaxInclChkbox = null;
    private EditText mCustomRate = null;
    private CheckBox mSalesperson1 = null;
    private CheckBox mSalesperson2 = null;
    private CheckBox mSalesperson3 = null;
    private int mWarehouseId = -1;
    private String mSalespersonStr;
    private JSONArray mSalespersonList = null;

    // Salespersons
    private LinearLayout salesperson1;
    private LinearLayout salesperson2;
    private LinearLayout salesperson3;

    private Spinner mSalesperson1Spnr = null;
    private Spinner mSalesperson2Spnr = null;
    private Spinner mSalesperson3Spnr = null;

    private int mSalesperson1Id = -1;
    private int mSalesperson2Id = -1;
    private int mSalesperson3Id = -1;

    private EditText mSalesperson1Contrib = null;
    private EditText mSalesperson2Contrib = null;
    private EditText mSalesperson3Contrib = null;

    private CheckBox salesperson1_remove = null;
    private CheckBox salesperson2_remove = null;
    private CheckBox salesperson3_remove = null;

    private ServiceInfo mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        mActivity = this;
        mProgressView = findViewById(R.id.apigetservice_progress);
        mAddServiceFormView = findViewById(R.id.add_service_form);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                mWarehouseId = -1;
            } else {
                mWarehouseId = extras.getInt("warehouseId");
                mSalespersonStr = extras.getString("salespersons");
            }
        } else {
            mWarehouseId = (int) savedInstanceState.getSerializable("warehouseId");
            mSalespersonStr =  (String) savedInstanceState.getSerializable("salespersons");
        }
        Log.i(TAG, "Warehouse ID : " + mWarehouseId);
        try {
            mSalespersonList = new JSONArray(mSalespersonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Payment Test : " + mSalespersonStr);
        Log.i(TAG, "Payment Test JSON: " + mSalespersonList);

        final ServiceAutoCompleteTextView serviceView = (ServiceAutoCompleteTextView) findViewById(R.id.service_name);
        serviceView.setThreshold(3);
        serviceView.setAdapter(new ServiceAutoCompleteAdapter(this));
        serviceView.setLoadingIndicator(
                (android.widget.ProgressBar) findViewById(R.id.service_loading_indicator));
        serviceView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ServiceAutoCompleteAdapter.Service service = (ServiceAutoCompleteAdapter.Service) adapterView.getItemAtPosition(position);
//                serviceView.setText(product.getLabel());
                Toast.makeText(getApplicationContext(), "Fetching Details.. " + service.getLabel(), Toast.LENGTH_SHORT).show();
                getService(service.getId(), false);
            }
        });

        mCustomRate = (EditText) findViewById(R.id.service_custom_rate);

        mCustomRateChkbox = (CheckBox) findViewById(R.id.service_custom_rate_chkbox);
        mCustomIsTaxInclChkbox = (CheckBox) findViewById(R.id.service_custom_istax_chkbox);

        mCustomRateChkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCustomIsTaxInclChkbox.setChecked(false);
                if(isChecked) {
                    mCustomRate.setVisibility(View.VISIBLE);
                    mCustomRate.setHint(R.string.prompt_product_custom_rate);
                    mCustomIsTaxInclChkbox.setVisibility(View.VISIBLE);
                } else {
                    mCustomRate.setHint("");
                    mCustomRate.setVisibility(View.GONE);
                    mCustomIsTaxInclChkbox.setVisibility(View.GONE);
                }
            }
        });


        mSalesperson1 = (CheckBox) findViewById(R.id.salesperson1_checkbox);

        mSalesperson1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
//                    mSalesperson1Contrib = (EditText) findViewById(R.id.salesperson1_contrib);
//                    String contrib = mSalesperson1Contrib.getText().toString().trim();
//                    if(contrib.isEmpty() || contrib.length() == 0 || contrib.equals("") || contrib == null){
//                        Toast.makeText(getApplicationContext(), "Please enter 1st salesperson contribution. ", Toast.LENGTH_LONG).show();
//                        mSalesperson1.setChecked(false);
//                    }
//                    else if (Float.parseFloat(contrib) == 0f){
//                        Toast.makeText(getApplicationContext(), "Please enter 1st salesperson contribution. ", Toast.LENGTH_LONG).show();
//                        mSalesperson1.setChecked(false);
//                    }
//                    else {
                        salesperson1 = (LinearLayout) findViewById(R.id.salesperson1_view);
                        salesperson1.setVisibility(View.VISIBLE);
                        mSalesperson1.setVisibility(View.GONE);
//                    }
                }
            }
        });

        mSalesperson2 = (CheckBox) findViewById(R.id.salesperson2_checkbox);

        mSalesperson2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    mSalesperson1Contrib = (EditText) findViewById(R.id.salesperson1_contrib);
                    String contrib = mSalesperson1Contrib.getText().toString().trim();
                    if(contrib.isEmpty() || contrib.length() == 0 || contrib.equals("") || contrib == null){
                        Toast.makeText(getApplicationContext(), "Please enter 1st salesperson contribution. ", Toast.LENGTH_LONG).show();
                        mSalesperson2.setChecked(false);
                    }
                    else if (Float.parseFloat(contrib) == 0f){
                        Toast.makeText(getApplicationContext(), "Please enter 1st salesperson contribution. ", Toast.LENGTH_LONG).show();
                        mSalesperson2.setChecked(false);
                    }
                    else {
                        salesperson2 = (LinearLayout) findViewById(R.id.salesperson2_view);
                        salesperson2.setVisibility(View.VISIBLE);
//                    mSalesperson2.setVisibility(View.GONE);
                        salesperson1.setVisibility(View.GONE);
                    }
                }
            }
        });

        mSalesperson3 = (CheckBox) findViewById(R.id.salesperson3_checkbox);

        mSalesperson3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    mSalesperson2Contrib = (EditText) findViewById(R.id.salesperson2_contrib);
                    String contrib = mSalesperson2Contrib.getText().toString().trim();
                    if(contrib.isEmpty() || contrib.length() == 0 || contrib.equals("") || contrib == null){
                        Toast.makeText(getApplicationContext(), "Please enter 2nd salesperson contribution. ", Toast.LENGTH_LONG).show();
                        mSalesperson3.setChecked(false);
                    }
                    else if (Float.parseFloat(contrib) == 0f){
                        Toast.makeText(getApplicationContext(), "Please enter 2nd salesperson contribution. ", Toast.LENGTH_LONG).show();
                        mSalesperson3.setChecked(false);
                    }
                    else {
                        salesperson3 = (LinearLayout) findViewById(R.id.salesperson3_view);
                        salesperson3.setVisibility(View.VISIBLE);
//                    mSalesperson3.setVisibility(View.GONE);
                        salesperson2.setVisibility(View.GONE);
                    }
                }
            }
        });

        salesperson1_remove = (CheckBox) findViewById(R.id.salesperson1_remove_checkbox);
        salesperson1_remove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    mSalesperson1Spnr = (Spinner) findViewById(R.id.salesperson1);
                    mSalesperson1Spnr.setSelection(0);
                    mSalesperson1Contrib = (EditText) findViewById(R.id.salesperson1_contrib);
                    mSalesperson1Contrib.setText("");

                    salesperson1 = (LinearLayout) findViewById(R.id.salesperson1_view);
                    salesperson1.setVisibility(View.GONE);
                    salesperson1_remove.setChecked(false);
                    mSalesperson1.setVisibility(View.VISIBLE);
                    mSalesperson1.setChecked(false);
                }
            }
        });

        salesperson2_remove = (CheckBox) findViewById(R.id.salesperson2_remove_checkbox);
        salesperson2_remove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    mSalesperson2Spnr = (Spinner) findViewById(R.id.salesperson2);
                    mSalesperson2Spnr.setSelection(0);
                    mSalesperson2Contrib = (EditText) findViewById(R.id.salesperson2_contrib);
                    mSalesperson2Contrib.setText("");


                    salesperson2 = (LinearLayout) findViewById(R.id.salesperson2_view);
                    salesperson2.setVisibility(View.GONE);
                    salesperson2_remove.setChecked(false);
//                    mSalesperson2.setVisibility(View.VISIBLE);
                    salesperson1.setVisibility(View.VISIBLE);
                    mSalesperson2.setChecked(false);

                }
            }
        });

        salesperson3_remove = (CheckBox) findViewById(R.id.salesperson3_remove_checkbox);
        salesperson3_remove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    mSalesperson3Spnr = (Spinner) findViewById(R.id.salesperson3);
                    mSalesperson3Spnr.setSelection(0);
                    mSalesperson3Contrib = (EditText) findViewById(R.id.salesperson3_contrib);
                    mSalesperson3Contrib.setText("");

                    salesperson3 = (LinearLayout) findViewById(R.id.salesperson3_view);
                    salesperson3.setVisibility(View.GONE);
                    salesperson3_remove.setChecked(false);
                    salesperson2.setVisibility(View.VISIBLE);
                    mSalesperson3.setChecked(false);

//                    salesperson3 = (LinearLayout) findViewById(R.id.salesperson3_view);
//                    salesperson3.setVisibility(View.GONE);
//                    salesperson3_remove.setChecked(false);
//                    mSalesperson3.setVisibility(View.VISIBLE);
//                    mSalesperson3.setChecked(false);
                }
            }
        });

        populateSalespersons();

        mSubmitBtn = (Button) findViewById(R.id.submit_button);

//        Button scanBtn = (Button) findViewById(R.id.scan_button);
//        scanBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Intent intent = new Intent();
////                intent.setClass(AddProduct.this, CodeScannerActivity.class);
////                startActivityForResult(intent, SCAN_SERVICE_REQUEST);
//                Intent intent = new Intent(AddService.this, ScanActivity.class);
//                startActivityForResult(intent, SCAN_SERVICE_REQUEST);
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCAN_SERVICE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
//                String barcode = data.getStringExtra("barcode");
                Barcode barcodedata = data.getParcelableExtra("barcode");
                String barcode = barcodedata.displayValue;
                Toast.makeText(getApplicationContext(), "Fetching Service Details: " + barcode, Toast.LENGTH_LONG).show();
                getService(barcode, true);
            }
        }
    }

    /**
     * Create salespersons spinners
     */

    private void  populateSalespersons(){

        // Populate the rates spinner
        mSalesperson1Spnr = (Spinner) findViewById(R.id.salesperson1);
        mSalesperson1Spnr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UserListInfo spinfo = (UserListInfo) parent.getItemAtPosition(position);
//                    Toast.makeText(getApplicationContext(),
//                            wh.name, Toast.LENGTH_SHORT).show()
                mSalesperson1Id = spinfo.id;
//                mPaymentModeName = spinfo.name;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(),
                        "Nothing selected!!", Toast.LENGTH_SHORT).show();
            }
        });

        mSalesperson2Spnr = (Spinner) findViewById(R.id.salesperson2);
        mSalesperson2Spnr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UserListInfo spinfo = (UserListInfo) parent.getItemAtPosition(position);
//                    Toast.makeText(getApplicationContext(),
//                            wh.name, Toast.LENGTH_SHORT).show()
                mSalesperson2Id = spinfo.id;
//                mPaymentModeName = spinfo.name;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(),
                        "Nothing selected!!", Toast.LENGTH_SHORT).show();
            }
        });

        mSalesperson3Spnr = (Spinner) findViewById(R.id.salesperson3);
        mSalesperson3Spnr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UserListInfo spinfo = (UserListInfo) parent.getItemAtPosition(position);
//                    Toast.makeText(getApplicationContext(),
//                            wh.name, Toast.LENGTH_SHORT).show()
                mSalesperson3Id = spinfo.id;
//                mPaymentModeName = spinfo.name;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(),
                        "Nothing selected!!", Toast.LENGTH_SHORT).show();
            }
        });

        List<UserListInfo> list = new ArrayList<>();
        for (int i=0; i<mSalespersonList.length(); i++) {
            try {
                JSONObject salespersons = mSalespersonList.getJSONObject(i);
                Log.i(TAG, "Username:" + salespersons.getString("username"));
                Log.i(TAG, "User ID:" + salespersons.getInt("id"));

                Gson gson = new GsonBuilder().serializeNulls().create();
                UserListInfo sp = gson.fromJson(salespersons.toString(), UserListInfo.class);

                //list.add(Integer.toString(warehouse.getInt("id")));
                list.add(sp);
            } catch (Exception e) {
                Log.e(TAG, "Failed to populate payment mode list, ex:" + e);
            }
        }
//        for (UserListInfo spinfo: list){
//            Log.i(TAG, "Payment Mode Array: "+spinfo.name);
//        }
        SalespersonAdapter dataAdapter = new SalespersonAdapter(mActivity,android.R.layout.simple_spinner_item, list);
        mSalesperson1Spnr.setAdapter(dataAdapter);
        mSalesperson2Spnr.setAdapter(dataAdapter);
        mSalesperson3Spnr.setAdapter(dataAdapter);

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

            mAddServiceFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mAddServiceFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAddServiceFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mAddServiceFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void getService(final String serviceDetail, boolean isBarcode) {
        if (mServiceAPITask != null) {
            return;
        }

        showProgress(true);

        if (isBarcode) {
            Log.i(TAG, "getService by barcode:" + serviceDetail);
        } else {
            Log.i(TAG, "getService by id:" + serviceDetail);
        }

        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            mServiceAPITask = new GetServiceAPITask(authToken, serviceDetail, isBarcode);
            mServiceAPITask.execute((Void) null);
        } else {
            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
        }
    }

    public class GetServiceAPITask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.GetServAPITask";
        private String targetURL = null;
        private final String mToken;
        private final String mServiceDetail;
        private final boolean mBarcode;

        GetServiceAPITask(String token, String serviceDetail, boolean isBarcode) {
            mToken = token;
            mServiceDetail = serviceDetail;
            mBarcode = isBarcode;
        }

        @Override
        protected Integer doInBackground(Void... uInfo) {
            Log.i(TAG, "doInBackground");

            //Compose the get Request

            if(mBarcode) {
                targetURL = Constants.SERVER_ADDR + APIs.product_barcode_get;
                targetURL += ("?service_barcode=" + mServiceDetail);
            } else {
                targetURL = Constants.SERVER_ADDR + APIs.service_id_get;
                targetURL += ("?service_id=" + mServiceDetail);
            }

            StringBuffer response = new StringBuffer();

            Log.i(TAG, "try to POST HTTP request");
            HttpURLConnection httpConnection = null;
            try {
                targetURL += ("&warehouse_id=" + Integer.toString(mWarehouseId));
                Log.i(TAG, "Sending request:" + targetURL);
                URL targetUrl = new URL(targetURL);
                httpConnection = (HttpURLConnection) targetUrl.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Authorization", "jwt " + mToken);
                httpConnection.setConnectTimeout(10000); //10secs
                httpConnection.connect();

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
                // Save the product details
                return parseServiceInfo(response.toString());

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
            mServiceAPITask = null;
            showProgress(false);

            if (status == Constants.Status.OK) {
                Log.i(TAG, "Successfully received service data");
                populateServiceInfo();
            } else if (status == Constants.Status.ERR_INVALID) {
                Toast.makeText(getApplicationContext(), "Sorry! Service does not exist.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mServiceAPITask = null;
            showProgress(false);
        }

        private int parseServiceInfo(String service) {
            if (service.contentEquals("{\"error\": \"Service Does not exist\"}")) {
                Log.i(TAG, "Service doesn't exist");
                return Constants.Status.ERR_INVALID;
            }

            Log.i(TAG, "parse Service Info: " + service);
            Gson gson = new GsonBuilder().serializeNulls().create();
            ServiceInfo serviceInfo = gson.fromJson(service, ServiceInfo.class);
            Log.i(TAG, "ServiceInfo :" + serviceInfo);

            mService = serviceInfo;

            return Constants.Status.OK;
        }

        private void populateServiceInfo() {
            Log.i(TAG, "populate Service info" + mService);

            mSubmitBtn.setVisibility(View.VISIBLE);

            // Auto Fill the product form

            EditText pNameView = (EditText) findViewById(R.id.service_name);
            EditText pIdView = (EditText) findViewById(R.id.service_id);
            EditText pUnitView = (EditText) findViewById(R.id.service_unit);
            Spinner pRatesView = (Spinner) findViewById(R.id.service_rate_spinner);
            EditText pCGSTView = (EditText) findViewById(R.id.service_cgst);
            EditText pSGSTView = (EditText) findViewById(R.id.service_sgst);
            EditText pQuantityView = (EditText) findViewById(R.id.service_quantity);

            pNameView.setText(mService.service_name);
            pIdView.setText(Integer.toString(mService.service_id));
            pUnitView.setText(mService.unit);
            pCGSTView.setText(Double.toString(mService.cgst));
            pSGSTView.setText(Double.toString(mService.sgst));
            pQuantityView.setText(Integer.toString(0));

            // Populate the rates spinner
            List<String> list = new ArrayList<String>();
            for (int i=0; i<mService.rate.size(); i++) {
                list.add(mService.rate.get(i).tentative_sales_rate);
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(mActivity,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            pRatesView.setAdapter(dataAdapter);

            pQuantityView.requestFocus();
        }
    }

    public void submitService(View v) {
        if (mService == null) {
            Log.e(TAG, "service is null!!");
            return;
        }

        //Error Handling
        // Check if quantity is 0
        EditText pQuantityView = (EditText) findViewById(R.id.service_quantity);
        if (Integer.valueOf(pQuantityView.getText().toString()) == 0) {
            pQuantityView.setError("Quantity cannot be 0");
            pQuantityView.requestFocus();
            return;
        }

        float contribution_total = 0;
        boolean check = false;

        JsonArray salesperson_array = new JsonArray();
        JsonObject salesperson = new JsonObject();
        mSalesperson1 = (CheckBox) findViewById(R.id.salesperson1_checkbox);
        if(mSalesperson1.isChecked()) {
            check = true;
            mSalesperson1Contrib = (EditText) findViewById(R.id.salesperson1_contrib);
            Log.i(TAG, "Contrib 1:" + mSalesperson1Contrib.getText().toString());
            try {
                contribution_total += Float.parseFloat(mSalesperson1Contrib.getText().toString());
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "Salesperson 1 contribution is not properly entered.", Toast.LENGTH_LONG).show();
                return;
            }
            salesperson.addProperty("id", mSalesperson1Id);
            salesperson.addProperty("cont", mSalesperson1Contrib.getText().toString());
            salesperson_array.add(salesperson);
        }

        Log.i(TAG, "Salespersons after 1" + salesperson_array.toString());

        mSalesperson2 = (CheckBox) findViewById(R.id.salesperson2_checkbox);
        if(mSalesperson2.isChecked()) {
            check = true;
            salesperson = new JsonObject();
            mSalesperson2Contrib = (EditText) findViewById(R.id.salesperson2_contrib);
            try {
                contribution_total += Float.parseFloat(mSalesperson2Contrib.getText().toString());
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "Salesperson 2 contribution is not properly entered.", Toast.LENGTH_LONG).show();
                return;
            }
            Log.i(TAG, "Contrib 2:" + mSalesperson2Contrib.getText().toString());
            salesperson.addProperty("id", mSalesperson2Id);
            salesperson.addProperty("cont", mSalesperson2Contrib.getText().toString());
            salesperson_array.add(salesperson);
        }

        Log.i(TAG, "Salespersons after 2" + salesperson_array.toString());

        mSalesperson3 = (CheckBox) findViewById(R.id.salesperson3_checkbox);
        if(mSalesperson3.isChecked()) {
            check = true;
            salesperson = new JsonObject();
            mSalesperson3Contrib = (EditText) findViewById(R.id.salesperson3_contrib);
            Log.i(TAG, "Contrib 3:" + mSalesperson3Contrib.getText().toString());
            try {
                contribution_total += Float.parseFloat(mSalesperson3Contrib.getText().toString());
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "Salesperson 3 contribution is not properly entered.", Toast.LENGTH_LONG).show();
                return;
            }
            salesperson.addProperty("id", mSalesperson3Id);
            salesperson.addProperty("cont", mSalesperson3Contrib.getText().toString());
            salesperson_array.add(salesperson);
        }
        Log.i(TAG, "Salespersons after 3" + salesperson_array.toString());

        if (check && contribution_total!=1f){
            Log.i(TAG, "Contrib Total:" + contribution_total);
            Toast.makeText(getApplicationContext(), "Total Salespersons Contribution must be equal to 1", Toast.LENGTH_LONG).show();
            return;
        }
        //Set the salespersons
        mService.salespersons = salesperson_array;

        // Set the selected quantity
        mService.selectedQuantity = Integer.valueOf(pQuantityView.getText().toString());

        if(!mCustomRateChkbox.isChecked()) {
            // Show error msg if no Rates are available.
            if (mService.rate.size() == 0) {
                Toast.makeText(getApplicationContext(), "No rates found!! Add a custom rate", Toast.LENGTH_LONG).show();
                return;
            }

            // TODO : Set the selected rate from SPINNER
            mService.selectedRate = Double.valueOf(mService.rate.get(0).tentative_sales_rate);
            mService.selectedIsTaxIncluded =  mService.rate.get(0).is_tax_included;
        } else {
            if ((mCustomRate.getText().toString().length() == 0) ||
                    (Double.valueOf(mCustomRate.getText().toString()) == 0)) {
                mCustomRate.setError("Add proper custom rate!");
                mCustomRate.requestFocus();
                return;
            }
//            mCustomIsTaxInclChkbox = (CheckBox) findViewById(R.id.service_custom_istax_chkbox);
            mService.selectedRate = Double.valueOf(mCustomRate.getText().toString());
            mService.selectedIsTaxIncluded = mCustomIsTaxInclChkbox.isChecked();
        }

        Log.i(TAG, "Salespersons " + mService.salespersons.toString());
        // Send the added service
        String service = (new Gson().toJson(mService));
        Intent returnIntent = new Intent();
        returnIntent.putExtra("service", service);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
