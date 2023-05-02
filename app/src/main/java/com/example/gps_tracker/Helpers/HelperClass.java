package com.example.gps_tracker.Helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class HelperClass {
    public static final String TAG_LOG = "TAG_FOR_LOG";
    public static void makeToast(Context context, String message, int length) {
        Toast.makeText(context, message, length).show();
    }
    public static void logString(String message) {
        Log.d(TAG_LOG, message);
    }
}
