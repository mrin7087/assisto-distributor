package com.techassisto.mrinmoy.assisto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.techassisto.mrinmoy.assisto.customer.CustomerLanding;
import com.techassisto.mrinmoy.assisto.purchase.PurchaseLanding;
import com.techassisto.mrinmoy.assisto.retailSales.RetailSalesLanding;
import com.techassisto.mrinmoy.assisto.utils.Constants;
import com.techassisto.mrinmoy.assisto.utils.TenantInfo;
import com.techassisto.mrinmoy.assisto.vendor.VendorLanding;

public abstract class DashBoardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Assisto.NavDrawer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_dash_board);
        setContentView(getLayoutResId());
        onCreateDrawer();
    }

    protected void onCreateDrawer(/*Bundle savedInstanceState*/) {
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_dash_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();



        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set some info and actions in NAV Header
        View nav_header = navigationView.getHeaderView(0);
        //Display the username in the Nav Header
//        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
//        String username = userPref.getString(Constants.UserPref.SP_USERNAME, null);
//        if (username != null) {
//            TextView nav_header_profileName = (TextView) nav_header.findViewById(R.id.nav_header_profilename);
//            nav_header_profileName.append(" " + username);
//        }

        TenantInfo tenantInfo = null;
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String tenant = userPref.getString(Constants.UserPref.SP_TENANT, null);
        if (tenant != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            tenantInfo = gson.fromJson(tenant, TenantInfo.class);
//            Log.i(TAG, "Tenant:" + tenantInfo.tenant_name + " First Name:" + tenantInfo.first_name);
            TextView nav_header_profileName = (TextView) nav_header.findViewById(R.id.nav_header_profilename);
            nav_header_profileName.append(" " + tenantInfo.first_name);

            TextView nav_header_tenantName = (TextView) nav_header.findViewById(R.id.nav_header_tenantname);
            nav_header_tenantName.append(" " + tenantInfo.tenant_name);
        }

        //Set the Nav Header image to redirect to Home Activiity
        ImageView imgView = (ImageView) nav_header.findViewById(R.id.nav_header_image);
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dash_board, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.i(TAG, "navigation item selected");
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_customer) {
            //Toast.makeText(this, "Selected Customer", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setClass(DashBoardActivity.this, CustomerLanding.class);
            startActivity(intent);

        } else if (id == R.id.nav_vendor) {
            //Toast.makeText(this, "Selected Vendor", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.setClass(DashBoardActivity.this, VendorLanding.class);
            startActivity(intent);

        } else if (id == R.id.nav_distributor_sales) {
            //Toast.makeText(this, "Selected Logout", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.setClass(DashBoardActivity.this, RetailSalesLanding.class);
            startActivity(intent);

        } else if (id == R.id.nav_purchase) {
            //Toast.makeText(this, "Selected Logout", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.setClass(DashBoardActivity.this, PurchaseLanding.class);
            startActivity(intent);

        } else if (id == R.id.nav_manufacturer) {
            //Toast.makeText(this, "Selected Manufacturer", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_product) {
            //Toast.makeText(this, "Selected Vendor", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            //Toast.makeText(this, "Selected Settings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            //Toast.makeText(this, "Selected Logout", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*
 * Extending activities use this class to supply the
 * id of their layout file. This way you can set the view
 * only once and there is no need to create the drawer twice.
 */
    //@LayoutResId
    public abstract int getLayoutResId();
}
