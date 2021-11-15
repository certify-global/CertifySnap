package com.certify.snap.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.certify.callback.JSONObjectCallback;
import com.certify.snap.activity.IrCameraActivity;
import com.certify.snap.activity.ProIrCameraActivity;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.ApplicationLifecycleHandler;
import com.certify.snap.common.Constants;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.controller.ApplicationController;

import org.json.JSONObject;

import java.util.Calendar;

import static android.os.SystemClock.elapsedRealtime;

public class DeviceHealthService extends Service implements JSONObjectCallback {
    protected static final String LOG = DeviceHealthService.class.getSimpleName();
    private final static int BACKGROUND_INTERVAL_10_MINUTES = 10;
    private AlarmManager alarmService;
    private PendingIntent restartServicePendingIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
        try {
            //sendNotificationEvent(getString(R.string.app_name), "Alert Background MyRabbit", "", getApplicationContext());
            restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, new Intent(this, DeviceHealthService.class), PendingIntent.FLAG_ONE_SHOT);
            alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Calendar cal = Calendar.getInstance();
            long sysTime = elapsedRealtime();
            cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + (BACKGROUND_INTERVAL_10_MINUTES - (cal.get(Calendar.MINUTE) % BACKGROUND_INTERVAL_10_MINUTES)));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long currTime = Util.getCurrentTimeLong();
            if (alarmService != null)
                alarmService.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, sysTime + (cal.getTimeInMillis() - currTime), restartServicePendingIntent);
            Util.getDeviceHealthCheck(this, this);
        } catch (Exception e) {
            Log.e(LOG + "onStartCommand(Intent.)", e.getMessage());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alarmService != null && restartServicePendingIntent != null) {
            alarmService.cancel(restartServicePendingIntent);
        }
    }

    @Override
    public void onJSONObjectListener(String reportInfo, String status, JSONObject req) {
        //do noop
        try {
            SharedPreferences sharedPreferences = Util.getSharedPreferences(getApplicationContext());
            if (reportInfo == null) {
                Log.d("DeviceHealthService", "Health check error response "+ Util.getMMDDYYYYDate());
                Util.writeBoolean(sharedPreferences, GlobalParameters.Internet_Indicator, false);
                return;
            }
            JSONObject json = new JSONObject(reportInfo);
            if (json.has("responseCode") && json.getInt("responseCode") == 1) {
                Log.d("DeviceHealthService", "Health check success response "+ Util.getMMDDYYYYDate());
                Util.writeBoolean(sharedPreferences, GlobalParameters.Internet_Indicator, true);
                ApplicationController.getInstance().cancelHealthCheckTimer();
                if(ApplicationLifecycleHandler.isInBackground)
                    bringApplicationToForeground();
            } else {
                Util.writeBoolean(sharedPreferences, GlobalParameters.Internet_Indicator, false);
            }

            if (reportInfo.contains("token expired")) {
                Log.d("DeviceHealthService", "Health check token expired "+ Util.getMMDDYYYYDate());
                Util.getToken(this, this);
            }

        } catch (Exception e) {
            Logger.error(LOG + "onJSONObjectListener(JSONObject reportInfo, String status, JSONObject req)", e.getMessage());
        }
    }

    private void bringApplicationToForeground() {
        Log.d("DeviceHealthService", "BringApplicationToFront");
        Class<?> activity = IrCameraActivity.class;
        if (Util.isDeviceProModel() && AppSettings.isProSettings()) {
            activity = ProIrCameraActivity.class;
        }
        Intent notificationIntent = new Intent(this, activity);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Log.e("DeviceHealthService", "Exception in bringApplicationToForeground " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
}