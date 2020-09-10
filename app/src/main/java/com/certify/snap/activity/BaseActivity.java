package com.certify.snap.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.certify.fcm.FireBaseMessagingService;
import com.certify.snap.service.NetworkReceiver;

public abstract class BaseActivity extends Activity {

    private BroadcastReceiver activityReceiver;
    private NetworkReceiver networkReceiver;

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
}
