package com.certify.fcm;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.certify.snap.common.Logger;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class FireBaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FireBaseMessagingService -> ";
    private static NotificationChannel mChannel;
    private static NotificationManager notifManager;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
             String title=remoteMessage.getData().get("Title");
             String body=remoteMessage.getData().get("Body");
             Logger.debug("push",data.toString());
            }

        } catch (Exception e) {
            Logger.error(TAG + "onMessageReceived(RemoteMessage remoteMessage)", e.getMessage());
        }
    }

    //This method is only generating push notification
    @SuppressLint("WrongConstant")
    private void sendNotification(String messageTitle, String messageBody, Map<String, String> row,String click_action) {
        try {

        } catch (Exception e) {
            Logger.error(TAG + "sendNotification(String messageTitle, String messageBody, Map<String, String> row)", e.getMessage());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String title, String channel, String description) {
        try {
            if (notifManager == null) {
                notifManager = (NotificationManager) getSystemService
                        (Context.NOTIFICATION_SERVICE);
            }
            if (mChannel == null) {
                mChannel = new NotificationChannel
                        (channel, title, NotificationManager.IMPORTANCE_HIGH);
                mChannel.setDescription(description);
                mChannel.enableVibration(true);
                notifManager.createNotificationChannel(mChannel);
            }
        } catch (Exception e) {
            Logger.error(TAG + "createNotificationChannel(String title, String channel, String description)", e.getMessage());
        }
    }

    private static int generateRandomNumber(int max, int min) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
}