package com.techassisto.mrinmoy.assisto.retailSales.retailNewInvoice;

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
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.techassisto.mrinmoy.assisto.BarcodeFiles.ScanActivity;
import com.techassisto.mrinmoy.assisto.CodeScannerActivity;
import com.techassisto.mrinmoy.assisto.ProductInfo;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.purchase.newInventoryReceipt.AddPurchaseProduct;
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

public class AddProduct extends AppCompatActivity {
    private static final String TAG = "Assisto.AddProduct";
    private static final int SCAN_PRODUCT_REQUEST = 1;

    private Activity mActivity = null;
    private GetProductAPITask mProductAPITask = null;
    private View mProgressView = null;
    private View mAddProductFormView = null;
    private Button mSubmitBtn = null;
    private CheckBox mCustomRateChkbox = null;
    private CheckBox mCustomIsTaxInclChkbox = null;
    private EditText mCustomRate = null;
    private int mWarehouseId = -1;

    private ProductInfo mProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        mActivity = this;
        mProgressView = findViewById(R.id.apigetproduct_progress);
        mAddProductFormView = findViewById(R.id.add_product_form);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                mWarehouseId = -1;
            } else {
                mWarehouseId = extras.getInt("warehouseId");
            }
        } else {
            mWarehouseId = (int) savedInstanceState.getSerializable("warehouseId");
        }
        Log.i(TAG, "Warehouse ID : " + mWarehouseId);

        final ProductAutoCompleteTextView prodView = (ProductAutoCompleteTextView) findViewById(R.id.product_name);
        prodView.setThreshold(3);
        prodView.setAdapter(new ProductAutoCompleteAdapter(this));
        prodView.setLoadingIndicator(
                (android.widget.ProgressBar) findViewById(R.id.product_loading_indicator));
        prodView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ProductAutoCompleteAdapter.Product product = (ProductAutoCompleteAdapter.Product) adapterView.getItemAtPosition(position);
//                prodView.setText(product.getLabel());
                Toast.makeText(getApplicationContext(), "Fetching Details.. " + product.getLabel(), Toast.LENGTH_SHORT).show();
                getProduct(product.getId(), false);
            }
        });

        mCustomRate = (EditText) findViewById(R.id.product_custom_rate);

        mCustomRateChkbox = (CheckBox) findViewById(R.id.product_custom_rate_chkbox);
        mCustomIsTaxInclChkbox = (CheckBox) findViewById(R.id.product_custom_istax_chkbox);

        mCustomRateChkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCustomIsTaxInclChkbox.setChecked(false);
                if(isChecked) {
                    mCustomRate.setVisibility(View.VISIBLE);
                    mCustomIsTaxInclChkbox.setVisibility(View.VISIBLE);
                } else {
                    mCustomRate.setVisibility(View.INVISIBLE);
                    mCustomIsTaxInclChkbox.setVisibility(View.GONE);
                }
            }
        });

        mSubmitBtn = (Button) findViewById(R.id.submit_button);

        Button scanBtn = (Button) findViewById(R.id.scan_button);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(AddProduct.this, CodeScannerActivity.class);
//                startActivityForResult(intent, SCAN_PRODUCT_REQUEST);
                Intent intent = new Intent(AddProduct.this, ScanActivity.class);
                startActivityForResult(intent, SCAN_PRODUCT_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCAN_PRODUCT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
//                String barcode = data.getStringExtra("barcode");
                Barcode barcodedata = data.getParcelableExtra("barcode");
                String barcode = barcodedata.displayValue;
                Toast.makeText(getApplicationContext(), "Fetching Product Details: " + barcode, Toast.LENGTH_LONG).show();
                getProduct(barcode, true);
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

            if(mBarcode) {
                targetURL = Constants.SERVER_ADDR + APIs.product_barcode_get;
                targetURL += ("?product_barcode=" + mProdDetail);
            } else {
                targetURL = Constants.SERVER_ADDR + APIs.product_id_get;
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
                Toast.makeText(getApplicationContext(), "Sorry! Product does not exist.", Toast.LENGTH_SHORT).show();
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
            if (product.contentEquals("{\"error\": \"Product Does not exist\"}")) {
                Log.i(TAG, "Product doesn't exist");
                return Constants.Status.ERR_INVALID;
            }

            Log.i(TAG, "parse Product Info: " + product);
            Gson gson = new GsonBuilder().serializeNulls().create();
            ProductInfo productInfo = gson.fromJson(product, ProductInfo.class);
            Log.i(TAG, "ProductInfo :" + productInfo);

            mProduct = productInfo;

            return Constants.Status.OK;
        }

        private void populateProductInfo() {
            Log.i(TAG, "populate Product info" + mProduct);

            mSubmitBtn.setVisibility(View.VISIBLE);

            // Auto Fill the product form

            EditText pNameView = (EditText) findViewById(R.id.product_name);
            EditText pIdView = (EditText) findViewById(R.id.product_id);
            EditText pUnitView = (EditText) findViewById(R.id.product_unit);
            Spinner pRatesView = (Spinner) findViewById(R.id.product_rate_spinner);
            EditText pCGSTView = (EditText) findViewById(R.id.product_cgst);
            EditText pSGSTView = (EditText) findViewById(R.id.product_sgst);
            EditText pQuantityView = (EditText) findViewById(R.id.product_quantity);

            pNameView.setText(mProduct.product_name);
            pIdView.setText(Integer.toString(mProduct.product_id));
            pUnitView.setText(mProduct.unit);
            pCGSTView.setText(Double.toString(mProduct.cgst));
            pSGSTView.setText(Double.toString(mProduct.sgst));
            pQuantityView.setText(Integer.toString(0));

            // Populate the rates spinner
            List<String> list = new ArrayList<String>();
            for (int i=0; i<mProduct.rate.size(); i++) {
                list.add(mProduct.rate.get(i).tentative_sales_rate);
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(mActivity,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            pRatesView.setAdapter(dataAdapter);

            pQuantityView.requestFocus();
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
        if (Integer.valueOf(pQuantityView.getText().toString()) == 0) {
            pQuantityView.setError("Quantity cannot be 0");
            pQuantityView.requestFocus();
            return;
        }

        // Set the selected quantity
        mProduct.selectedQuantity = Integer.valueOf(pQuantityView.getText().toString());

        if(!mCustomRateChkbox.isChecked()) {
            // Show error msg if no Rates are available.
            if (mProduct.rate.size() == 0) {
                Toast.makeText(getApplicationContext(), "No rates found!! Add a custom rate", Toast.LENGTH_LONG).show();
                return;
            }

            // TODO : Set the selected rate from SPINNER
            mProduct.selectedRate = Double.valueOf(mProduct.rate.get(0).tentative_sales_rate);
            mProduct.selectedIsTaxIncluded =  mProduct.rate.get(0).is_tax_included;
        } else {
            if ((mCustomRate.getText().toString().length() == 0) ||
                (Double.valueOf(mCustomRate.getText().toString()) == 0)) {
                mCustomRate.setError("Add proper custom rate!");
                mCustomRate.requestFocus();
                return;
            }
//            mCustomIsTaxInclChkbox = (CheckBox) findViewById(R.id.product_custom_istax_chkbox);
            mProduct.selectedRate = Double.valueOf(mCustomRate.getText().toString());
            mProduct.selectedIsTaxIncluded = mCustomIsTaxInclChkbox.isChecked();
        }

        // Send the added product
        String product = (new Gson().toJson(mProduct));
        Intent returnIntent = new Intent();
        returnIntent.putExtra("product", product);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}