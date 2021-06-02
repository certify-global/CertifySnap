package com.certify.snap.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Util;
import com.certify.snap.controller.DatabaseController;

public class ResetOfflineDataReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Util.isOfflineMode(context)) {
            DatabaseController.getInstance().deleteAllOfflineRecord();
        }
        /*if (AppSettings.isScanOnQrEnabled()) {
            DatabaseController.getInstance().resetMemberAccessData();
        }*/
    }
}
