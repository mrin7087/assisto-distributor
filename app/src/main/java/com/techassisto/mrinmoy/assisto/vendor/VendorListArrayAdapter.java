package com.techassisto.mrinmoy.assisto.vendor;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.techassisto.mrinmoy.assisto.R;

import java.util.List;

/**
 * Created by Mrinmoy on 6/8/2017.
 */

public class VendorListArrayAdapter extends ArrayAdapter<VendorListModel> {
    private static final String TAG = "Assisto.VendorAdapter";

    private List<VendorListModel> list;
    private final Activity context;

    public VendorListArrayAdapter(Activity context, List<VendorListModel> list) {
        super(context, R.layout.vendorlistrow, list);
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        protected TextView name;
        protected TextView key;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "Mrinmoy.. adapter getview() for" + position);
        View view = null;
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.vendorlistrow, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.name);
            viewHolder.key = (TextView) view.findViewById(R.id.key);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            //((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
        }
        ViewHolder holder = (ViewHolder) view.getTag();

        Log.i(TAG, "Mrinmoy.. name:" + list.get(position).getName());
        Log.i(TAG, "Mrinmoy.. key:" + list.get(position).getKey());

        holder.name.setText(list.get(position).getName());
        holder.key.setText(list.get(position).getKey());
        return view;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        Log.i(TAG, "Mrinmoy.. adapter getview() for" + position);
//        // Get the data item for this position
//        VendorListModel vendor = getItem(position);
//        // Check if an existing view is being reused, otherwise inflate the view
//        ViewHolder viewHolder; // view lookup cache stored in tag
//        if (convertView == null) {
//            // If there's no view to re-use, inflate a brand new view for row
//            viewHolder = new ViewHolder();
//            LayoutInflater inflater = LayoutInflater.from(getContext());
//            convertView = inflater.inflate(R.layout.vendorlistrow, parent, false);
//            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
//            viewHolder.key = (TextView) convertView.findViewById(R.id.key);
//            // Cache the viewHolder object inside the fresh view
//            convertView.setTag(viewHolder);
//        } else {
//            // View is being recycled, retrieve the viewHolder object from tag
//            viewHolder = (ViewHolder) convertView.getTag();
//        }
//        // Populate the data from the data object via the viewHolder object
//        // into the template view.
//        viewHolder.name.setText(vendor.getName());
//        viewHolder.key.setText(vendor.getKey());
//        // Return the completed view to render on screen
//        return convertView;
//    }

}
