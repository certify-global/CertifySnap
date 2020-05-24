package com.certify.snap.common;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.microsoft.appcenter.analytics.Analytics;

import java.util.HashMap;
import java.util.Map;


public class Logger {
    private static Map<String, String> properties = new HashMap<String, String>() {{
        put("sn", Util.getSerialNumber());
    }};

    public static void debug(String classname, String message) {

        Log.d(classname, "" + message);
        Analytics.trackEvent(classname+" "+message, properties);
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message + "", Toast.LENGTH_SHORT).show();
    }

    public static void error(String classname, String message) {
        Log.e(classname, message);
        Analytics.trackEvent(classname+" "+message, properties);
    }

    public static void warn(String tag, String message) {
        Log.w(tag, message);
        Analytics.trackEvent(message, properties);
    }

    public static void debug(String classname, String methodName, String message) {
        Log.d(classname, methodName + "-" + message);
        Analytics.trackEvent(classname+" "+methodName+" "+message, properties);
    }

    public static void error(String classname, String methodName, String message) {
        Log.e(classname, methodName + "-" + message);
        Analytics.trackEvent(classname+" "+methodName+" "+message, properties);
    }
}