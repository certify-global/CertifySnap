package com.certify.snap.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.certify.snap.common.Application;
import com.certify.snap.common.Logger;

@SuppressLint("SpecifyJobSchedulerIdRange")
public class DeviceHealthJobService extends JobService {
    public static String TAG = DeviceHealthJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Logger.debug(TAG, "DeviceHealthJobService onStartJob");
        scheduledJob(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Logger.debug(TAG, "DeviceHealthJobService onStopJob");
        return false;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void scheduledJob(Context context) {
        //schedule with JobScheduler below code

        Intent intent = new Intent(context, DeviceHealthService.class);
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
         PendingIntent pendingIntent =
                PendingIntent.getService(context, Application.REQUEST_CODE_HEALTH, intent,PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent == null) {
            Logger.debug(TAG, "not running");
            Application.getInstance().runDeviceService();
            //reschedule device health check.
        }else Logger.debug(TAG, "it  running");

    }
}
