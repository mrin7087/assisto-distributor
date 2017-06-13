package com.techassisto.mrinmoy.assisto.customer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.R;

public class CustomerLanding extends DashBoardActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_customer_landing);
    }

    public int getLayoutResId() {
        return R.layout.activity_customer_landing;
    }
}
