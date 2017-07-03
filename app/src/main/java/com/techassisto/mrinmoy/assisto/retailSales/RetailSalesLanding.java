package com.techassisto.mrinmoy.assisto.retailSales;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.R;

public class RetailSalesLanding extends DashBoardActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_distributor_sales_landing);

        Button createInvoiceBtn = (Button) findViewById(R.id.createInvoiceBtn);
        createInvoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(RetailSalesLanding.this, NewSalesInvoice.class);
                startActivity(intent);
            }
        });
    }

    public int getLayoutResId() {
        return R.layout.activity_distributor_sales_landing;
    }
}
