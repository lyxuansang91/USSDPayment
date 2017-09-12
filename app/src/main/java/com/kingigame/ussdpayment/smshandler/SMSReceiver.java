package com.kingigame.ussdpayment.smshandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.kingigame.ussdpayment.utils.USSDUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sanglx on 9/8/17.
 */

public class SMSReceiver extends BroadcastReceiver {
    private static final String TAG = SMSReceiver.class.getSimpleName();
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    // private static final String SMS_DELIVER = "android.provider.Telephony.SMS_DELIVER";
    final SmsManager smsManager = SmsManager.getDefault();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "on received message");
        Log.d(TAG, "intent get action:" + intent.getAction());
        if(intent.getAction().equals(SMS_RECEIVED)) {
            Map<String, String> mMessages = new HashMap<String, String>();
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus.length == 0) {
                    return;
                }
                SmsMessage[] messages;
                if (Build.VERSION.SDK_INT >= 19) {
                    messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                } else {
                    messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            String format = bundle.getString("format");
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                        } else {
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        }
                    }
                }
                if (messages.length > 0) {
                    // handle message sms
                    for (SmsMessage message: messages) {
                        if(!mMessages.containsKey(message.getOriginatingAddress())){
                            mMessages.put(message.getOriginatingAddress(), message.getMessageBody());
                        } else {
                            String messageBody = mMessages.get(message.getOriginatingAddress());
                            mMessages.put(message.getOriginatingAddress(), messageBody + message.getMessageBody());
                        }
                    }
                }
            }

           USSDUtils.checkValidTopupSMS(mMessages);
        }
    }
}

