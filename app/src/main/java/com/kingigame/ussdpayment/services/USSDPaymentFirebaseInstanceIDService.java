package com.kingigame.ussdpayment.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by sanglx on 9/8/17.
 */

public class USSDPaymentFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = USSDPaymentFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(refreshToken);
        subscribeTopic(this.getApplicationContext().getPackageName());
    }

    private void subscribeTopic(String pkgName) {
        Log.d(TAG, "package name:" + pkgName);
        FirebaseMessaging.getInstance().subscribeToTopic(pkgName);
    }

    private void sendRegistrationToServer(String refreshToken) {
        // TODO: send refresh token to server
        Log.d(TAG, "refresh token:" + refreshToken);
    }
}
