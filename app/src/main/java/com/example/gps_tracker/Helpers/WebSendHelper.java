package com.example.gps_tracker.Helpers;

import android.app.Activity;
import android.content.Context;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebSendHelper {
    public final String TAG_LOG = "TAG_FOR_LOG";
//    private Context _context;
//    private Activity activity;
    public final String SECRET = "94956781";
    public final String SECRET_KEY = "abyrvalg";
    private String _urlToAdvInfo = "https://api.maxbarannyk.ru/create-new-device";
    private String _urlToSendGpsData = "https://api.maxbarannyk.ru/save-gps-data";
    private String _urlToSendGpsPoint = "https://api.maxbarannyk.ru/save-gps-point";

    public WebSendHelper() {
    }

    public void sendGpsData(String message) {
        sendData(message, _urlToSendGpsData);
    }

    public void sendGpsPoint(String message) {
        sendData(message, _urlToSendGpsPoint);
    }

    public void sendAppCredentials(String androidId, String deviceName) {
        HelperClass.logString("WebSendHelper sendData() worked");
        String data = "android_id=" + androidId + "&device_name=" + deviceName;
        sendData(data, _urlToAdvInfo);
    }
    public void sendData(String messageToSend, String urlString) {
        HelperClass.logString("WebSendHelper sendData() worked");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                String res = "Empty";
                try {
                    //Create connection
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");
                    HelperClass.logString("WebSendHelper messageToSend:" + messageToSend);
                    connection.setRequestProperty("Content-Length",
                            Integer.toString(messageToSend.getBytes().length));
                    connection.setRequestProperty("Content-Language", "en-US");

                    connection.setUseCaches(false);
                    connection.setDoOutput(true);

                    //Send request
                    DataOutputStream wr = new DataOutputStream(
                            connection.getOutputStream());
                    wr.writeBytes(messageToSend);
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
                    HelperClass.logString("WebSendHelper got result:" + res);
                } catch (Exception e) {
                    e.printStackTrace();
                     HelperClass.logString("WebSendHelper Net send catch Exception:" + e.getMessage());
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }

            }
        };

        Thread t = new Thread(r);
        t.start();
    }

}
