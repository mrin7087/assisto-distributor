package com.techassisto.mrinmoy.assisto.retailSales.retailSalesReturn;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.retailSales.LineItem;

import java.util.List;

/**
 * Created by sayantan on 11/2/18.
 */

public class RetailInvoiceListAdapter extends ArrayAdapter<LineItem> {

    private  static final String TAG = "Assisto.InvoiceDetailsAdapter";
    private List<LineItem> list;

    private final Activity context;

    public RetailInvoiceListAdapter(Activity context, List<LineItem> list){
        super(context, R.layout.return_invoice_detail_row, list);
//       Change layout. We need a separate layout for this view
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        //        protected TextView id;
        protected TextView product_name;
        protected TextView product_rate;
        protected TextView product_quantity;
        protected TextView line_previous_return;
        protected TextView line_gst;
        protected TextView line_total;
        protected TextView return_qty;
        protected TextView return_total;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.return_invoice_detail_row, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.product_name= (TextView) view.findViewById(R.id.name);
            viewHolder.product_rate = (TextView) view.findViewById(R.id.rate);
            viewHolder.product_quantity = (TextView) view.findViewById(R.id.quantity);
            viewHolder.line_previous_return = (TextView) view.findViewById(R.id.previous_return);
            viewHolder.line_gst = (TextView) view.findViewById(R.id.gst);
            viewHolder.line_total = (TextView) view.findViewById(R.id.total);
            viewHolder.line_total = (TextView) view.findViewById(R.id.total);
            viewHolder.return_qty = (TextView) view.findViewById(R.id.return_quantity);
            viewHolder.return_total = (TextView) view.findViewById(R.id.return_total);
            view.setTag(viewHolder);
        }else{
            view=convertView;
        }


        ViewHolder holder = (ViewHolder) view.getTag();

        double zero_qty = 0.0;

        holder.product_name.setText(list.get(position).getProductName());
        holder.product_rate.setText(list.get(position).getSalesPrice());
//        holder.product_quantity.setText(list.get(position).getQuantity());

        holder.product_quantity.setText(list.get(position).getQuantity());
        holder.line_previous_return.setText(list.get(position).getQuantityReturned());
        Double total_gst_value = Double.parseDouble(list.get(position).getCgstValue())+Double.parseDouble(list.get(position).getSgstValue());
        holder.line_gst.setText(String.valueOf(total_gst_value));
        holder.line_total.setText(list.get(position).getLineTotal());
        try {
            if (Double.parseDouble(list.get(position).getCurrentQuantityReturned()) > zero_qty) {
                holder.return_qty.setText("Returned: " + list.get(position).getCurrentQuantityReturned());
                holder.return_qty.setVisibility(View.VISIBLE);
                holder.return_total.setText("Return Value: " + list.get(position).getReturn_line_total());
                holder.return_total.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){

        }

        return view;
    }
}
