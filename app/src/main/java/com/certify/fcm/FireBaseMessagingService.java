package com.certify.fcm;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.certify.callback.JSONObjectCallback;
import com.certify.callback.MemberIDCallback;
import com.certify.callback.PushCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.common.Application;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.controller.ApplicationController;
import com.certify.snap.model.MemberSyncDataModel;
import com.certify.snap.service.MemberSyncService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class FireBaseMessagingService extends FirebaseMessagingService implements SettingCallback, MemberIDCallback, JSONObjectCallback, PushCallback {
    private static final String TAG = FireBaseMessagingService.class.getSimpleName();
    private static NotificationChannel mChannel;
    private static NotificationManager notifManager;
    public static final String NOTIFICATION_BROADCAST_ACTION = "com.action.notification.restart";
    public static final String NOTIFICATION_SETTING_BROADCAST_ACTION = "com.action.notification.update.setting";
     SharedPreferences sharedPreferences;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            if (remoteMessage.getNotification() != null) {
                Logger.verbose(TAG, "Remote Body: ", remoteMessage.getNotification().getBody());
               // sendNotification(remoteMessage.getNotification().getBody());
                sendNotification(remoteMessage.getNotification().getBody());
            }

        } catch (Exception e) {
            Log.e(TAG + "onMessageReceived()", e.getMessage());
        }
    }

    @Override
    public void onNewToken(String token) {
        sharedPreferences=Util.getSharedPreferences(this);
        ApplicationController.getInstance().setFcmPushToken(token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        Util.writeString(sharedPreferences,GlobalParameters.Firebase_Token,token);
        Util.activateApplication(this, this);
    }

    //This method is only generating push notification
    @SuppressLint("WrongConstant")
    private void sendNotification(String messageBody) {
        try {
            JSONObject jsonObject = new JSONObject(messageBody);
            sharedPreferences=Util.getSharedPreferences(this);

            String command=jsonObject.isNull("command") ? "":jsonObject.getString("command");
            String certifyId=jsonObject.isNull("certifyId") ? "":jsonObject.getString("certifyId");
            String commandGUID=jsonObject.isNull("commandGUID") ? "":jsonObject.getString("commandGUID");
            String uniqueDeviceId=jsonObject.isNull("uniqueDeviceId") ? "":jsonObject.getString("uniqueDeviceId");
            String eventTypeId=jsonObject.isNull("eventTypeId") ? "":jsonObject.getString("eventTypeId");
            String institutionId=jsonObject.isNull("institutionId") ? "":jsonObject.getString("institutionId");

            if(command.equals("SETTINGS")){
                //Util.getSettings(this,this);
                sendSettingBroadcastMessage();
                Thread.sleep(1000);
                Util.restartApp(this);
            }else if(command.equals("ALLMEMBER")) {
                if (sharedPreferences != null && (sharedPreferences.getBoolean(GlobalParameters.FACIAL_DETECT, true)
                            || sharedPreferences.getBoolean(GlobalParameters.RFID_ENABLE, false))) {
                    if (sharedPreferences.getBoolean(GlobalParameters.SYNC_ONLINE_MEMBERS, false)) {
                            startService(new Intent(this, MemberSyncService.class));
                            Application.StartService(this);
                    }
                }
            }else if(command.equals("MEMBER")){
                String CertifyId=jsonObject.isNull("certifyId") ? "":jsonObject.getString("certifyId");
                Util.getMemberID(this,CertifyId);
            }else if(command.equals("RESET")){
                Util.getPushresponse(this,this,commandGUID,uniqueDeviceId,command,eventTypeId);
                Thread.sleep(1000);
                Util.deleteAppData(this);
                sendBroadcastMessage();
                Util.restartApp(this);
            }else if(command.equals("RESTART")){
                sendBroadcastMessage();
                Util.restartApp(this);
            }else if(command.equals("DEACTIVATE")){
                Util.getPushresponse(this,this,commandGUID,uniqueDeviceId,command,eventTypeId);
                Thread.sleep(1000);
                Util.deleteAppData(this);
                Util.writeBoolean(sharedPreferences,GlobalParameters.ONLINE_MODE,false);
            }else if(command.equals("CHECKHEALTH")){
                Util.getDeviceHealthCheck(FireBaseMessagingService.this, FireBaseMessagingService.this);
            }else if(command.equals("NAVBARON")){
                boolean navigationBar =true;
                sendBroadcast(new Intent(navigationBar ? GlobalParameters.ACTION_SHOW_NAVIGATIONBAR : GlobalParameters.ACTION_HIDE_NAVIGATIONBAR));
                sendBroadcast(new Intent(navigationBar ? GlobalParameters.ACTION_OPEN_STATUSBAR : GlobalParameters.ACTION_CLOSE_STATUSBAR));
                Util.writeBoolean(sharedPreferences,GlobalParameters.NavigationBar,true);
            }else if(command.equals("NAVBAROFF")){
                boolean navigationBar = false;
                sendBroadcast(new Intent(navigationBar ? GlobalParameters.ACTION_SHOW_NAVIGATIONBAR : GlobalParameters.ACTION_HIDE_NAVIGATIONBAR));
                sendBroadcast(new Intent(navigationBar ? GlobalParameters.ACTION_OPEN_STATUSBAR : GlobalParameters.ACTION_CLOSE_STATUSBAR));
                Util.writeBoolean(sharedPreferences,GlobalParameters.NavigationBar,false);
            }
            if (!command.equals("RESET") && !command.equals("DEACTIVATE")) {
                Util.getPushresponse(this,this,commandGUID,uniqueDeviceId,command,eventTypeId);
            }
        } catch (Exception e) {
            Logger.error(TAG + "sendNotification()", e.getMessage());
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

    @Override
    public void onJSONObjectListenerSetting(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
            Util.retrieveSetting(reportInfo, this);
        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListenerSetting()", "Exception while processing API response callback" + e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerMemberID(JSONObject reportInfo, String status, JSONObject req) {
        if (reportInfo == null) {
            Logger.info(TAG, "onJSONObjectListenerMemberID reportInfo null", "");
            return;
        }

        try {
            if (reportInfo.getString("responseCode").equals("1")) {
                JSONArray memberList = reportInfo.getJSONArray("responseData");
                if (memberList != null) {
                    MemberSyncDataModel.getInstance().createMemberDataAndUpdate(memberList);
                }
            }
        } catch (JSONException e) {

        }
    }

    @Override
    public void onJSONObjectListener(String reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
           /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Util.getTokenActivate(reportInfo, status, this, "");
            }*/

        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListener()", "Exception occurred while processing API response callback with Token activate" + e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerPush(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListenerPush()", "Exception occurred while processing API response callback with Push command response api" + e.getMessage());
        }
    }

    private void sendBroadcastMessage() {
        Intent intent = new Intent();
        intent.setAction(NOTIFICATION_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendSettingBroadcastMessage(){
        Intent intent = new Intent();
        intent.setAction(NOTIFICATION_SETTING_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}