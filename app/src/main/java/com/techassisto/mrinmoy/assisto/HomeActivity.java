package com.techassisto.mrinmoy.assisto;

import android.os.Bundle;
import android.widget.Button;

public class HomeActivity extends DashBoardActivity {

    private Button mScanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_home);
    }

    public int getLayoutResId() {
        return R.layout.activity_home;
    }
}
