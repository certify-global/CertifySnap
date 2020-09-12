package com.certify.snap.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.snap.common.Application;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

public class NetworkReceiver extends BroadcastReceiver {

    public static final String TAG = NetworkReceiver.class.getSimpleName();
    private SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        sharedPreferences = Util.getSharedPreferences(context);

        if (("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) || ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction()))) {
            if (Util.isConnectedWifi(context) || Util.isConnectedEthernet(context) || Util.isConnectedMobile(context)) {
                try {
                    if (sharedPreferences != null && sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true))
                        if (!Util.isServiceRunning(DeviceHealthService.class, context)) {
                            context.startService(new Intent(context, DeviceHealthService.class));
                            Application.StartService(context);
                        }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.error(TAG, "initHealthCheckService()", "Exception occurred in starting DeviceHealth Service" + e.getMessage());
                }

                try {
                    if (!Util.isServiceRunning(MemberSyncService.class, context)) {
                        if (sharedPreferences != null && (sharedPreferences.getBoolean(GlobalParameters.FACIAL_DETECT, true)
                                || sharedPreferences.getBoolean(GlobalParameters.RFID_ENABLE, false))) {
                            if (sharedPreferences.getBoolean(GlobalParameters.SYNC_ONLINE_MEMBERS, false)) {
                                context.startService(new Intent(context, MemberSyncService.class));
                                Application.StartService(context);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in starting Member sync service " +e.getMessage());
                }
            }
        }
    }
}
