package com.techassisto.mrinmoy.assisto.retailSales.retailInvoiceDetails;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.techassisto.mrinmoy.assisto.R;

import java.util.List;

/**
 * Created by sayantan on 15/8/17.
 */

public class RetailInvoiceAdapter extends ArrayAdapter <RetailInvoiceLineDetails> {

    private  static final String TAG = "Assisto.InvoiceDetailsAdapter";
    private List<RetailInvoiceLineDetails> list;

    private final Activity context;

    public RetailInvoiceAdapter(Activity context, List<RetailInvoiceLineDetails> list){
        super(context, R.layout.invoice_detail_row, list);
//       Change layout. We need a separate layout for this view
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        //        protected TextView id;
        protected TextView product_name;
        protected TextView product_rate;
        protected TextView product_quantity;
        protected TextView line_cgst;
        protected TextView line_sgst;
        protected TextView line_total;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.invoice_detail_row, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.product_name= (TextView) view.findViewById(R.id.name);
            viewHolder.product_rate = (TextView) view.findViewById(R.id.rate);
            viewHolder.product_quantity = (TextView) view.findViewById(R.id.quantity);
            viewHolder.line_cgst = (TextView) view.findViewById(R.id.cgst);
            viewHolder.line_sgst = (TextView) view.findViewById(R.id.sgst);
            viewHolder.line_total = (TextView) view.findViewById(R.id.total);
            view.setTag(viewHolder);
        }else{
            view=convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();



        holder.product_name.setText(list.get(position).getProduct_name());
        holder.product_rate.setText(list.get(position).getSales_price());
        holder.product_quantity.setText(list.get(position).getQuantity());
        holder.line_cgst.setText(list.get(position).getCgst_value());
        holder.line_sgst.setText(list.get(position).getSgst_value());
        holder.line_total.setText(list.get(position).getLine_total());

        return view;
    }
}

