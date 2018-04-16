package com.techassisto.mrinmoy.assisto.retailSales.retailDailyReport;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.techassisto.mrinmoy.assisto.R;

import java.util.List;

/**
 * Created by sayantan on 11/2/18.
 */

public class DailyPaymentReportAdapter extends ArrayAdapter<DailySalesPayment> {

    private  static final String TAG = "Assisto.InvoiceDetailsAdapter";
    private List<DailySalesPayment> list;

    private final Activity context;

    public DailyPaymentReportAdapter(Activity context, List<DailySalesPayment> list){
        super(context, R.layout.dailypaymentlist, list);
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        //        protected TextView id;
        protected TextView payment_id;
        protected TextView payment_name;
        protected TextView payment_amount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.dailypaymentlist, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.payment_id = (TextView) view.findViewById(R.id.payment_id);
            viewHolder.payment_name = (TextView) view.findViewById(R.id.payment_name);
            viewHolder.payment_amount = (TextView) view.findViewById(R.id.payment_amount);
            view.setTag(viewHolder);
        }else{
            view=convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();



        holder.payment_id.setText(list.get(position).getPaymentModeIdString());
        holder.payment_name.setText(list.get(position).getPaymentModeName());
        holder.payment_amount.setText(list.get(position).getValue());

        return view;
    }
}
