package com.certify.snap.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.certify.snap.activity.HomeActivity;
import com.certify.snap.common.Logger;


public class BootBroadcastReceiver extends BroadcastReceiver {
    protected static final String TAG = BootBroadcastReceiver.class.getSimpleName();
    private final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    /**
     * 接收广播消息后都会进入 onReceive 方法，然后要做的就是对相应的消息做出相应的处理
     *
     * @param context 表示广播接收器所运行的上下文
     * @param intent  表示广播接收器收到的Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received Boot completed action" + intent.getAction());

        /**
         * 如果 系统 启动的消息，则启动 APP 主页活动
         */
        if (ACTION_BOOT.equals(intent.getAction())) {
            Intent guideintent = new Intent(context, HomeActivity.class);
            guideintent.putExtra("DEVICE_BOOT", "BootCompleted");
            guideintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(guideintent);
            Logger.info(TAG,"bootcompleted", "After booting up start the application!");
        }
    }

}