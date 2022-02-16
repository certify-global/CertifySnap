package com.certify.snap.service;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.certify.snap.common.Application;
import com.certify.snap.common.Logger;


public class DeviceHealthWorkManager extends Worker {
    public static String TAG = DeviceHealthWorkManager.class.getSimpleName();
    private Context context;

    public DeviceHealthWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Logger.debug(TAG, "doWork");
        deviceHealthWork();
        return Result.success();
    }

    private void deviceHealthWork() {
        try {
            //schedule with JobScheduler below code
            Intent intent = new Intent(context, DeviceHealthService.class);
            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent =
                    PendingIntent.getService(context, Application.REQUEST_CODE_HEALTH, intent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent == null) {
                Logger.debug(TAG, "not running");
                Application.getInstance().runDeviceService(); //reschedule device health check.
            } else Logger.debug(TAG, "it  running");
        } catch (Exception e) {
            Logger.error(TAG, "deviceHealthWork() ->" + e.getMessage());
        }
    }
}
