package com.techassisto.mrinmoy.assisto.PrintBarcode;

/**
 * Created by sayantan on 16/4/18.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.godex.Godex;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.utilDeclaration.ProductBarcodePrint;
import com.techassisto.mrinmoy.assisto.utils.ApiClient;
import com.techassisto.mrinmoy.assisto.utils.ApiInterface;
import com.techassisto.mrinmoy.assisto.utils.Constants;
import com.techassisto.mrinmoy.assisto.utils.TenantInfo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrintBarcodeActivity extends DashBoardActivity
{

    private static final String TAG = "Assisto.PrintBarcode";
//    private Button sendButton,disconnectButton,printButton,settingButton,uploadButton;
    private Button printButton;

    private ProductBarcodePrint mProductBarcodePrint = null;

    private Spinner mIdentifierTypeSpinner;
    EditText mProductNameText;

    private LinearLayout productLayout;
    private LinearLayout printLayout;
    private TenantInfo tenantInfo;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_print_barcode);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();

        Godex.debug(3);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Spinner spinner = (Spinner) findViewById(R.id.select_identifier);
//        Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.product_identifier_array, android.R.layout.simple_spinner_item);
//        Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String tenant = userPref.getString(Constants.UserPref.SP_TENANT, null);
        if (tenant != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            tenantInfo = gson.fromJson(tenant, TenantInfo.class);
            Log.i(TAG, "Tenant:" + tenantInfo.tenant_name + " First Name:" + tenantInfo.first_name);
        }

        Button getProductButton = (Button) findViewById(R.id.getProduct);
        getProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInvoiceDetail();
            }
        });

        printButton = (Button) findViewById(R.id.print);
        printButton.setOnClickListener
                ( new OnClickListener()
                  {
                      public void onClick(View v)
                      {

                          if (mProductBarcodePrint == null){
                              Toast.makeText(getApplicationContext(), "Hmm!! Kindly select a product first.", Toast.LENGTH_SHORT).show();
                          }
                          else {
                              Godex.setup("38", "10", "2", "0", "3", "0");
                              Godex.sendCommand("^L");

                              Godex.sendCommand("AB,120,20,1,1,0,0," + tenantInfo.tenant_name);
                              Godex.sendCommand("AA,120,60,1,1,0,0," + mProductBarcodePrint.getName());
                              Godex.sendCommand("AA,120,90,1,1,0,0,Rs." + mProductBarcodePrint.getRate());
                              Godex.sendCommand("BA,120,120,1,2,80,0,1," + mProductBarcodePrint.getBarcode());

                              Godex.sendCommand("AB,450,20,1,1,0,0," + tenantInfo.tenant_name);
                              Godex.sendCommand("AA,450,60,1,1,0,0," + mProductBarcodePrint.getName());
                              Godex.sendCommand("AA,450,90,1,1,0,0,Rs." + mProductBarcodePrint.getRate());
                              Godex.sendCommand("BA,450,120,1,2,80,0,1," + mProductBarcodePrint.getBarcode());
                              Godex.sendCommand("E");
                          }
                      }
                  }
                );


    }

    public int getLayoutResId() {
        return R.layout.activity_print_barcode;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
    }

    public void getInvoiceDetail(){

        mIdentifierTypeSpinner=(Spinner) findViewById(R.id.select_identifier);
        String entryType = mIdentifierTypeSpinner.getSelectedItem().toString();

        mProductNameText = (EditText) findViewById(R.id.product_name);
        String name      =  mProductNameText.getText().toString();

        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);
            String authorization = "jwt " + authToken;

            Call<ProductBarcodePrint> call = apiService.getProductBarcodePrintDetail(entryType, name, authorization);
            call.enqueue(new Callback<ProductBarcodePrint>() {
                @Override
                public void onResponse(Call<ProductBarcodePrint> call, Response<ProductBarcodePrint> response) {

                    try {
//                        productLayout = (LinearLayout) findViewById(R.id.product_layout);
                        printLayout = (LinearLayout) findViewById(R.id.print_layout);
//                        productLayout.setVisibility(View.GONE);
                        printLayout.setVisibility(View.VISIBLE);

                        mProductBarcodePrint = response.body();

//                        TextView invoiceView = (TextView) findViewById(R.id.invoice_no);
//                        invoiceView.setText(invoice_no);
//
//                        TextView dateView = (TextView) findViewById(R.id.date);
//                        dateView.setText(mInvoiceDetail.getDate());


//                    mAdapter.notifyDataSetChanged();

                        Log.i(TAG, "Invoice Detail:");
//                    PaymentModeAdapter dataAdapter = new PaymentModeAdapter(mActivity, android.R.layout.simple_spinner_item, modesList);
//                    mPaymentModeSpnr.setAdapter(dataAdapter);
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(), "Oopss!! Couldn't get the product detail. Kindly recheck your entry.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ProductBarcodePrint> call, Throwable t) {
                    // Log error here since request failed
                    Toast.makeText(getApplicationContext(), "Oopss!! Couldn't get the product detail. Kindly recheck your entry.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, t.toString());
                }
            });
        }
    }

}
