package com.certify.snap.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.certify.snap.activity.GuideActivity;


public class BootBroadcastReceiver extends BroadcastReceiver {
    private final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    /**
     *
     * @param context
     * @param intent
     * */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("-- logs::", intent.getAction());

        /**
         */
        if (ACTION_BOOT.equals(intent.getAction())) {
            Intent guideintent = new Intent(context, GuideActivity.class);
            guideintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(guideintent);

        }
    }

}