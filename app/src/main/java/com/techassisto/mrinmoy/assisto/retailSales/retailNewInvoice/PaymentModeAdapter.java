package com.techassisto.mrinmoy.assisto.retailSales.retailNewInvoice;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.techassisto.mrinmoy.assisto.PaymentModeInfo;
import com.techassisto.mrinmoy.assisto.PaymentModeOption;
import com.techassisto.mrinmoy.assisto.R;

import java.util.List;

/**
 * Created by sayantan on 8/9/17.
 */


public class PaymentModeAdapter extends ArrayAdapter<PaymentModeOption> {

    LayoutInflater inflater;

    public PaymentModeAdapter(Activity context, int resouceId, List<PaymentModeOption> list){
        super(context, resouceId, list);
        inflater = context.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        PaymentModeOption rowItem = getItem(position);
        View rowView = inflater.inflate(android.R.layout.simple_spinner_item,null,true);

        TextView nameView = (TextView) rowView.findViewById(android.R.id.text1);
        nameView.setText(rowItem.getName());

        return rowView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = inflater.inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        PaymentModeOption rowItem = getItem(position);

        TextView name = (TextView) convertView.findViewById(android.R.id.text1);
        name.setText(rowItem.getName());

        return convertView;
    }
}
