package com.techassisto.mrinmoy.assisto.retailSales;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.ProductInfo;
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
import java.util.ArrayList;
import java.util.List;

public class NewSalesInvoice extends DashBoardActivity {
    private final static String TAG = "Assisto.NewSalesInvoice";
    private Activity mActivity = null;
    private ListView mListView = null;
    ArrayList<InvoiceProductListModel> mModelList;
    InvoiceProductListAdapter mAdapter = null;

    private View mProgressView = null;
    private View mInvoiceView = null;
    private InvoiceSaveTask mSubmitTask = null;

    private static final int ADD_PRODUCT_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_new_sales_invoice);
        Log.i(TAG, "oncreate");

        mActivity = this;

        mInvoiceView = findViewById(R.id.invoiceView);
        mProgressView = findViewById(R.id.apisubmit_progress);

        mListView = (ListView) findViewById(R.id.invoicelistview);
        mModelList = new ArrayList<InvoiceProductListModel>();
        mAdapter = new InvoiceProductListAdapter(mActivity, mModelList);
        mListView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(NewSalesInvoice.this, AddProduct.class);
                startActivityForResult(intent, ADD_PRODUCT_REQUEST);
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_new_sales_invoice;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            //Toast.makeText(getApplicationContext(), "Save", Toast.LENGTH_SHORT).show();

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle("Save Invoice")
                    .setMessage("Save current invoice ?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            saveInvoice();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveInvoice() {
        Log.i(TAG, "Save Invoice");

        if (mModelList.size() == 0) {
            Toast.makeText(getApplicationContext(), "Product list is empty!!", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i=0; i<mModelList.size(); i++) {
            Log.i(TAG, mModelList.get(i).getProduct().toString() + " qty: " + mModelList.get(i).getProduct().selectedQuantity);
        }

        // Create the data to be submitted in API format
        Product productArr[] = new Product[mModelList.size()];
        Log.i(TAG, "productArr size:" + productArr.length);

        double billTotal = 0;
        for (int  i=0; i<mModelList.size(); i++) {
            ProductInfo pInfo = mModelList.get(i).getProduct();

            productArr[i] = new Product();
            productArr[i].product_id = Integer.toString(pInfo.product_id);
            productArr[i].quantity = pInfo.selectedQuantity;
            productArr[i].unit_id = pInfo.unit_id;
            productArr[i].sales = pInfo.selectedRate;
            productArr[i].discount_amount = 0.0;
            productArr[i].cgst_p = pInfo.cgst;
            productArr[i].cgst_v = 0.0;
            productArr[i].sgst_p = pInfo.sgst;
            productArr[i].sgst_v = 0.0;
            productArr[i].taxable_total = pInfo.selectedRate;
            productArr[i].line_total = pInfo.selectedRate * pInfo.selectedQuantity;

            billTotal += productArr[i].line_total;
        }

        InvoiceDetails newInvoice = new InvoiceDetails();
        newInvoice.bill_details = productArr;
        newInvoice.cgsttotal = 0;
        newInvoice.sgsttotal = 0;
        newInvoice.subtotal = billTotal;
        newInvoice.total = billTotal;
        newInvoice.warehouse = 1;
        newInvoice.calltype = "mobilesave";

        showProgress(true);
        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            Log.i(TAG, "Start task to save new invoice...");
            mSubmitTask = new InvoiceSaveTask(newInvoice, authToken);
            mSubmitTask.execute((Void) null);
        } else {
            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
        }
    }


    private void addProduct(String product) {
        Log.i(TAG, "Product: " + product);
        ProductInfo productInfo = new Gson().fromJson(product, ProductInfo.class);
        Log.i(TAG, "ProductInfo: " + productInfo);

        mModelList.add(new InvoiceProductListModel(productInfo));

//        mModelList.add(new InvoiceProductListModel(
//                productInfo.product_name,
//                productInfo.selectedQuantity,
//                productInfo.selectedRate));
        mAdapter.notifyDataSetChanged();
        Log.i(TAG, "List adapter updated");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_PRODUCT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                String product = data.getStringExtra("product");
                if (product != null) {
                    addProduct(product);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Discard Invoice")
                .setMessage("Are you sure you want to discard current invoice ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        //super.onBackPressed();
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

            mInvoiceView.setVisibility(show ? View.GONE : View.VISIBLE);
            mInvoiceView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mInvoiceView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mInvoiceView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private class Product {
        String product_id;
        int quantity;
        int unit_id;
        double sales;
        double discount_amount;
        double cgst_p;
        double cgst_v;
        double sgst_p;
        double sgst_v;
        double taxable_total;
        double line_total;
    }

    private class InvoiceDetails {
        Product[] bill_details;
        String customer_phone;
        String customer_name;
        String customer_address;
        double subtotal;
        double cgsttotal;
        double sgsttotal;
        int warehouse;
        double total;
        String calltype;
        String customer_email;
    }

    /**
     * Represents an asynchronous task used to create
     * a new Sales Invoice.
     */
    public class InvoiceSaveTask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Assisto.InvoiceSaveTask";
        private static final String targetURL = Constants.SERVER_ADDR + APIs.retail_invoice_save;

        private final InvoiceDetails mInvoice;
        private final String mToken;

        InvoiceSaveTask(InvoiceDetails invoice, String authToken) {
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
                Log.i(TAG, "Successfully Created New Invoice");
                Toast.makeText(getApplicationContext(), "Successfully Saved New Invoice!!", Toast.LENGTH_SHORT).show();
            } else if (status == Constants.Status.ERR_INVALID) {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mSubmitTask = null;
            showProgress(false);
        }
    }
}
