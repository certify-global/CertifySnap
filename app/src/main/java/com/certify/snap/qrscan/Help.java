package com.certify.snap.qrscan;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;

public class Help {

    public static final class permission {
        public static final String[] camera = new String[]{android.Manifest.permission.CAMERA};
        public static final String[] contacts = new String[]{android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS, android.Manifest.permission.GET_ACCOUNTS};
        public static final String[] location = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
        //  public static final String[] phone = new String[]{android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.CALL_PHONE, android.Manifest.permission.READ_CALL_LOG, android.Manifest.permission.PROCESS_OUTGOING_CALLS};
        public static final String[] sms = new String[]{android.Manifest.permission.READ_SMS, android.Manifest.permission.RECEIVE_SMS, android.Manifest.permission.RECEIVE_MMS};
        public static final String[] storage = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

    }

    public static boolean PermissionRequest(android.app.Activity context, String[] permissions) {
        try {
            if (Build.VERSION.SDK_INT < 23 || permissions == null) return false;
            ArrayList<String> requestPermission = new ArrayList<>();
            for (String permission : permissions) {
                int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED)
                    requestPermission.add(permission);
            }
            if (requestPermission.size() <= 0) return false;
            ActivityCompat.requestPermissions(context, requestPermission.toArray(new String[0]), 1);
        } catch (Exception e) {
            Log.e("PermissionRequest()", e.getMessage());
        }
        return true;
    }

    public static boolean isConnectingToInternet(Context context) {
        try {
            //noinspection deprecation
            ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
            if (cm != null)
                for (NetworkInfo ni : cm.getAllNetworkInfo()) {
                    switch (ni.getTypeName().trim().toUpperCase()) {
                        case "WIFI":
                        case "MOBILE":
                            if (ni.isConnected() && !(ni.getSubtypeName() != null && ni.getSubtypeName().trim().toUpperCase().equals("LTE") && ni.getExtraInfo() != null && ni.getExtraInfo().trim().toUpperCase().equals("IMS"))) {
                                // MyApplication.noInternet = 0;
                                return true;
                            }
                            break;
                    }
                }
        } catch (Exception e) {
            Log.e("isConnectingToInternet", e.getMessage());
        }
        return false;
    }

   /* public static JSONObject getJSONObject(JSONObject obj, String url) {
        try {
            String responseStr = Requestor.requestJson(Help.rabbitTrackAdmin + url, obj, "");
            Log.d("getJSONObject", responseStr);
            return new JSONObject(responseStr);

        } catch (Exception e) {
            Log.e("getJSONObject", e.getMessage());
        }
        return null;
    }*/
}
