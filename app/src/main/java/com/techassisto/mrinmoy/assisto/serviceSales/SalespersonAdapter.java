package com.techassisto.mrinmoy.assisto.serviceSales;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.utilDeclaration.UserListInfo;

import java.util.List;

/**
 * Created by sayantan on 6/12/17.
 */

public class SalespersonAdapter extends ArrayAdapter<UserListInfo> {

    LayoutInflater inflater;

    public SalespersonAdapter(Activity context, int resouceId, List<UserListInfo> list){
        super(context, resouceId, list);
        inflater = context.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        UserListInfo rowItem = getItem(position);
        View rowView = inflater.inflate(android.R.layout.simple_spinner_item,null,true);

        TextView nameView = (TextView) rowView.findViewById(android.R.id.text1);
        nameView.setText(rowItem.first_name+" "+rowItem.last_name);

        return rowView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = inflater.inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        UserListInfo rowItem = getItem(position);

        TextView name = (TextView) convertView.findViewById(android.R.id.text1);
        name.setText(rowItem.first_name+" "+rowItem.last_name);

        return convertView;
    }

}
