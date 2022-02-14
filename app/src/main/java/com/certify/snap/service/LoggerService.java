package com.certify.snap.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.certify.snap.async.AsyncDeviceLog;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.LoggerUtil;
import com.certify.snap.common.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import static android.os.SystemClock.elapsedRealtime;

public class LoggerService extends Service implements LoggerUtil.LogMessagesCallback {

    private final String TAG = LoggerService.class.getSimpleName();
    private final static int BACKGROUND_INTERVAL_MINUTES = 60;
    private AlarmManager alarmService;
    private PendingIntent restartServicePendingIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Logger.debug(TAG, "onStartCommand()");
            LoggerUtil.getInstance().setListener(this);
            sendDeviceLogs(getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, "onStartCommand(Intent.)" + e.getMessage());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    public void sendDeviceLogs(Context context) {
        if (Util.isOfflineMode(context)) return;
        LoggerUtil.getInstance().logMessagesToFile(context, "AppLog");
    }

    @Override
    public void onLogMessagesToFile(String fileName) {
        JSONObject obj = new JSONObject();
        SharedPreferences sharedPreferences = Util.getSharedPreferences(getApplicationContext());
        try {
            obj.put("deviceSN", Util.getSNCode(getApplicationContext()));
            String encodedData = Base64.encodeToString(Util.getBytesFromFile(fileName), Base64.NO_WRAP);
            obj.put("deviceLog", encodedData);
            obj.put("deviceData", Util.MobileDetails(getApplicationContext()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new AsyncDeviceLog(obj, null, sharedPreferences.getString(GlobalParameters.URL,
                EndPoints.prod_url) + EndPoints.DeviceLogs, getApplicationContext()).execute();
    }
}