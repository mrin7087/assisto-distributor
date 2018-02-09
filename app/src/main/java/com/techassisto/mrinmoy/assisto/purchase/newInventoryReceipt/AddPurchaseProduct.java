package com.techassisto.mrinmoy.assisto.purchase.newInventoryReceipt;

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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.techassisto.mrinmoy.assisto.BarcodeFiles.ScanActivity;
import com.techassisto.mrinmoy.assisto.PurchaseProductInfo;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.Constants;

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
 * Created by sayantan on 26/10/17.
 */

public class AddPurchaseProduct extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "Assisto.AddPurProduct";
    private static final int SCAN_PRODUCT_REQUEST = 1;

    private Activity mActivity = null;
    private GetProductAPITask mProductAPITask = null;
    private View mProgressView = null;
    private View mAddProductFormView = null;
    private Button mSubmitBtn = null;
    // TODO: Remove these custom checkboxes
    private EditText mDiscount = null;
    private EditText mDiscount_2 = null;
    private int mWarehouseId = -1;

    private PurchaseProductInfo mProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_purchase_product);

        mActivity = this;
        mProgressView = findViewById(R.id.apigetproduct_progress);
        mAddProductFormView = findViewById(R.id.add_product_form);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mWarehouseId = -1;
            } else {
                mWarehouseId = extras.getInt("warehouseId");
            }
        } else {
            mWarehouseId = (int) savedInstanceState.getSerializable("warehouseId");
        }
        Log.i(TAG, "Warehouse ID: " + mWarehouseId);

        final PurchaseProductAutoCompleteTextView prodView = (PurchaseProductAutoCompleteTextView) findViewById(R.id.product_name);
        prodView.setThreshold(3);
        prodView.setAdapter(new PurchaseProductAutoCompleteAdapter(this));
        prodView.setLoadingIndicator(
                (android.widget.ProgressBar) findViewById(R.id.product_loading_indicator));
        prodView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                PurchaseProductAutoCompleteAdapter.Product product = (PurchaseProductAutoCompleteAdapter.Product) adapterView.getItemAtPosition(position);
//                prodView.setText(product.getLabel());
                Toast.makeText(getApplicationContext(), "Fetching Details.. " + product.getLabel(), Toast.LENGTH_SHORT).show();
                getProduct(product.getId(), false);
            }
        });

        List<String> discount1Type = new ArrayList<String>();
        discount1Type.add("Discount -1: No Discount");
        discount1Type.add("Discount -1: Percent");
        discount1Type.add("Discount -1: Value");

        List<String> discount2Type = new ArrayList<String>();
        discount2Type.add("Discount -2: No Discount");
        discount2Type.add("Discount -2: Percent");
        discount2Type.add("Discount -2: Value");

        Spinner disc1Spinner = (Spinner) findViewById(R.id.disc_1_type);
        ArrayAdapter<String> disc1Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, discount1Type);
        disc1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        disc1Spinner.setAdapter(disc1Adapter);
        disc1Spinner.setOnItemSelectedListener(this);

        Spinner disc2Spinner = (Spinner) findViewById(R.id.disc_2_type);
        ArrayAdapter<String> disc2Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, discount2Type);
        disc2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        disc2Spinner.setAdapter(disc2Adapter);
        disc2Spinner.setOnItemSelectedListener(this);


        mSubmitBtn = (Button) findViewById(R.id.submit_button);

        Button scanBtn = (Button) findViewById(R.id.scan_button);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(AddPurchaseProduct.this, CodeScannerActivity.class);
//                startActivityForResult(intent, SCAN_PRODUCT_REQUEST);

//                Intent intent = new Intent(AddPurchaseProduct.this, BarcodeCaptureActivity.class);
                Intent intent = new Intent(AddPurchaseProduct.this, ScanActivity.class);
                startActivityForResult(intent, SCAN_PRODUCT_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        Toast.makeText(getApplicationContext(), "Request code: " + requestCode, Toast.LENGTH_SHORT).show();

        if (requestCode == SCAN_PRODUCT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {

                Barcode barcodedata = data.getParcelableExtra("barcode");

                Log.i(TAG, "Barcode From Cam: " + barcodedata);

                String barcode = barcodedata.displayValue;
//                statusMessage.setText(R.string.barcode_success);
//                barcodeValue.setText(barcode.displayValue);

                Toast.makeText(getApplicationContext(), "Fetching Service Details: " + barcode, Toast.LENGTH_LONG).show();
                getProduct(barcode, true);
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.disc_1_type){
            String item = parent.getItemAtPosition(position).toString();
            EditText discount1ValueView = (EditText) findViewById(R.id.disc_1_value);
            if (item == "Discount -1: No Discount"){
                discount1ValueView.setText("0.00");
                discount1ValueView.setHint("");
                discount1ValueView.setVisibility(View.GONE);
                Log.i(TAG, "Discount: No discount 1" );
            }
            else{
                discount1ValueView.setVisibility(View.VISIBLE);
                Log.i(TAG, "Discount: Some discount 1" );
                discount1ValueView.setText("");
                discount1ValueView.setHint("Discount -1");
            }
        }else{
            String item = parent.getItemAtPosition(position).toString();
            EditText discount2ValueView = (EditText) findViewById(R.id.disc_2_value);
            if (item == "Discount -2: No Discount"){
                discount2ValueView.setText("0.00");
                discount2ValueView.setHint("");
                discount2ValueView.setVisibility(View.GONE);
            }
            else{
                discount2ValueView.setVisibility(View.VISIBLE);
                discount2ValueView.setText("");
                discount2ValueView.setHint("Discount -2");
            }
        }

        // Showing selected spinner item
//        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

            mAddProductFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mAddProductFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAddProductFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mAddProductFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void getProduct(final String prodDetail, boolean isBarcode) {
        if (mProductAPITask != null) {
            return;
        }

        showProgress(true);

        if (isBarcode) {
            Log.i(TAG, "getProduct by barcode:" + prodDetail);
        } else {
            Log.i(TAG, "getProduct by id:" + prodDetail);
        }

        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            mProductAPITask = new GetProductAPITask(authToken, prodDetail, isBarcode);
            mProductAPITask.execute((Void) null);
        } else {
            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
        }
    }

    // AsyncTask to send requests
    public class GetProductAPITask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.GetProdAPITask";
        private String targetURL = null;
        private final String mToken;
        private final String mProdDetail;
        private final boolean mBarcode;

        GetProductAPITask(String token, String prodDetail, boolean isBarcode) {
            mToken = token;
            mProdDetail = prodDetail;
            mBarcode = isBarcode;
        }

        @Override
        protected Integer doInBackground(Void... uInfo) {
            Log.i(TAG, "doInBackground");

            //Compose the get Request

            if (mBarcode) {
                targetURL = Constants.SERVER_ADDR + APIs.purchase_product_barcode_get;
                targetURL += ("?product_barcode=" + mProdDetail);
            } else {
                targetURL = Constants.SERVER_ADDR + APIs.purchase_product_id_get;
                targetURL += ("?product_id=" + mProdDetail);
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
                return parseProductInfo(response.toString());

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
            mProductAPITask = null;
            showProgress(false);

            if (status == Constants.Status.OK) {
                Log.i(TAG, "Successfully received product data");
                populateProductInfo();
            } else if (status == Constants.Status.ERR_INVALID) {
                Toast.makeText(getApplicationContext(), "Sorry! Service does not exist.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mProductAPITask = null;
            showProgress(false);
        }

        private int parseProductInfo(String product) {
            if (product.contentEquals("{\"error\": \"Service Does not exist\"}")) {
                Log.i(TAG, "Service doesn't exist");
                return Constants.Status.ERR_INVALID;
            }

            Log.i(TAG, "parse Service Info: " + product);
            Gson gson = new GsonBuilder().serializeNulls().create();
            PurchaseProductInfo productInfo = gson.fromJson(product, PurchaseProductInfo.class);
            Log.i(TAG, "ProductInfo :" + productInfo);

            mProduct = productInfo;

            return Constants.Status.OK;
        }

        private void populateProductInfo() {
            Log.i(TAG, "populate Service info" + mProduct);

            mSubmitBtn.setVisibility(View.VISIBLE);

            // Auto Fill the product form

            EditText pNameView = (EditText) findViewById(R.id.product_name);
//            EditText pIdView = (EditText) findViewById(R.id.product_id);
//            EditText pUnitView = (EditText) findViewById(R.id.product_unit);
//            Spinner pRatesView = (Spinner) findViewById(R.id.product_rate_spinner);
            EditText pCGSTView = (EditText) findViewById(R.id.product_cgst);
            EditText pSGSTView = (EditText) findViewById(R.id.product_sgst);
            EditText pQuantityView = (EditText) findViewById(R.id.product_quantity);

            EditText pPurchaseView = (EditText) findViewById(R.id.product_purchase);
            EditText pTSPView = (EditText) findViewById(R.id.product_tsp);
            EditText pMRPView = (EditText) findViewById(R.id.product_mrp);

            pNameView.setText(mProduct.product_name);
//            pIdView.setText(Integer.toString(mProduct.product_id));
//            pUnitView.setText(mProduct.unit);
            pCGSTView.setText(Double.toString(mProduct.cgst));
            pSGSTView.setText(Double.toString(mProduct.sgst));
            pQuantityView.setText(Integer.toString(0));
            pPurchaseView.setText(Integer.toString(0));
            pTSPView.setText(Integer.toString(0));
            pMRPView.setText(Integer.toString(0));

//            List<String> list = new ArrayList<String>();
//            for (int i=0; i<mProduct.rate.size(); i++) {
//                list.add(mProduct.rate.get(i).tentative_sales_rate);
//            }

            pPurchaseView.requestFocus();
        }
    }

    public void submitProduct(View v) {
        if (mProduct == null) {
            Log.e(TAG, "product is null!!");
            return;
        }

        //Error Handling
        // Check if quantity is 0
        EditText pQuantityView = (EditText) findViewById(R.id.product_quantity);
        EditText pPurchaseView = (EditText) findViewById(R.id.product_purchase);
        EditText pTSPView = (EditText) findViewById(R.id.product_tsp);
        EditText pMRPView = (EditText) findViewById(R.id.product_mrp);

        Spinner disc1Spinner = (Spinner) findViewById(R.id.disc_1_type);
        String discount1Text = disc1Spinner.getSelectedItem().toString();
        EditText discount1Value = (EditText) findViewById(R.id.disc_1_value);

        Spinner disc2Spinner = (Spinner) findViewById(R.id.disc_2_type);
        String discount2Text = disc2Spinner.getSelectedItem().toString();
        EditText discount2Value = (EditText) findViewById(R.id.disc_2_value);

        if (Integer.valueOf(pQuantityView.getText().toString()) == 0) {
            pQuantityView.setError("Quantity cannot be 0");
            pQuantityView.requestFocus();
            return;
        }

        // Set the selected quantity
        mProduct.selectedQuantity = Integer.valueOf(pQuantityView.getText().toString());
        mProduct.purchase_rate = Double.valueOf(pPurchaseView.getText().toString());
        mProduct.tsp = Double.valueOf(pTSPView.getText().toString());
        mProduct.mrp = Double.valueOf(pMRPView.getText().toString());

        if (discount1Text == "Discount -1: No Discount"){
            mProduct.disc_type = 0;
            mProduct.disc = 0;
        }else{
            try {
                mProduct.disc = Double.parseDouble(discount1Value.getText().toString());
            }catch (Exception e){
                mProduct.disc = 0;
            }
            if (discount1Text == "Discount -1: Percent"){
                mProduct.disc_type = 1;
            }else{
                mProduct.disc_type = 2;
            }
        }
        Log.i(TAG, "Discout 2 Type Text: " + discount2Text);
        if (discount2Text == "Discount -2: No Discount"){
            mProduct.disc_type_2 = 0;
            mProduct.disc_2 = 0;
        }else{
            try {
                mProduct.disc_2 = Double.parseDouble(discount2Value.getText().toString());
            }catch (Exception e){
                mProduct.disc_2 = 0;
            }
            if (discount2Text == "Discount -2: Percent"){
                mProduct.disc_type_2 = 1;
            }else{
                mProduct.disc_type_2 = 2;
            }
        }
        Log.i(TAG, "Discout 2 Type: " + mProduct.disc_type_2);


        // Send the added product
        Log.i(TAG, "Add Service : " + mProduct);
        String product = (new Gson().toJson(mProduct));
        Intent returnIntent = new Intent();
        returnIntent.putExtra("product", product);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

}
