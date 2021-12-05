package com.example.gps_tracker;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestSenderHelper {

    private Activity _activity;
    private Context _context;
    private String _urlToSendRequest;
    private String _dataToSend;

    public RequestSenderHelper(Context context, String urlToSendRequest, String dataToSend) {
        //this._activity = activity;
        this._context = context;
        this._urlToSendRequest = urlToSendRequest;
        this._dataToSend = dataToSend;
    }

    public void sendDataToServerByGet() {
        Handler handler = new Handler();  //Optional. Define as a variable in your activity.

        Runnable r = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                String res = "Empty";
                try {

                    //Create connection
                    URL url = new URL(_urlToSendRequest);Log.d("TAG1", "LINE 41");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");

                    connection.setRequestProperty("Content-Length",
                            Integer.toString(_dataToSend.getBytes().length));
                    connection.setRequestProperty("Content-Language", "en-US");

                    connection.setUseCaches(false);
                    connection.setDoOutput(true);Log.d("TAG1", "LINE 52");

                    //Send request
                    DataOutputStream wr = new DataOutputStream(
                            connection.getOutputStream());
                    wr.writeBytes(_dataToSend);
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
                    Log.d("TAG1", "Net send return result 71=" + res);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("TAG1", "Net send return result 74=" + e.getMessage());
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                final String fRes = res;
                handler.post(new Runnable()  //If you want to update the UI, queue the code on the UI thread
                {
                    public void run() {
                        makeToast(fRes);
                    }
                });
            }
        };

        Thread t = new Thread(r);
        t.start();

    }

    public void makeToast(String message) {
        Toast.makeText(_context, message, Toast.LENGTH_SHORT).show();
    }
}
