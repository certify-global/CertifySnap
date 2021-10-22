package com.certify.snap.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.certify.fcm.FireBaseMessagingService;
import com.certify.snap.R;
import com.certify.snap.bluetooth.bleCommunication.BluetoothLeService;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Application;
import com.certify.snap.common.ContextUtils;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.controller.ApplicationController;
import com.certify.snap.controller.CameraController;
import com.certify.snap.controller.DeviceSettingsController;
import com.certify.snap.service.DeviceHealthService;
import com.certify.snap.service.LoggerService;
import com.certify.snap.service.MemberSyncService;
import com.certify.snap.service.OfflineRecordSyncService;

import java.util.Locale;

public abstract class BaseActivity extends Activity {

    private BroadcastReceiver activityReceiver;
    private BroadcastReceiver networkReceiver;
    private final String TAG = BaseActivity.this.getClass().getSimpleName();
    protected boolean isDisconnected = false;
    private IntentFilter activityReceiverFilter;
    protected ImageView internetIndicatorImg;

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale localeToSwitchTo = new Locale(DeviceSettingsController.getInstance().getLanguageToUpdate());
        ContextWrapper localeUpdatedContext = ContextUtils.updateLocale(newBase, localeToSwitchTo);
        super.attachBaseContext(localeUpdatedContext);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityReceiver();
        initNetworkReceiver();
        activityReceiverFilter = new IntentFilter();
        activityReceiverFilter.addAction(FireBaseMessagingService.NOTIFICATION_BROADCAST_ACTION);
        activityReceiverFilter.addAction(FireBaseMessagingService.NOTIFICATION_SETTING_BROADCAST_ACTION);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (internetIndicatorImg != null) {
            if (!Util.isNetworkOff(this) && Util.getSharedPreferences(this).getBoolean(GlobalParameters.Internet_Indicator, true)) {
                internetIndicatorImg.setVisibility(View.GONE);
            } else {
                internetIndicatorImg.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activityReceiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(activityReceiver, activityReceiverFilter);
        }
        if (networkReceiver != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            this.registerReceiver(networkReceiver, intentFilter);
        }
        this.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (activityReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(activityReceiver);
        }
        if (networkReceiver != null) {
            this.unregisterReceiver(networkReceiver);
        }
        this.unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkReceiver = null;
        activityReceiver = null;
    }

    private void initActivityReceiver() {
        activityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction().equals(FireBaseMessagingService.NOTIFICATION_BROADCAST_ACTION)) {
                    finishAffinity();
                }
                if (intent != null && intent.getAction().equals(FireBaseMessagingService.NOTIFICATION_SETTING_BROADCAST_ACTION)) {
                    if (isDestroyed() || isFinishing()) return;
                    Toast.makeText(BaseActivity.this, getString(R.string.update_setting_app_restart), Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
            }
        };
    }

    /**
     * get broadcast intent-filter
     *
     * @return
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final Intent mIntent = intent;
            final String action = intent.getAction();

            // connected
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                isDisconnected = false;
                //Toast.makeText(getBaseContext(), R.string.ble_connect_success, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onReceive: " + R.string.ble_connect_success);
            }
            // disconnected
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isDisconnected = true;
                //Toast.makeText(getBaseContext(), R.string.ble_disconnected, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onReceive: " + R.string.ble_disconnected);
            }
        }
    };

    private void initNetworkReceiver() {
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) || ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction()))) {
                    if (Util.isConnectedWifi(context) || Util.isConnectedEthernet(context) || Util.isConnectedMobile(context)) {
                        if (internetIndicatorImg != null) {
                            internetIndicatorImg.setVisibility(View.GONE);
                        }
                        Log.d(TAG, "Health network online");
                        onNetworkConnected(context);
                    } else {
                        Log.d(TAG, "Health network offline");
                    }
                }
            }
        };
    }

    protected void restartApplication() {
        finishAffinity();
        Intent intent = new Intent(this, HomeActivity.class);
        int mPendingIntentId = 111111;
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis(), mPendingIntent);
    }

    protected void checkDeviceMode() {
        runOnUiThread(() -> {
            int mode = CameraController.getInstance().getDeviceMode();
            Log.d(TAG, "Device mode " + mode);
            if (mode == 0 || ApplicationController.getInstance().getTemperatureUtil() == null) {
                Toast.makeText(this, getString(R.string.app_restart_msg), Toast.LENGTH_SHORT).show();
                finishAffinity();
                restartApplication();
            }
        });
    }

    private void onNetworkConnected(Context context) {
        if (!DeviceSettingsController.getInstance().isSettingsRetrieved(context)) {
            if (isDestroyed() || isFinishing()) return;
            Toast.makeText(context, getString(R.string.restarting_wait_msg), Toast.LENGTH_LONG).show();
            Intent guideintent = new Intent(context, HomeActivity.class);
            guideintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            guideintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(guideintent);
        } else {
            updateHealthService(context);
            updateMemberSyncService(context);
            updateOfflineService(context);
            updateLoggerService(context);
        }
    }

    private void updateMemberSyncService(Context context) {
        try {
            SharedPreferences sharedPreferences = Util.getSharedPreferences(this);
            if (!Util.isServiceRunning(MemberSyncService.class, context)) {
                if (sharedPreferences != null && (AppSettings.isFacialDetect()
                        || AppSettings.isRfidEnabled())) {
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

    private void updateOfflineService(Context context) {
        try {
            if (!Util.isServiceRunning(OfflineRecordSyncService.class, context)) {
                Log.d(TAG, "Offline service ");
                context.startService(new Intent(context, OfflineRecordSyncService.class));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in starting offline sync service " +e.getMessage());
        }
    }

    private void updateHealthService(Context context) {
        try {
            SharedPreferences sharedPreferences = Util.getSharedPreferences(this);
            if (sharedPreferences != null && sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true)) {
                if (!Util.isServiceRunning(DeviceHealthService.class, context)) {
                    context.startService(new Intent(context, DeviceHealthService.class));
                    Application.StartService(context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(TAG, "initHealthCheckService()", "Exception occurred in starting DeviceHealth Service" + e.getMessage());
        }
    }

    private void updateLoggerService(Context context) {
        if (!Util.isServiceRunning(LoggerService.class, this)) {
            startService(new Intent(this, LoggerService.class));
            Application.StartService(this);
        }
    }
}
