package com.techassisto.mrinmoy.assisto.purchase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.techassisto.mrinmoy.assisto.R;
import com.techassisto.mrinmoy.assisto.utils.APIs;
import com.techassisto.mrinmoy.assisto.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by sayantan on 28/10/17.
 */

public class VendorAutoCompleteAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = "Assisto.VendorAdapter";
    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List<Vendor> resultList = new ArrayList<Vendor>();

    public VendorAutoCompleteAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Vendor getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.simple_dropdown_item_2line, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.product_label)).setText(getItem(position).getLabel());
//        ((TextView) convertView.findViewById(R.id.product_id)).setText(getItem(position).getId());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<Vendor> vendors = getVendors(mContext, constraint.toString());

                    if(vendors == null) {
                        return null;
                    }

                    // Assign the data to the FilterResults
                    filterResults.values = vendors;
                    filterResults.count = vendors.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List<Vendor>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    public class Vendor {
        public String label;
        public int id;

        public String getLabel() {
            return label;
        }

        public String getId() {
            return Integer.toString(id);
        }
    }

    /**
     * Returns a search result for the given product pattern;
     */
    private List<Vendor> getVendors(Context context, String vendPattern) {
        Log.i(TAG, "getVendors, term:" + vendPattern);

        if (vendPattern == null) {
            Log.i(TAG, "Ignore null request..");
            return null;
        }

        if (vendPattern.length() < 3) {
            Log.i(TAG, "Ignore request of length:" + vendPattern.length());
            return null;
        }

        //Get the auth token
        SharedPreferences userPref = mContext.getSharedPreferences(Constants.UserPref.SP_NAME, MODE_PRIVATE);
        String authToken = userPref.getString(Constants.UserPref.SP_UTOKEN, null);
        if (authToken == null) {
            Log.e(TAG, "Failed to get auth token. return..");
            return null;
        }

        //Compose the get Request
        String targetURL = Constants.SERVER_ADDR + APIs.vendor_autocomplete_get;

        StringBuffer response = new StringBuffer();

        Log.i(TAG, "try to POST HTTP request");
        HttpURLConnection httpConnection = null;
        try {
            targetURL += ("?term=");
            String query = URLEncoder.encode(vendPattern, "utf-8");
            targetURL += (query);
            URL targetUrl = new URL(targetURL);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Authorization", "jwt " + authToken);
            httpConnection.setConnectTimeout(10000); //10secs
            httpConnection.connect();

            Log.i(TAG, "response code:" + httpConnection.getResponseCode());
            if (httpConnection.getResponseCode() != 200) {
                Log.e(TAG, "Failed : HTTP error code : " + httpConnection.getResponseCode());
                return null;
            }

            //Received Response
            InputStream is = httpConnection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();

            Log.i(TAG, response.toString());
            // Save the product details
            return parseVendorList(response.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "Malformed URL, err:" + Constants.Status.ERR_NETWORK);
            return null;
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            Log.e(TAG, "Socket timeout, err:" + Constants.Status.ERR_NETWORK);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IO Error, err:" + Constants.Status.ERR_UNKNOWN);
            return null;
        } finally {

            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    private List<Vendor> parseVendorList(String response) {
        Log.i(TAG, "parseVendorList..");
        List<Vendor> vendList = new ArrayList<Vendor>();
        try {
            JSONArray jsonVendArr = new JSONArray(response);
            if (jsonVendArr.length() == 0) {
                Log.i(TAG, "No matching vendor found!!");
                return null;
            }

            for (int i = 0; i < jsonVendArr.length(); i++) {
                JSONObject jsonVend = jsonVendArr.getJSONObject(i);
                // Serialize and add to list
                Gson gson = new GsonBuilder().serializeNulls().create();
                Vendor vendor = gson.fromJson(jsonVend.toString(), Vendor.class);
                vendList.add(vendor);
                Log.i(TAG, "Added Vendor to list:" + jsonVend.toString());
            }
            return vendList;
        } catch (Exception ex) {
            Log.e(TAG, "Failed to parse vendor Json array, ex:" + ex.toString());
        }
        return null;
    }
}
