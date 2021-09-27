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
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.provider.Settings.Secure;
import java.util.Arrays;
import org.json.JSONArray;

public class MyService extends Service {
    private LocationManager locationManager;
    private static final int TAG_CODE_PERMISSION_LOCATION = 444;
    public String lastCoord = "";
    private Context context;
    private SQLiteConnector connector;
    private SQLiteDatabase db;
    private SimpleCursorAdapter adapter;
    private Cursor result;
    public static MyService instance = null;
    public MyService() {
        Context context = this;
    }
    private Double flLatReceived, flLonReceived, flLatSaved, flLonSaved;
    private ThreadServiceChecker threadServiceChecker;
    private static final int TAG_CODE_PERMISSION_INTERNET = 6567;
    private String android_id;
    private String user_login;

    String sendUrl1 = "http://maxbarannyk.ru/gps-serv/func.php?command=insertcoord";
    String sendUrlData = "http://maxbarannyk.ru/saveDataQwu.php";
    String sendUrlPoint = "http://maxbarannyk.ru/savePointQwu.php";
    String dataToSend = "emptyString";
    String res = "EMPTY";
    String user = "maxrexfax";
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }
    @Override
    public void onCreate(){
        Log.d("TAG1", "Service onCreate worked");
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
        //Log.d("TAG1", "Service onStartCommand.worked");
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //Log.d("TAG1", "Service onDestroy worked");
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
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
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
        //Log.d("TAG1", "Service showLocation worked");
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            lastCoord = formatLocation(location);

        }
    }

    private String formatLocation(Location location) {
        //Log.d("TAG1", "Service formatLocation worked");
        if (location == null)
            return "";
        String latTmp = "", lonTmp = "";

        latTmp = String.format("%1$.6f", location.getLatitude()).replace(',','.');
        lonTmp = String.format("%1$.6f", location.getLongitude()).replace(',','.');
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
        if(rowId!=-1)
            Log.d("TAG1", "service insertion complete. row ID " + rowId );
        else
            Log.d("TAG1", "service insertion error " + rowId );
        /*return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));*/


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)==
                PackageManager.PERMISSION_GRANTED)
        {
            Log.d("TAG1", "INTERNET PERMISSION_GRANTED");
            Map<String, String> map = new HashMap<String, String>();
            map.put("android_id", android_id);
            map.put("user_login", user_login);
            map.put("lat", latTmp);
            map.put("lon", lonTmp);
            map.put("date", getDate());
            JSONArray jsonArray = new JSONArray(Arrays.asList(map));
            Log.d("TAG1", "Service jsonArray=" + jsonArray);
            //testSend(android_id + "_" + latTmp+"_"+lonTmp+"_"+getDate());
            testSend(jsonArray.toString());
        }
        else{
            Log.d("TAG1", "Service - INTERNET DENY");
        }
        return "";
    }
    private void testSend(String dataSend){
        user = "maxrexfax";
        Log.d("TAG1", "private void test2 START");
        final String finalUrl = sendUrl1+"&user="+user+ "&data="+dataSend;
        Log.d("TAG1", "finalUrl===" + finalUrl);
        Thread background = new Thread(new Runnable() {
            // After call for background.start this run method call
            public void run() {
                try {
                    try {
                        Log.d("TAG1", "test2  TRY 0001");
                        URL url = new URL(finalUrl);
                        Log.d("TAG1", "test2  TRY 0002");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        Log.d("TAG", "test2  TRY 0003");
                        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            Log.d("TAG1", "TRY 0004  HttpURLConnection.HTTP_OK");
                            //Если запрос выполнен удачно, читаем полученные данные и далее, делаем что-то
                            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            res = in.readLine();
                            in.close();
                            Log.d("TAG1", "TRY 0005  IF HTTP_OK res=" + res);
                            //textView02.setText("Result: " + res);
                            //String res = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
                        } else {
                            Log.d("TAG1", "TRY 0006 con.getResponseCode()=" + con.getResponseCode());
                            //Если запрос выполнен не удачно, делаем что-то другое
                            res = "FAILURE!!";
                            //textView02.setText("Result: " + res);
                            Log.d("TAG1", "test2  007 else HTTP_FAIL res=" + res);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("TAG1", "008 private String getDataFromUrl catch IOException=" + e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("TAG1", "009 private String getDataFromUrl catch Exception=" + e.getMessage() + " " + e.getLocalizedMessage());
                       }
                    Log.d("TAG1", "010 private String getDataFromUrl return result=" + res);
                    //textView02.setText(res);
                } catch (Throwable t) {
                    // just end the background thread
                    Log.d("TAG1", "public void run Thread  exception " + t);                       }
            }



        });
        // Start Thread
        background.start();
    }

    private void checkEnabled() {
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

        }else{
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
