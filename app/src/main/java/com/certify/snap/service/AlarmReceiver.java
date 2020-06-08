package com.certify.snap.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.certify.snap.common.Logger;

public class AlarmReceiver extends BroadcastReceiver {
    public static Context sInstance = null;
    private static String LOG = "AlarmReceiver - ";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
//            if (Util.isServiceRunning(BeaconService.class, context))
//                context.stopService(new Intent(context, BackgroundSyncService.class));
            context.startService(new Intent(context, DeviceHealthService.class));
            context.startService(new Intent(context, MemberSyncService.class));
            //  BackgroundSyncService.callFromCreate = true;
            // MyApplication.StartService(context, null, intent);
        } catch (Exception e) {
            Logger.error(LOG + "onReceive(Context context, Intent intent)", e.getMessage());
        }
    }
}
