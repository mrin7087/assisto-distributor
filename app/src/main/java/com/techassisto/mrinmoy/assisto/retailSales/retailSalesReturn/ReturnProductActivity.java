package com.techassisto.mrinmoy.assisto.retailSales.retailSalesReturn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.techassisto.mrinmoy.assisto.R;

/**
 * Created by sayantan on 13/2/18.
 */

public class ReturnProductActivity extends AppCompatActivity {

    private static final String TAG = "Assisto.ReturnProduct";
    private static final int SCAN_PRODUCT_REQUEST = 1;

    private Activity mActivity = null;
    private View mProgressView = null;
    private View mAddProductFormView = null;
    private Button mSubmitBtn = null;
    private EditText pReturnQty = null;
    private TextView pProductName = null;
    String productName;
    float originalQty;
    float returnQty;
    String position;

//    private ProductInfo mProduct;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        Intent intent = this.getIntent();
        productName=intent.getStringExtra("Product Name");
        originalQty=Float.parseFloat(intent.getStringExtra("Original Qty"));
        position=intent.getStringExtra("Line Position");
        Log.i(TAG, "Position: "+position);
        try {
            returnQty = Float.parseFloat(intent.getStringExtra("Return Qty"));
        }catch (Exception e){
            returnQty = 0;
            Log.i(TAG, "Exception is: "+ e);
        }
        pProductName = (TextView) findViewById(R.id.product_name);
        EditText productQty = (EditText) findViewById(R.id.product_quantity);
        productQty.setVisibility(View.GONE);
        pReturnQty = (EditText) findViewById(R.id.return_quantity);
        pReturnQty.setVisibility(View.VISIBLE);

        pProductName.setText(productName);
        pReturnQty.setText(String.valueOf(returnQty));
    }

    public void submitProduct(View v) {

        EditText pQuantityView = (EditText) findViewById(R.id.return_quantity);
        String quantity = pQuantityView.getText().toString();
        float return_qty = 0;
        try {
            return_qty = Float.parseFloat(quantity);
        }catch (Exception e){
            quantity = String.valueOf(0);
        }
        if (return_qty > originalQty){
            Toast.makeText(getApplicationContext(), "Return Quantity cannot be greater than original sales quantity.", Toast.LENGTH_LONG).show();
        }
        else if(return_qty == 0){
            Toast.makeText(getApplicationContext(), "Return Quantity cannot be zero. To return, please press back button.", Toast.LENGTH_LONG).show();
        }
        else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("Return Quantity", quantity);
            returnIntent.putExtra("Line Position", position);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }
}
