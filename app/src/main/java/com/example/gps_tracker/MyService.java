package com.example.gps_tracker;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.provider.Settings.Secure;

import com.example.gps_tracker.Helpers.HelperClass;
import com.example.gps_tracker.Helpers.WebSendHelper;

import java.util.Arrays;

import org.json.JSONArray;

public class MyService extends Service {
    private LocationManager locationManager;
    private static final int TAG_CODE_PERMISSION_LOCATION = 444;
    public String lastCoord = "";
    private Context context;
    private SQLiteConnector connector;
    private SQLiteDatabase db;
    private SimpleCursorAdapter adaptesender_idr;
    private Cursor result;
    WebSendHelper _webSendHelper;
    public static MyService instance = null;

    public MyService() {
        _webSendHelper = new WebSendHelper();
        Context context = this;
    }

    private Double flLatReceived, flLonReceived, flLatSaved, flLonSaved;
    private ThreadServiceChecker threadServiceChecker;
    private static final int TAG_CODE_PERMISSION_INTERNET = 6567;
    private String android_id;
    private String user_login;

    String urlToSendData = "https://maxbarannyk.ru/saveDataQwu.php";
    String dataToSend = "emptyString";
    String res = "EMPTY";
    String user = "maxrexfax";
    private RequestSenderHelper requestSenderHelper;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public void onCreate() {
        HelperClass.logString("Service onCreate worked");
        android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
        user_login = "max_admin";
        Toast.makeText(this, "Tracking started!!", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 1, locationListener);
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 1, 1, locationListener);
            checkEnabled();
        } else {
            Toast.makeText(this, "Tracking stops - GPS is OFF!", Toast.LENGTH_SHORT).show();
        }
        connector = new SQLiteConnector(this, "dbCoordinates", 1);
        db = connector.getWritableDatabase();
        instance = this;
        flLatSaved = 0.0;
        flLonSaved = 0.0;
        threadServiceChecker = new ThreadServiceChecker(locationManager, this);
        threadServiceChecker.setDaemon(true);
        threadServiceChecker.start();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //HelperClass.logString("Service onStartCommand.worked");
        //return super.onStartCommand(intent, flags, startId);
        //return START_STICKY;
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        //HelperClass.logString("Service onDestroy worked");
        //Toast.makeText(this, "Service onDestroy!!", Toast.LENGTH_SHORT).show();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }
        instance = null;
        super.onDestroy();
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED) {
                    showLocation(locationManager.getLastKnownLocation(provider));
                } else {
                    //onDestroy();
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                //tvStatusGPS.setText("Status: " + String.valueOf(status));
            } /*else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tvStatusNet.setText("Status: " + String.valueOf(status));
            }*/
        }
    };

    private void showLocation(Location location) {
        //HelperClass.logString("Service showLocation worked");
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            lastCoord = formatLocation(location);

        }
    }

    private String formatLocation(Location location) {
        //HelperClass.logString("Service formatLocation worked");
        if (location == null)
            return "";
        String latTmp = "", lonTmp = "";

        latTmp = String.format("%1$.6f", location.getLatitude()).replace(',', '.');
        lonTmp = String.format("%1$.6f", location.getLongitude()).replace(',', '.');
        flLatReceived = Double.parseDouble(latTmp);
        flLonReceived = Double.parseDouble(lonTmp);

        ContentValues cv = new ContentValues();//хранилище
        cv.put("_dateDayTime", getDate());
        //cv.put("_dateGps", String.valueOf(new Date(location.getTime())));

        cv.put("_latitude", latTmp);
        cv.put("_longitude", lonTmp);


        //cv.put("_dlatitude",flLatReceived);
        //cv.put("_dlongitude",flLatReceived);
        //flLatReceived, flLonReceived, flLatSaved, flLonSaved
        cv.put("_delivered", 0);
        long rowId = db.insert("Coordinates", null, cv);
        if (rowId != -1)
            HelperClass.logString("service insertion complete. row ID " + rowId);
        else
            HelperClass.logString("service insertion error " + rowId);



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) ==
                PackageManager.PERMISSION_GRANTED) {
            //HelperClass.logString("INTERNET PERMISSION_GRANTED");
            Map<String, String> map = new HashMap<String, String>();
            map.put("android_id", android_id);
            map.put("user_login", user_login);
            map.put("lat", latTmp);
            map.put("lon", lonTmp);
            map.put("date", getDate());
            JSONArray jsonArray = new JSONArray(Arrays.asList(map));
            HelperClass.logString("Service jsonArray=" + jsonArray);
            //testSend(android_id + "_" + latTmp+"_"+lonTmp+"_"+getDate());


            String urlParameters = "lat=" + latTmp
                    + "&long=" + lonTmp
                    + "&time=" + getDate()
                    + "&" + _webSendHelper.SECRET_KEY + "=" + _webSendHelper.SECRET
                    + "&android_id=" + android_id;
            _webSendHelper.sendGpsData(urlParameters);
//            requestSenderHelper = new RequestSenderHelper(this, urlToSendData, urlParameters);
//            requestSenderHelper.sendDataToServerByGet();
            //testSend(jsonArray.toString());
        } else {
           HelperClass.logString("Service - INTERNET DENY");
        }
        return "";
    }



    private void checkEnabled() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

        } else {
            Toast.makeText(this, "Enable GPS or stop tracking!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickLocationSettings(View view) {
        startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    private String getDate() {
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return dateFormat.format(currentDate);
    }
}
