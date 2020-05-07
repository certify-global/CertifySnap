package com.certify.snap.common;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.microsoft.appcenter.analytics.Analytics;


public class Logger {
    public static void debug(String classname, String message) {
        Log.d(classname, "" + message);
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message + "", Toast.LENGTH_SHORT).show();
    }

    public static void error(String classname, String message) {
        if (EndPoints.deployment == EndPoints.Mode.Demo) {
            if (message == null) message = "null";
            Log.e(classname,message);
            Analytics.trackEvent("Date Time"+Util.getMMDDYYYYDate()+"SN"+Util.getSNCode()+"Error:"+message);
        }else{
            Analytics.trackEvent("Date Time"+Util.getMMDDYYYYDate()+"SN"+Util.getSNCode()+"Error:"+message);
        }

    }
}