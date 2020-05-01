package com.certify.snap.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.certify.snap.activity.GuideActivity;

/**
 * 作者    ljf
 * 时间    2019/12/26 0026 15:17
 * 文件    DownloadTest
 * 描述
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    private final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    /**
     * 接收广播消息后都会进入 onReceive 方法，然后要做的就是对相应的消息做出相应的处理
     *
     * @param context 表示广播接收器所运行的上下文
     * @param intent  表示广播接收器收到的Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("开机广播--- logs::", intent.getAction());

        /**
         * 如果 系统 启动的消息，则启动 APP 主页活动
         */
        if (ACTION_BOOT.equals(intent.getAction())) {
            Intent guideintent = new Intent(context, GuideActivity.class);
            guideintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(guideintent);
            Log.e("bootcomplete---","开机完毕~启动应用！");
        }
    }

}