package com.certify.snap.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.certify.snap.common.AppSettings;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

public class AlarmReceiver extends BroadcastReceiver {
    public static Context sInstance = null;
    private static String LOG = "AlarmReceiver - ";
    SharedPreferences  sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Logger.debug(LOG, "onReceive - AlarmReceiver");
            sharedPreferences = Util.getSharedPreferences(context);
//            if (Util.isServiceRunning(BeaconService.class, context))
//                context.stopService(new Intent(context, BackgroundSyncService.class));
 //           context.startService(new Intent(context, DeviceHealthService.class));
            if((AppSettings.isFacialDetect() || AppSettings.isRfidEnabled())
                    && sharedPreferences.getBoolean(GlobalParameters.SYNC_ONLINE_MEMBERS,false))
                context.startService(new Intent(context, MemberSyncService.class));
            //  BackgroundSyncService.callFromCreate = true;
            // MyApplication.StartService(context, null, intent);
        } catch (Exception e) {
            Logger.error(LOG + "onReceive(Context context, Intent intent)", e.getMessage());
        }
    }
}
