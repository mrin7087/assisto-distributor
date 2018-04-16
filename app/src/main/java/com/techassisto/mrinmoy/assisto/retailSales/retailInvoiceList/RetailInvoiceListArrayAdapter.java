package com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.techassisto.mrinmoy.assisto.R;

import java.util.List;

/**
 * Created by sayantan on 11/8/17.
 */

public class RetailInvoiceListArrayAdapter extends ArrayAdapter <LatestSalesInvoices> {

    private static final String TAG = "Assisto.InvoiceListAdapter";
    private List <LatestSalesInvoices> list;
    private final Activity context;

    public RetailInvoiceListArrayAdapter(Activity context, List<LatestSalesInvoices> list) {
        super(context, R.layout.retailinvoicelistrow, list);
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
//        protected TextView id;
        protected TextView invoice_id;
        protected TextView date;
        protected TextView cgsttotal;
        protected TextView sgsttotal;
        protected TextView total;
        protected TextView payment_name;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.retailinvoicelistrow, null);
            final ViewHolder viewHolder = new ViewHolder();
//            viewHolder.id= (TextView) view.findViewById(R.id.id);
            viewHolder.invoice_id = (TextView) view.findViewById(R.id.invoice_id);
            viewHolder.date = (TextView) view.findViewById(R.id.date);
            viewHolder.cgsttotal = (TextView) view.findViewById(R.id.cgsttotal);
            viewHolder.sgsttotal = (TextView) view.findViewById(R.id.sgsttotal);
            viewHolder.total = (TextView) view.findViewById(R.id.total);
            viewHolder.payment_name = (TextView) view.findViewById(R.id.payment_name);
            view.setTag(viewHolder);
        }else{
            view=convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();

//        holder.id.setText(list.get(position).getId());
        holder.invoice_id.setText(list.get(position).getInvoice_id());
        holder.date.setText(list.get(position).getDate());
        holder.cgsttotal.setText(list.get(position).getCgsttotal());
        holder.sgsttotal.setText(list.get(position).getSgsttotal());
        holder.total.setText(list.get(position).getTotal());
        holder.payment_name.setText(list.get(position).getPayment_name());

        return view;

    }
}
