package com.example.gps_tracker.Helpers;

public class UpdateHelper {

    WebSendGetHelper _webSendGetHelper;
    private final double currentAppVersion = 0.1;
    public double getCurrentVersion() {
        return this.currentAppVersion;
    }

    public double getLastVersionOnUpdateCenter() throws InterruptedException {
        _webSendGetHelper = new WebSendGetHelper(
                "https://api.maxbarannyk.ru/test-return-post",
                "testKey0001",
                "testValue00001"
        );
        _webSendGetHelper.start();
        _webSendGetHelper.join();
        String resultFromThread = _webSendGetHelper.getResult();
        System.out.println("Got result:" + resultFromThread);
        //send request
        //get double
        return 0.1;

    }
}
