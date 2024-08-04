package com.example.gps_tracker.Helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class HelperClass {
    public static final String TAG_LOG = "TAG_FOR_LOG";
    public static final String APP_NAME_UPDATES = "gps_tracker";
    public static void makeToast(Context context, String message, int length) {
        Toast.makeText(context, message, length).show();
    }
    public static void logString(String message) {
        Log.d(TAG_LOG, message);
    }

    public String getDateInString()
    {
        return new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss", Locale.ENGLISH).format(new java.util.Date());
    }
}
