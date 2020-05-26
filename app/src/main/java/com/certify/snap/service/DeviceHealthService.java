package com.certify.snap.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.certify.callback.JSONObjectCallback;
import com.certify.snap.R;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

import org.json.JSONObject;

import java.util.Calendar;

import static android.os.SystemClock.elapsedRealtime;

public class DeviceHealthService extends Service implements JSONObjectCallback {
   protected static final String LOG = "BackgroundSyncService - ";
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
           Util.getDeviceHealthCheck(this,this);
      } catch (Exception e) {
         Logger.error(LOG + "onStartCommand(Intent intent, int flags, int startId)", e.getMessage());
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
   public void onJSONObjectListener(String report, String status, JSONObject req) {
      //do noop
   }

   @Override
   public void onTaskRemoved(Intent rootIntent) {
      super.onTaskRemoved(rootIntent);
      stopSelf();
   }
}