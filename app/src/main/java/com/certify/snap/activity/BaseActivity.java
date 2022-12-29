package com.certify.snap.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.certify.snap.common.ContextUtils;
import com.certify.snap.common.Util;
import com.certify.snap.controller.ApplicationController;
import com.certify.snap.controller.CameraController;
import com.certify.snap.controller.DeviceSettingsController;
import com.certify.snap.service.DeviceHealthService;
import com.certify.snap.service.MemberSyncService;
import com.certify.snap.service.OfflineRecordSyncService;

import java.util.Locale;

public abstract class BaseActivity extends Activity {

    private BroadcastReceiver activityReceiver;
    private BroadcastReceiver offlineCheckReceiver;
    private final String TAG = BaseActivity.this.getClass().getSimpleName();
    protected boolean isDisconnected = false;
    private IntentFilter activityReceiverFilter;
    public static ImageView internetIndicatorImg;
    protected boolean cameraError = false;

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
        initOfflineCheckReceiver();
        activityReceiverFilter = new IntentFilter();
        activityReceiverFilter.addAction(FireBaseMessagingService.NOTIFICATION_BROADCAST_ACTION);
        activityReceiverFilter.addAction(FireBaseMessagingService.NOTIFICATION_SETTING_BROADCAST_ACTION);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (internetIndicatorImg != null) {
            if (Util.isNetworkOff(this)) {
                internetIndicatorImg.setVisibility(View.VISIBLE);
            } else {
                internetIndicatorImg.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activityReceiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(activityReceiver, activityReceiverFilter);
        }
        this.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (offlineCheckReceiver != null) {
            IntentFilter intentFilter = new IntentFilter();
            //intentFilter.addAction(DeviceHealthService.HEALTH_CHECK_OFFLINE_ACTION);
            intentFilter.addAction(DeviceHealthService.HEALTH_CHECK_ONLINE_ACTION);
            LocalBroadcastManager.getInstance(this).registerReceiver(offlineCheckReceiver, intentFilter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (activityReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(activityReceiver);
        }
        this.unregisterReceiver(mGattUpdateReceiver);
        if (offlineCheckReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(offlineCheckReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityReceiver = null;
        offlineCheckReceiver = null;
        cameraError = false;
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

    private void initOfflineCheckReceiver() {
        offlineCheckReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    runOnUiThread(() -> {
                        if (intent.getAction().equals(DeviceHealthService.HEALTH_CHECK_ONLINE_ACTION)) {
                            updateOfflineService(BaseActivity.this);
                        }
                    });
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

    protected void restartApplication() {
//        finishAffinity();
//        Intent intent = new Intent(this, HomeActivity.class);
//        int mPendingIntentId = 111111;
//        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis(), mPendingIntent);
    }

    protected void checkDeviceMode() {
        runOnUiThread(() -> {
            int mode = CameraController.getInstance().getDeviceMode();
            Log.d(TAG, "Device mode " + mode);
            if (mode == 0 || (ApplicationController.getInstance().getTemperatureUtil() == null) || cameraError) {
                Toast.makeText(this, getString(R.string.app_restart_msg), Toast.LENGTH_SHORT).show();
             //   finishAffinity();
              //  restartApplication();
            }
        });
    }

    private void updateOfflineService(Context context) {
        try {
            if (!Util.isOfflineMode(this)) {
                Log.d(TAG, "Offline service ");
                if (internetIndicatorImg != null) {
                    internetIndicatorImg.setVisibility(View.GONE);
                }
                stopService(new Intent(context, OfflineRecordSyncService.class));
                startService(new Intent(this, OfflineRecordSyncService.class));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in starting offline sync service " + e.getMessage());
        }
    }

    protected void stopServices() {
        stopHealthCheckService();
        stopService(new Intent(this, MemberSyncService.class));
        stopService(new Intent(this, OfflineRecordSyncService.class));
    }

    protected void stopHealthCheckService() {
        ApplicationController.getInstance().cancelHealthCheckTimer(this);
        stopService(new Intent(this, DeviceHealthService.class));
    }
}
