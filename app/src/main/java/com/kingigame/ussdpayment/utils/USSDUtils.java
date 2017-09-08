package com.kingigame.ussdpayment.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by sanglx on 9/8/17.
 */

public class USSDUtils {
    private static final String TAG = USSDUtils.class.getSimpleName();

    public static String makeUSSDString(String code, String serial) {
        String ussd = "*" + code + "*" + serial + Uri.encode("#");
        return ussd;
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
