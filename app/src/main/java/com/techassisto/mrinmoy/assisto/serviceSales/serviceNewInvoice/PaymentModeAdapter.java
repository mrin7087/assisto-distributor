package com.techassisto.mrinmoy.assisto.serviceSales.serviceNewInvoice;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.techassisto.mrinmoy.assisto.utilDeclaration.PaymentModeInfo;
import com.techassisto.mrinmoy.assisto.R;

import java.util.List;

/**
 * Created by sayantan on 4/12/17.
 */

public class PaymentModeAdapter extends ArrayAdapter<PaymentModeInfo> {

    LayoutInflater inflater;

    public PaymentModeAdapter(Activity context, int resouceId, List<PaymentModeInfo> list){
        super(context, resouceId, list);
        inflater = context.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        PaymentModeInfo rowItem = getItem(position);
        View rowView = inflater.inflate(android.R.layout.simple_spinner_item,null,true);

        TextView nameView = (TextView) rowView.findViewById(android.R.id.text1);
        nameView.setText(rowItem.name);

        return rowView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = inflater.inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        PaymentModeInfo rowItem = getItem(position);

        TextView name = (TextView) convertView.findViewById(android.R.id.text1);
        name.setText(rowItem.name);

        return convertView;
    }
}
