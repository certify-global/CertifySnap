package com.certify.snap.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.certify.snap.activity.IrCameraActivity;
import com.certify.snap.activity.ProIrCameraActivity;
import com.certify.snap.api.ApiInterface;
import com.certify.snap.api.RetrofitInstance;
import com.certify.snap.api.request.HealthCheckRequest;
import com.certify.snap.api.response.HealthCheckResponse;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.controller.ApplicationController;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceHealthService extends Service {
    protected static final String LOG = DeviceHealthService.class.getSimpleName();
    private final static int BACKGROUND_INTERVAL_10_MINUTES = 10;

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
            Logger.debug(LOG, "onStartCommand(Intent intent, int flags, int startId)");
            getDeviceHealthCheck(getApplicationContext());
        } catch (Exception e) {
            Log.e(LOG + "onStartCommand(Intent.)", e.getMessage());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void getDeviceHealthCheck(Context context) {
        Logger.debug(LOG, "getDeviceHealthCheck");
        try {
            if (ApplicationController.getInstance().checkTokenExpiry(context)) {
                Log.d(LOG, "Refresh the token");
                ApplicationController.getInstance().getToken(context);
                return;
            }
            ApiInterface apiInterface = RetrofitInstance.getInstance().getApiInterface();
            if (apiInterface == null) {
                Logger.error(LOG, "apiInterface null");
                return;
            }
            HealthCheckRequest healthCheckRequest = new HealthCheckRequest();
            healthCheckRequest.deviceSN = AppSettings.getSNCode();
            healthCheckRequest.deviceInfo = AppSettings.getDeviceInfo();
            healthCheckRequest.institutionId = AppSettings.getInstitutionId();
            healthCheckRequest.appState = Util.getAppState();
            healthCheckRequest.appUpTime = Util.getAppUpTime(context);
            healthCheckRequest.deviceUpTime = Util.getDeviceUpTime();
            Logger.debug(LOG, "HealthCheck Request " + Util.getMMDDYYYYDate());
            healthCheckRequest.lastUpdateDateTime = Util.getUTCDate("");

            Call<HealthCheckResponse> call = apiInterface.getDeviceHealthCheck(Util.getSNCode(context), healthCheckRequest);
            call.enqueue(new Callback<HealthCheckResponse>() {
                @Override
                public void onResponse(Call<HealthCheckResponse> call, Response<HealthCheckResponse> response) {
                    if (response.body() != null) {
                        Logger.debug(LOG, "HealthCheckResponse onResponse = " + response.body().responseCode+",response.code() ="+response.code()); //401 failed token
                    }
                }

                @Override
                public void onFailure(Call<HealthCheckResponse> call, Throwable t) {
                    Log.e(LOG, "Health Error in device health check " + t.getMessage());
                }
            });

        } catch (Exception e) {
            Logger.error(LOG, "getDeviceHealthCheck()", e.getMessage());

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("DeviceHealthService", "onDestroy");

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