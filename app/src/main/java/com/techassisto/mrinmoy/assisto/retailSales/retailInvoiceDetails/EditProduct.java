package com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceDetails;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.FloatProperty;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;
import com.techassisto.mrinmoy.assisto.R;

/**
 * Created by sayantan on 14/11/17.
 */

public class EditProduct extends AppCompatActivity {
    private static final String TAG = "Assisto.EditProduct";
    private static final int SCAN_PRODUCT_REQUEST = 1;

    private Activity mActivity = null;
    private View mProgressView = null;
    private View mAddProductFormView = null;
    private Button mSubmitBtn = null;
    private EditText pRevisedQty = null;
    private TextView pProductName = null;
    String productName;
    float originalQty;
    float revisedQty;
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
            revisedQty = Float.parseFloat(intent.getStringExtra("Revised Qty"));
        }catch (Exception e){
            revisedQty = 0;
            Log.i(TAG, "Exception is: "+ e);
        }
        pProductName = (TextView) findViewById(R.id.product_name);
        pRevisedQty = (EditText) findViewById(R.id.product_quantity);

        pProductName.setText(productName);
        pRevisedQty.setText(String.valueOf(revisedQty));


    }

    public void submitProduct(View v) {

        EditText pQuantityView = (EditText) findViewById(R.id.product_quantity);
        String quantity = pQuantityView.getText().toString();
        float revised_qty = 0;
        try {
            revised_qty = Float.parseFloat(quantity);
        }catch (Exception e){
            quantity = String.valueOf(0);
        }
        if (revised_qty > originalQty){
            Toast.makeText(getApplicationContext(), "For additional quantity, please generate new invoice", Toast.LENGTH_LONG).show();
        }
        else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("Revised Qty", quantity);
            returnIntent.putExtra("Line Position", position);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

}
