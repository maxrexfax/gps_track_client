package com.example.gps_tracker;

import android.app.Service;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

public class ThreadServiceChecker extends Thread {
    private boolean isWorking;
    private LocationManager locManager;
    private Service srv;
    public ThreadServiceChecker(LocationManager locationManager, Service service){
        isWorking = true;
        locManager = locationManager;
        srv = service;
        //Log.d("TAG1", "ThreadServiceChecker constructor");
    }
    @Override
    public void run() {
        while (isWorking){
            if(isWorking){
                try {
                    if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

                    }
                    else{
                        if(MyService.instance!=null) {
                           // Log.d("TAG1", "ThreadServiceChecker GPS_PROVIDER check failed");
                            srv.onDestroy();
                            // isServiceStarted = false;
                        }
                    }
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        super.run();
    }
}
