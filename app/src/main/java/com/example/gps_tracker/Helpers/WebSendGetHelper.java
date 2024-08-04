package com.example.gps_tracker.Helpers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebSendGetHelper extends Thread{

    public String urlString;
    public String propertyKey;
    public String propertyValue;
    public String messageToSend;

    private String result;

    public WebSendGetHelper(String url, String key, String value) {
        this.urlString = url;
//        this.propertyKey = key;
//        this.propertyValue = value;
        this.messageToSend = key + "=" + value;
    }

    public String getResult() {
        return this.result;
    }
    @Override
    public void run() {
        HttpURLConnection connection = null;
//        String res = "Empty";
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
            result = response.toString();
            HelperClass.logString("WebSendHelper got result:" + result);
        } catch (Exception e) {
            e.printStackTrace();
            HelperClass.logString("WebSendHelper Net send catch Exception:" + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
