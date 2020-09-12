package com.certify.snap.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.certify.fcm.FireBaseMessagingService;
import com.certify.snap.R;
import com.certify.snap.bluetooth.bleCommunication.BluetoothLeService;
import com.certify.snap.service.NetworkReceiver;

public abstract class BaseActivity extends Activity {

    private BroadcastReceiver activityReceiver;
    private NetworkReceiver networkReceiver;
    private final String TAG = BaseActivity.this.getClass().getSimpleName();
    protected boolean isDisconnected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityReceiver();
        networkReceiver = new NetworkReceiver();

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(activityReceiver, new IntentFilter(FireBaseMessagingService.NOTIFICATION_BROADCAST_ACTION));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.registerReceiver(networkReceiver, intentFilter);
        this.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (activityReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(activityReceiver);
        }
        if (networkReceiver != null){
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
            }
        };
    }

    /**
     * get broadcast intent-filter
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
                isDisconnected = true;
                Toast.makeText(getBaseContext(), R.string.ble_connect_success, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onReceive: "+ R.string.ble_connect_success);
            }
            // disconnected
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isDisconnected = true;
                Toast.makeText(getBaseContext(), R.string.ble_disconnected, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onReceive: "+ R.string.ble_disconnected);
            }
        }
    };
}
