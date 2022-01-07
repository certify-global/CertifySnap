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

import com.certify.snap.activity.IrCameraActivity;
import com.certify.snap.activity.ProIrCameraActivity;
import com.certify.snap.api.ApiInterface;
import com.certify.snap.api.RetrofitInstance;
import com.certify.snap.api.request.DeviceInfo;
import com.certify.snap.api.request.HealthCheckRequest;
import com.certify.snap.api.response.HealthCheckResponse;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.ApplicationLifecycleHandler;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.ApplicationController;
import com.google.gson.Gson;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.SystemClock.elapsedRealtime;

public class DeviceHealthService extends Service {
    protected static final String LOG = DeviceHealthService.class.getSimpleName();
    private final static int BACKGROUND_INTERVAL_10_MINUTES = 10;
    private AlarmManager alarmService;
    private PendingIntent restartServicePendingIntent;
    public static String HEALTH_CHECK_OFFLINE_ACTION = "com.action.health.offline";
    public static String HEALTH_CHECK_ONLINE_ACTION = "com.action.health.online";

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
            getDeviceHealthCheck();
        } catch (Exception e) {
            Log.e(LOG + "onStartCommand(Intent.)", e.getMessage());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void getDeviceHealthCheck() {
        if (ApplicationController.getInstance().checkTokenExpiry(this)) {
            Log.d(LOG, "Refresh the token");
            ApplicationController.getInstance().getToken(this);
            return;
        }
        SharedPreferences sharedPreferences = Util.getSharedPreferences(this);
        ApiInterface apiInterface = RetrofitInstance.getInstance().getApiInterface();
        HealthCheckRequest healthCheckRequest = new HealthCheckRequest();
        healthCheckRequest.appState = Util.getAppState();
        healthCheckRequest.deviceSN = Util.getSNCode(this);
        healthCheckRequest.deviceInfo = Util.getDeviceInfo(this);
        healthCheckRequest.institutionId = sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, "");
        healthCheckRequest.appState = Util.getAppState();
        healthCheckRequest.appUpTime = Util.getAppUpTime(this);
        healthCheckRequest.deviceUpTime = Util.getDeviceUpTime();
        healthCheckRequest.lastUpdateDateTime = Util.getUTCDate("");
        Util.sendOfflineServiceBroadcast(getApplicationContext());

        Log.d(LOG, "Health check time " + Util.getMMDDYYYYDate());
        Call<HealthCheckResponse> call = apiInterface.getDeviceHealthCheck(Util.getSNCode(this), healthCheckRequest);
        call.enqueue(new Callback<HealthCheckResponse>() {
            @Override
            public void onResponse(Call<HealthCheckResponse> call, Response<HealthCheckResponse> response) {
                if (response.body() != null) {
                    Log.d(LOG, "Health Response " + response.body().responseCode);
                    if (response.body().responseCode == 1) {
                        ApplicationController.getInstance().cancelHealthCheckTimer(getApplicationContext());
                        if(ApplicationLifecycleHandler.isInBackground)
                            bringApplicationToForeground();
                    }
                    if (response.body().message.contains("Authorization has been denied for this request")) {
                        Log.d(LOG, "DeviceHealthService Get Token");
                        ApplicationController.getInstance().getToken(getApplicationContext());
                    }
                }
            }

            @Override
            public void onFailure(Call<HealthCheckResponse> call, Throwable t) {
                Log.e(LOG, "Health Error in device health check " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alarmService != null && restartServicePendingIntent != null) {
            alarmService.cancel(restartServicePendingIntent);
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