package com.techassisto.mrinmoy.assisto.retailSales.retailDailyReport;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.techassisto.mrinmoy.assisto.DashBoardActivity;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.utils.ApiClient;
import com.techassisto.mrinmoy.assisto.utils.ApiInterface;
import com.techassisto.mrinmoy.assisto.utils.Constants;
import com.techassisto.mrinmoy.assisto.utils.TenantInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sayantan on 11/2/18.
 */

public class DailySalesReportActivity extends DashBoardActivity {

    private static final String TAG = "Assisto.RetailLanding";

    private Activity mActivity = null;


    private View mMainView = null;
    private View mProgressView = null;
    private View mCurrentView = null;
    EditText date;
    DatePickerDialog datePickerDialog;
    ArrayList<DailySalesPayment> mDailySalesPaymentList = new ArrayList<>();
    private ListView mListView = null;
    private LinearLayout dailyReportView;
    private LinearLayout dailyPaymentModeReport;

    DailyPaymentReportAdapter mDailyPaymentReportAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_retailsales_landing);

        mActivity = this;

        mMainView = findViewById(R.id.create_invoice_layout);
        mProgressView = findViewById(R.id.apiget_progress);

        mListView = (ListView) findViewById(R.id.daily_payment_list_view);
        dailyReportView = (LinearLayout) findViewById(R.id.daily_report_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();


        // Fetch Tenant Details
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String tenant = userPref.getString(Constants.UserPref.SP_TENANT, null);
        if (tenant != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            TenantInfo tenantInfo = gson.fromJson(tenant, TenantInfo.class);
            Log.i(TAG, "Tenant:" + tenantInfo.tenant_name + " First Name:" + tenantInfo.first_name);
        }
//        Button viewInvoiceBtn = (Button) findViewById(R.id.viewInvoiceBtn);
//        viewInvoiceBtn .setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(RetailSalesLanding.this, InvoiceActivity.class);
//                startActivity(intent);
//            }
//        });

        date = (EditText) findViewById(R.id.date);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(DailySalesReportActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                date.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        Button getDailyPaymentReport = (Button) findViewById(R.id.getDailyPaymentReport);

        getDailyPaymentReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    EditText dateView = (EditText) findViewById(R.id.date);
                    String dateString = dateView.getText().toString();
                    Date date_DateFormat = null;
                    try {
                        date_DateFormat = new SimpleDateFormat("dd/MM/yyyy").parse(dateString);
                    } catch (ParseException e) {
                        Log.i(TAG, "Parse exception: "+e);
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String date = dateFormat.format(date_DateFormat);
                        getDailySalesPaymentReport(date);
                }catch (Exception e){
                    Log.i(TAG, "Date exception: "+e);
                    Toast.makeText(getApplicationContext(), "Oops!! There were some problems. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public int getLayoutResId() {
        return R.layout.activity_retail_daily_sales_report;
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

            mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
            mMainView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void getDailySalesPaymentReport(String date) {

//        showProgress(true);

        Log.i(TAG, "get Downloads...");

        //Get the auth token
        SharedPreferences userPref = getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken != null) {
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);
            String authorization = "jwt "+authToken;
            String calltype = "daily_payment_report";
            Call<List<DailySalesPayment>> call = apiService.geRetailDailySalesReport(calltype, date, authorization);
            call.enqueue(new Callback<List<DailySalesPayment>>() {
                @Override
                public void onResponse(Call<List<DailySalesPayment>>call, Response<List<DailySalesPayment>> response) {
//                    mPaymentModeSpnr = (Spinner) findViewById(R.id.paymentMode_spinner);
//                    List<DailySalesPayment> mDailySalesPaymentList = new ArrayList<>();
                    dailyReportView = (LinearLayout) findViewById(R.id.daily_report_view);
                    dailyPaymentModeReport = (LinearLayout) findViewById(R.id.daily_payment_mode_report);
                    dailyReportView.setVisibility(View.GONE);
                    dailyPaymentModeReport.setVisibility(View.VISIBLE);
//                    showProgress(false);
                    for (int i=0; i<response.body().size(); i++) {
                        mDailySalesPaymentList.add(response.body().get(i));
                        Log.i(TAG, "Daily Sales Report Name:" + response.body().get(i).getPaymentModeName());
                    }

                    mDailyPaymentReportAdapter = new DailyPaymentReportAdapter(mActivity, mDailySalesPaymentList);
                    mListView.setAdapter(mDailyPaymentReportAdapter);
//                    PaymentModeAdapter dataAdapter = new PaymentModeAdapter(mActivity,android.R.layout.simple_spinner_item, mDailySalesPaymentList);
//                    mPaymentModeSpnr.setAdapter(dataAdapter);
                }

                @Override
                public void onFailure(Call<List<DailySalesPayment>>call, Throwable t) {
                    // Log error here since request failed
                    Log.e(TAG, t.toString());
//                    showProgress(false);
                    Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
//            showProgress(false);
            Toast.makeText(getApplicationContext(), "Oops!! Something went wrong. Try Again", Toast.LENGTH_SHORT).show();

            // REDIRECT TO LOGIN PAGE
        }
    }
}
