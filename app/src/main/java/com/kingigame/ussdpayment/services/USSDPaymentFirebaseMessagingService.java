package com.kingigame.ussdpayment.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by sanglx on 9/8/17.
 */

public class USSDPaymentFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = USSDPaymentFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage != null && remoteMessage.getData().size() > 0) {
            Log.d(TAG, "From:" + remoteMessage.getFrom());
            Log.d(TAG, "Message data payload:" + remoteMessage.getData());

        }
        Log.d(TAG, "Message notification body:" + remoteMessage.getNotification().getBody());
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
    }
}
