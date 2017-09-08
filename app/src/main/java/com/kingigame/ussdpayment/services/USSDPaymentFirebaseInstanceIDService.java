package com.kingigame.ussdpayment.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by sanglx on 9/8/17.
 */

public class USSDPaymentFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = USSDPaymentFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(refreshToken);
    }

    private void sendRegistrationToServer(String refreshToken) {
        // TODO: send refresh token to server
        Log.d(TAG, "refresh token:" + refreshToken);
    }
}
