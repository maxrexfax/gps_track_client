package com.example.gps_tracker;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ThreadChecker extends Thread {

    private static final String BROADCAST_DATA = "broacasting_service";
    private static final String BROADCAST_KEY = "broacasting_key";
    private boolean isWorking;
    private MainActivity activity;

    public ThreadChecker(MainActivity act){
        this.activity = act;
        isWorking = true;
        //HelperClass.logString("ThreadChecker constructor");
    }
    public void threadCheckerStop(){
        isWorking = false;
    }
    @Override
    public void run() {
        while (isWorking){
            if(isWorking){
                try {
                    int srvState;
                    Intent intent = new Intent(BROADCAST_DATA);
                    if(MyService.instance == null){
                        srvState = 0;
                    }
                    else{
                        srvState = 1;
                    }
                    intent.putExtra(BROADCAST_KEY, srvState);
                    activity.sendBroadcast(intent);
                    /*activity.runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Toast.makeText(activity.getApplicationContext(), "Тред работает!", Toast.LENGTH_SHORT).show();
                        }
                    });*/
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        super.run();
    }

}
