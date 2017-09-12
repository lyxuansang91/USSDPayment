package com.kingigame.ussdpayment;

import android.app.Application;

/**
 * Created by sanglx on 9/11/17.
 */

public class USSDApplication extends Application {
    private static USSDApplication _instance;

    public static USSDApplication getInstance() {
        return _instance;
    }

    @Override
    public void onCreate() {
        _instance = this;
        super.onCreate();
    }
}
