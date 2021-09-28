package com.example.gps_tracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings.Secure;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private static final int TAG_CODE_PERMISSION_LOCATION = 444;
    private static final int TAG_CODE_PERMISSION_STORAGE = 345;
    private static final int TAG_CODE_PERMISSION_INTERNET = 6567;
    TextView tvEnabledGPS, tvTitleServiceStat;
    TextView textViewServiceStatus;
    TextView tvDebug1, tvDebug2, tvDebug3;
    TextView tvLongInMain, tvLatInMain, tvTimeInMain;
    private EditText etCommentPoint;
    private boolean isSqlSettingsVisible;
    public String lastCoord = "Not set";
    private LocationManager locationManager;
    private SQLiteConnector connector;
    private SQLiteDatabase db;
    private Cursor result;
    private static final String TEXT_SAVE_COORD = "TEXT_SAVE_COORD";
    private static final String TEXT_SAVE_THREAD_STATE = "TEXT_SAVE_THRST";
    private boolean isThreadStarted = false;
    File docsFolder;
    Double latTmp, lonTmp;
    private static final String BROADCAST_DATA = "broacasting_service";
    private static final String BROADCAST_KEY = "broacasting_key";
    private BroadcastReceiver broadcastReceiver;
    private int serviceStatus;
    private int backColorServiceOn, backColorServiceOff;
    private int textColorServiceOn, textColorServiceOff;
    private ThreadChecker threadChecker;
    private boolean isGpsEnabled;
    String[] latLonTime;
    private int colorTrackingOn, colorTrackingOff;
    public String android_id;

    String sendUrl1 = "http://maxbarannyk.ru/gps-serv/func.php?command=insertcoord";
    String sendUrlData = "http://maxbarannyk.ru/saveDataQwu.php";
    String sendUrlPoint = "http://maxbarannyk.ru/savePointQwu.php";
    String dataToSend = "someString";
    String res = "EMPTY";
    String user = "";
    private String user_login;
    /*В базе и андроид приложении
    Внести коррективы для сохранения идентификатора клиента.

    Продумать КРОН задачу по парсингу уже готовых данных и раскладывание в таблицы с учетом нормализации.

    На сайте для GPS трекера
1 - страница с логином
2 - если залогинен можно войти на страницу с просмотром списка координат
3 - изначально есть select со списком юзеров,
4 - после выбора юзера можно загрузить весь список его точек сохранения или jQuerry датапикером взять диапазон дат.*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
        user_login = "max_admin";
        tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
        textViewServiceStatus = (TextView) findViewById(R.id.tvServiceStatus);
        tvDebug1 = (TextView) findViewById(R.id.tvDebugging1);
        tvDebug2 = (TextView) findViewById(R.id.tvDebugging2);
        tvDebug3 = (TextView) findViewById(R.id.tvDebugging3);

        tvLongInMain = (TextView) findViewById(R.id.tvLongtitudeStatus);
        tvLatInMain = (TextView) findViewById(R.id.tvLatitudeStatus);
        tvTimeInMain = (TextView) findViewById(R.id.tvTimeStatus);
        tvTitleServiceStat = (TextView) findViewById(R.id.tvTitleServiceStatus);
        etCommentPoint = findViewById(R.id.editTextCommentToPoint);
        latLonTime = new String[3];
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                serviceStatus = intent.getIntExtra(BROADCAST_KEY, 0);
                displayServiceStatus(serviceStatus);
            }
        };
        IntentFilter intentFilter = new IntentFilter(BROADCAST_DATA);
        registerReceiver(broadcastReceiver, intentFilter);
        colorTrackingOn = getResources().getColor(R.color.colorAccent);
        colorTrackingOff = getResources().getColor(R.color.colorGrayServiceOff);
        textColorServiceOn = getResources().getColor(R.color.colorGrenTextGPSon);
        textColorServiceOff = getResources().getColor(R.color.colorGrayTextGPSoff);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        isSqlSettingsVisible = false;
        connector = new SQLiteConnector(this, "dbCoordinates", 1);
        db = connector.getWritableDatabase();
        /*connectorForPoints = new SQLiteConnector(this, "CoordPoints", 1);
        dbPoints = connectorForPoints.getWritableDatabase();*/
        if(savedInstanceState!=null){
            lastCoord = (savedInstanceState.getString(TEXT_SAVE_COORD));
            isThreadStarted = (savedInstanceState.getBoolean(TEXT_SAVE_THREAD_STATE));
        }
        //проверка существования и создание папки
        docsFolder = new File(Environment.getExternalStorageDirectory() + "/Download");
        checkAndCreateFolder();
        if(isThreadStarted==false){
            threadChecker = new ThreadChecker(this);
            threadChecker.setDaemon(true);
            threadChecker.start();
            isThreadStarted = true;
        }


        if (isNetwork(getApplicationContext())){

            Toast.makeText(getApplicationContext(), "Internet Connected", Toast.LENGTH_SHORT).show();

        } else {

            Toast.makeText(getApplicationContext(), "Internet Is Not Connected", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayServiceStatus(int serviceStatus) {
        if(serviceStatus==0){
            //textViewServiceStatus.setBackgroundColor(backColorServiceOff);
            textViewServiceStatus.setText(R.string.tv_tracking_off);
            tvTitleServiceStat.setText(R.string.tv_tracking_off_short);
            tvTitleServiceStat.setTextColor(colorTrackingOff);
        }
        else if(serviceStatus==1){
            //textViewServiceStatus.setBackgroundColor(backColorServiceOn);
            textViewServiceStatus.setText(R.string.tv_tracking_on);
            tvTitleServiceStat.setText(R.string.tv_tracking_on_short);
            tvTitleServiceStat.setTextColor(colorTrackingOn);
        }
    }

    //создание постоянной папки для сохранения отчетов
    private void checkAndCreateFolder() {
        boolean isCreated;
        if(isExternalStorageWritable()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
            if (!docsFolder.exists()) {
                Log.d("TAG", "Папка отсутствует, попытка создания");
                isCreated = docsFolder.mkdir();
                if (isCreated) {
                    Log.d("TAG", "Папка создана");
                } else {
                    Log.d("TAG", "Ошибка создания папки");
                }
            } else {
                Log.d("TAG", "Папка существует, создавать не нужно");
            }
        }else {
                ActivityCompat.requestPermissions(this, new String[]{
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        TAG_CODE_PERMISSION_STORAGE);
            }
        }
    }

    @Override
    //вот этот метод нужен для сохранения полей на экране при повороте
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TEXT_SAVE_COORD, lastCoord);
        outState.putBoolean(TEXT_SAVE_THREAD_STATE, isThreadStarted);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 1, 1, locationListener);
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 1, 1, locationListener);
            checkEnabled();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    TAG_CODE_PERMISSION_LOCATION);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }
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
            checkEnabled();
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                showLocation(locationManager.getLastKnownLocation(provider));
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        TAG_CODE_PERMISSION_LOCATION);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
           /* if (provider.equals(LocationManager.GPS_PROVIDER)) {
                tvStatusGPS.setText("Status: " + String.valueOf(status));
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tvStatusNet.setText("Status: " + String.valueOf(status));
            }*/
        }
    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            lastCoord = formatLocation(location);
            latLonTime[0] = String.format("%1$.6f", location.getLatitude()).replace(',','.');
            latTmp = Double.parseDouble(latLonTime[0]);
            tvLatInMain.setText(latLonTime[0]);
            latLonTime[1] = String.format("%1$.6f", location.getLongitude()).replace(',','.');
            lonTmp = Double.parseDouble(latLonTime[1]);
            tvLongInMain.setText(latLonTime[1]);
            latLonTime[2] = getDate();
            tvTimeInMain.setText(getDate());
            tvDebug1.setText(String.valueOf(new Date(location.getTime())));
            //tvLocationGPS.setText(formatLocation(location));
            //tvLocationGPS.setText("Coordinates: lat =" + latTmp +" , lon =" +lonTmp);
        } /*else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)) {
            tvLocationNet.setText(formatLocation(location));
        }*/
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }


    private void checkEnabled() {
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))//GPS сенсор включен
        {
            tvEnabledGPS.setText(R.string.tv_gps_sensor_on);
            tvEnabledGPS.setTextColor(textColorServiceOn);
            isGpsEnabled = true;//флаг, значит включать сервис можно
        }
        else
        {
            tvEnabledGPS.setText(R.string.tv_gps_sensor_off);
            tvEnabledGPS.setTextColor(textColorServiceOff);
            isGpsEnabled = false;//включать сервис нельзя!
        }
       /* tvEnabledGPS.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER));*/
    }


    public void showToast(String strToShow){
        Toast.makeText(this, strToShow, Toast.LENGTH_LONG).show();
    }

    public void onClick(View view){
        switch(view.getId()){
            case R.id.buttonGetCoord:
                if(latLonTime[0] == null){
                    Toast.makeText(this, R.string.tv_state_not_set, Toast.LENGTH_SHORT).show();
                }
                else{
                    //TODO вот тут сохраняем координаты временной точки во вторую таблицу
                    ContentValues cv1 = new ContentValues();//хранилище
                    String dates[] = latLonTime[2].split(" ");
                    cv1.put("_dateDay", dates[0]);
                    cv1.put("_dateTime", dates[1]);
                    cv1.put("_latitude", latLonTime[0]);
                    cv1.put("_longitude", latLonTime[1]);
                    cv1.put("_description", etCommentPoint.getText().toString());


                    //cv.put("_dlatitude",flLatReceived);
                    //cv.put("_dlongitude",flLatReceived);
                    //flLatReceived, flLonReceived, flLatSaved, flLonSaved
                    long rowId = db.insert("CoordPoints", null, cv1);
                    if(rowId!=-1)
                        Log.d("TAG1", "Activity insertion complete. row ID " + rowId );
                    else
                        Log.d("TAG1", "Activity insertion error " + rowId );
                    String urlParameters = "lat=" + latLonTime[0]
                            + "&long=" + latLonTime[1]
                            + "&time=" + getDate()
                            + "&descr=_ANDROID_ " + etCommentPoint.getText().toString()
                            + "&sender_id=" + android_id;

                    Log.d("TAG1", "urlParameters: " +  urlParameters);
                    //sendDataToServerByPost("http://maxbarannyk.ru/api/getip", urlParameters);
                    /*String resultPostSend =*/ sendDataToServerByPost("http://maxbarannyk.ru/savePointQwu.php", urlParameters);
                    //Log.d("TAG1", "Send POST result: " + resultPostSend);

                    //Toast.makeText(this, R.string.toast_point_saved, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(this, "resultPostSend=" + resultPostSend, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnLocationSettings:
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                break;

            case R.id.buttonStartService:
                Intent i = new Intent(this, MyService.class);
                if(isGpsEnabled){//если сенсор не включен, то сервис упадет
                    if(MyService.instance==null){
                        startService(i);
                        displayServiceStatus(1);
                        //isServiceStarted = true;
                    }
                    else{
                        Toast.makeText(this, R.string.toast_service_alr_started, Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(this, R.string.toast_service_alert_gps, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.buttonStopService:
                if(MyService.instance!=null) {
                    stopService(new Intent(this, MyService.class));
                    // isServiceStarted = false;
                }
                else{
                    Toast.makeText(this, R.string.toast_service_alr_stopped, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.buttonOpenDataActivity:
                Intent intent = new Intent(MainActivity.this, DataActivity.class);
                startActivity(intent);
                break;
            case R.id.buttonExportBase:
                File file;
                String str="";
                //TODO - AlertDialog ля выбора папки
                if (docsFolder.exists()) {
                    //Toast.makeText(this, "Папка есть, попытка создания файла", Toast.LENGTH_SHORT).show();
                    file = new File(docsFolder.getAbsolutePath(),"GpsTrackExport_"+getDate()+".csv");
                    try {
                        file.createNewFile();
                        FileWriter f = new FileWriter(file);
                        //f.write(lastCoord + "\r\n");
                        try {
                            result = db.rawQuery("select * from Coordinates", null);
                            while(result.moveToNext()){
                                int locDateIndex = result.getColumnIndex("_dateDay");
                                int locTimeIndex = result.getColumnIndex("_dateTime");
                                int latIndex = result.getColumnIndex("_latitude");
                                int lonIndex = result.getColumnIndex("_longitude");
                                str+=result.getString(locDateIndex)+",";
                                str+=result.getString(locTimeIndex)+",";
                                str+=result.getString(latIndex)+",";
                                str+=result.getString(lonIndex);
                                str+="\r\n";
                                f.write(str);
                                str="";
                            }
                        }
                        catch (Exception e){
                            tvDebug2.setText("Ошибка db.rawQuery: " +  e.getMessage());
                            Log.d("TAG1", "Ошибка db.rawQuery: " +  e.getMessage());
                        }
                        f.flush();
                        f.close();
                        Toast.makeText(this, R.string.toast_mesg_create_file_success, Toast.LENGTH_LONG).show();
                        tvDebug2.setText(R.string.toast_mesg_create_file_success);
                    }catch (Exception fe){
                        Toast.makeText(this, R.string.toast_mesg_error_create_file, Toast.LENGTH_SHORT).show();
                        Log.d("TAG1", "Ошибка записи файла: " +  fe.getMessage());
                        //tvDebug1.setText("Ошибка записи файла: " +  fe.getMessage());
                    }
                }
                else {
                    Toast.makeText(MainActivity.this,  "Ошибка создания файла", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tvTitleServiceStatus:
            case R.id.tvTitleGPS:
                if(MyService.instance!=null){
                    Toast.makeText(MainActivity.this,  "Сервис запущен", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this,  "Сервис остановлен", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.buttonExportPoints:
                File file1;
                String str1="";
                //TODO - AlertDialog ля выбора папки
                if (docsFolder.exists()) {
                    //Toast.makeText(this, "Папка есть, попытка создания файла", Toast.LENGTH_SHORT).show();
                    file1 = new File(docsFolder.getAbsolutePath(),"GpsPointsExport_"+getDate()+".csv");
                    try {
                        file1.createNewFile();
                        FileWriter f = new FileWriter(file1);
                        //f.write(lastCoord + "\r\n");
                        try {
                            result = db.rawQuery("select * from CoordPoints", null);
                            while(result.moveToNext()){
                                int locDateIndex = result.getColumnIndex("_dateDay");
                                int locTimeIndex = result.getColumnIndex("_dateTime");
                                int latIndex = result.getColumnIndex("_latitude");
                                int lonIndex = result.getColumnIndex("_longitude");
                                int descrIndex = result.getColumnIndex("_description");
                                str1+=result.getString(locDateIndex)+",";
                                str1+=result.getString(locTimeIndex)+",";
                                str1+=result.getString(latIndex)+",";
                                str1+=result.getString(lonIndex)+",";
                                str1+=result.getString(descrIndex);
                                str1+="\r\n";
                                f.write(str1);
                                str1="";
                            }
                        }
                        catch (Exception e){
                            tvDebug2.setText("Ошибка db.rawQuery: " +  e.getMessage());
                            Log.d("TAG1", "Ошибка db.rawQuery: " +  e.getMessage());
                        }
                        f.flush();
                        f.close();
                        Toast.makeText(this, R.string.toast_mesg_create_file_success, Toast.LENGTH_LONG).show();
                        tvDebug2.setText(R.string.toast_mesg_create_file_success);
                    }catch (Exception fe){
                        Toast.makeText(this, R.string.toast_mesg_error_create_file, Toast.LENGTH_SHORT).show();
                        Log.d("TAG1", "Ошибка записи файла: " +  fe.getMessage());
                        tvDebug1.setText("Ошибка записи файла: " +  fe.getMessage());
                    }
                }
                else {
                    Toast.makeText(MainActivity.this,  "Ошибка создания файла", Toast.LENGTH_SHORT).show();
                }
                break;
            case(R.id.buttonNetSave):
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)==
                        PackageManager.PERMISSION_GRANTED)
                {
                    Log.d("TAG1", "INTERNET PERMISSION_GRANTED");

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("android_id", android_id);
                    map.put("user_login", user_login);
                    map.put("lat", latLonTime[0]);
                    map.put("lon", latLonTime[1]);
                    map.put("date", getDate());
                    JSONArray jsonArray = new JSONArray(Arrays.asList(map));
                    Log.d("TAG1", "MA jsonArray=" + jsonArray);
                    //testSend(android_id+"_"+latLonTime[0]+"_"+latLonTime[1]+"_"+getDate());
                    testSend(jsonArray.toString());
                }
                else{
                    Log.d("TAG1", "INTERNET DENY");
                    ActivityCompat.requestPermissions(this, new String[] {
                                    android.Manifest.permission.INTERNET },
                            TAG_CODE_PERMISSION_INTERNET);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

                        Toast.makeText(MainActivity.this,  e.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("TAG1", "009 private String getDataFromUrl catch Exception=" + e.getMessage() + " " + e.getLocalizedMessage());
                        Toast.makeText(MainActivity.this,  e.getMessage(), Toast.LENGTH_LONG).show();
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

    public boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        return (state.equals(Environment.MEDIA_MOUNTED));
    }
    private String getDate() {
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
        return dateFormat.format(currentDate);
    }

    private boolean isServiceStarted(){
        if(MyService.instance!=null){
            Toast.makeText(MainActivity.this,  "Сервис запущен", Toast.LENGTH_SHORT).show();
            return true;
        }
        else{
            Toast.makeText(MainActivity.this,  "Сервис остановлен", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void sendDataToServerByPost(String urlToSendData, String urlParameters) {


        Handler handler = new Handler();  //Optional. Define as a variable in your activity.

        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                HttpURLConnection connection = null;
                String res = "Empty";
                try {

                    //Create connection
                    URL url = new URL(urlToSendData);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");

                    connection.setRequestProperty("Content-Length",
                            Integer.toString(urlParameters.getBytes().length));
                    connection.setRequestProperty("Content-Language", "en-US");

                    connection.setUseCaches(false);
                    connection.setDoOutput(true);

                    //Send request
                    DataOutputStream wr = new DataOutputStream (
                            connection.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.close();

                    //Get Response
                    InputStream is = connection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
                    String line;
                    while ((line = rd.readLine()) != null) {
                        response.append(line);
                        response.append('\r');
                    }
                    rd.close();
                    res = response.toString();
                    Log.d("TAG1", "Net send return result=" + res);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("TAG1", "Net send return result=" + e.getMessage());
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                final String fRes = res;
                handler.post(new Runnable()  //If you want to update the UI, queue the code on the UI thread
                {
                    public void run()
                    {
                        makeToast(fRes);
                    }
                });
            }
        };

        Thread t = new Thread(r);
        t.start();

    }

    public void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public boolean isNetwork(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}