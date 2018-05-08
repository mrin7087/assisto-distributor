package com.techassisto.mrinmoy.assisto.retailSales.retailNewInvoice;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.techassisto.mrinmoy.assisto.utilDeclaration.WarehouseInfo;

import java.util.List;

/**
 * Created by Mrinmoy on 7/27/2017.
 */

public class WarehouseAdapter extends ArrayAdapter<WarehouseInfo>{
    LayoutInflater inflater;

    public WarehouseAdapter(Activity context, int resouceId, List<WarehouseInfo> list){

        super(context, resouceId, list);
        inflater = context.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        WarehouseInfo rowItem = getItem(position);

        View rowView = inflater.inflate(android.R.layout.simple_spinner_item,null,true);

        TextView nameView = (TextView) rowView.findViewById(android.R.id.text1);
        nameView.setText(rowItem.name);

        return rowView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        WarehouseInfo rowItem = getItem(position);
        TextView name = (TextView) convertView.findViewById(android.R.id.text1);
        name.setText(rowItem.name);

        return convertView;
    }
}
