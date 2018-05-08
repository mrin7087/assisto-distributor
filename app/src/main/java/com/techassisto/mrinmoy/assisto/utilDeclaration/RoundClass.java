package com.techassisto.mrinmoy.assisto.utilDeclaration;

import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by sayantan on 6/9/17.
 */

public class RoundClass {
    public static double round(double value, int places) {
        final String TAG = "Assisto.RoundValue";
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        Log.i(TAG, "Value after multiplying: "+value);
        long tmp = Math.round(value);
        Log.i(TAG, "Temp Value: "+tmp);
        double this_val = ((double)tmp /(double)factor);
        Log.i(TAG, "Value after round: "+this_val);
        return this_val;
    }
}
