package com.kingigame.ussdpayment.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import com.kingigame.ussdpayment.USSDApplication;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by sanglx on 9/8/17.
 */

public class USSDUtils {
    private static final String TAG = USSDUtils.class.getSimpleName();

    public static String makeUSSDString(String code, String serial) {
        String ussd = "*" + code + "*" + serial + Uri.encode("#");
        return ussd;
    }

    public static boolean checkValidTopupSMS(Map<String, String> mMessage) {
        if (mMessage.isEmpty()) return false;
        Set entrySet = mMessage.entrySet();
        Iterator itr = entrySet.iterator();
        for(Map.Entry m : mMessage.entrySet()) {
            String address = String.valueOf(m.getKey());
            String body = String.valueOf(m.getValue());
            if (address.contains("195") && body.contains("Quy khach co") && body.contains("trong tai khoan")) { // Viettel
                return true;
            }
        }
        return false;
    }

    public static void sendUSSD(Context context, String ussd) {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(ussd));
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            context.startActivity(intent);
        } catch(Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

    }
}
