package com.certify.snap.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.certify.snap.common.Util;

import java.util.Calendar;

import static android.os.SystemClock.elapsedRealtime;

public class LoggerService extends Service {

    private final String TAG = LoggerService.class.getSimpleName();
    private final static int BACKGROUND_INTERVAL_MINUTES = 64;
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
            restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, new Intent(this, LoggerService.class), PendingIntent.FLAG_ONE_SHOT);
            alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Calendar cal = Calendar.getInstance();
            long sysTime = elapsedRealtime();
            cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + (BACKGROUND_INTERVAL_MINUTES - (cal.get(Calendar.MINUTE) % BACKGROUND_INTERVAL_MINUTES)));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long currTime = Util.getCurrentTimeLong();
            if (alarmService != null)
                alarmService.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, sysTime + (cal.getTimeInMillis() - currTime), restartServicePendingIntent);
            Util.sendDeviceLogs(this);
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
}