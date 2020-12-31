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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.certify.fcm.FireBaseMessagingService;
import com.certify.snap.R;
import com.certify.snap.bluetooth.bleCommunication.BluetoothLeService;
import com.certify.snap.common.ContextUtils;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.ApplicationController;
import com.certify.snap.controller.CameraController;
import com.certify.snap.service.NetworkReceiver;

import java.util.Locale;

public abstract class BaseActivity extends Activity {

    private BroadcastReceiver activityReceiver;
    private NetworkReceiver networkReceiver;
    private final String TAG = BaseActivity.this.getClass().getSimpleName();
    protected boolean isDisconnected = false;
    private IntentFilter activityReceiverFilter;

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale localeToSwitchTo;
        if (HomeActivity.mSelectLanguage) {
            localeToSwitchTo = new Locale("es");
        } else {
            localeToSwitchTo = new Locale("en");
        }
        ContextWrapper localeUpdatedContext = ContextUtils.updateLocale(newBase, localeToSwitchTo);
        super.attachBaseContext(localeUpdatedContext);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityReceiver();
        networkReceiver = new NetworkReceiver();
        activityReceiverFilter = new IntentFilter();
        activityReceiverFilter.addAction(FireBaseMessagingService.NOTIFICATION_BROADCAST_ACTION);
        activityReceiverFilter.addAction(FireBaseMessagingService.NOTIFICATION_SETTING_BROADCAST_ACTION);
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
                    Toast.makeText(BaseActivity.this, "Updating settings, App will restart", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "App encountered a problem, will restart", Toast.LENGTH_SHORT).show();
                finishAffinity();
                restartApplication();
            }
        });
    }
}
