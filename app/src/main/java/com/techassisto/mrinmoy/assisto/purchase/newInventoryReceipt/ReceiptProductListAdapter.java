package com.techassisto.mrinmoy.assisto.purchase.newInventoryReceipt;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.techassisto.mrinmoy.assisto.R;

import java.util.List;

/**
 * Created by sayantan on 25/10/17.
 */

public class ReceiptProductListAdapter extends ArrayAdapter<ReceiptProductListModel> {
    public interface OnItemDeletedListener {
        void onItemDeleted();
    }

    private static final String TAG = "Assisto.ReceiptAdapter";

    private List<ReceiptProductListModel> list;
    private final Activity context;
    private OnItemDeletedListener mDeleteListener;

    public ReceiptProductListAdapter(Activity context, List<ReceiptProductListModel> list) {
        super(context, R.layout.invoicelistrow, list);
        this.context = context;
        this.list = list;
    }

    public void setOnItemDeletedListener(OnItemDeletedListener listener){
        mDeleteListener = listener;
    }

    static class ViewHolder {
        protected TextView name;
        protected TextView quantity;
        protected TextView price;
        protected Button delete;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "Sayantan.. adapter getview() for" + position);
        View view = null;
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.invoicelistrow, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.product_name);
            viewHolder.quantity = (TextView) view.findViewById(R.id.product_quantity);
            viewHolder.price = (TextView) view.findViewById(R.id.product_price);

            viewHolder.delete = (Button) view.findViewById(R.id.product_delete);
            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Integer index = (Integer) v.getTag();
                    list.remove(position);
                    notifyDataSetChanged();
                    if (mDeleteListener != null) {
                        mDeleteListener.onItemDeleted();
                    } else {
                        Log.e(TAG, "OnItemDeletedListener is null!!");
                    }
                }
            });

            view.setTag(viewHolder);
        } else {
            view = convertView;
            //((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
        }
        ViewHolder holder = (ViewHolder) view.getTag();

        Log.i(TAG, "Vendor Pur name:" + list.get(position).getName());
        Log.i(TAG, "Vendor Pur Price:" + list.get(position).getPurchasePrice());

        holder.name.setText(list.get(position).getName());
        holder.quantity.setText(String.valueOf(list.get(position).getQuantity()));
        holder.price.setText(String.valueOf(list.get(position).getPurchasePrice()));

        return view;
    }
}
