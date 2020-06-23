package com.certify.fcm;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.certify.callback.JSONObjectCallback;
import com.certify.callback.MemberIDCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.activity.GuideActivity;
import com.certify.snap.activity.IrCameraActivity;
import com.certify.snap.common.Application;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.model.MemberSyncDataModel;
import com.certify.snap.service.MemberSyncService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Random;

public class FireBaseMessagingService extends FirebaseMessagingService implements SettingCallback, MemberIDCallback, JSONObjectCallback {
    private static final String TAG = "FireBaseMessagingService -> ";
    private static NotificationChannel mChannel;
    private static NotificationManager notifManager;
     SharedPreferences sharedPreferences;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            if (remoteMessage.getNotification() != null) {
                sendNotification(remoteMessage.getNotification().getBody());
            }

        } catch (Exception e) {
            Logger.error(TAG + "onMessageReceived()", e.getMessage());
        }
    }

    @Override
    public void onNewToken(String token) {
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

            String command=jsonObject.isNull("Command") ? "":jsonObject.getString("Command");
            String Value1=jsonObject.isNull("Value1") ? "":jsonObject.getString("Value1");


                if(command.equals("SETTINGS")){
                    Util.getSettings(this,this);
                }else if(command.equals("ALLMEMBER")){
                    if ( sharedPreferences.getBoolean(GlobalParameters.FACIAL_DETECT,true)) {
                        startService(new Intent(this, MemberSyncService.class));
                        Application.StartService(this);
                    }
                }else if(command.equals("MEMBER")){
                    String CertifyId=jsonObject.isNull("Value1") ? "":jsonObject.getString("Value1");
                    Util.getMemberID(this,CertifyId);

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
            Logger.error(TAG, "onJSONObjectListenerMemberID reportInfo nul");
            return;
        }

        try {
            if (reportInfo.isNull("responseCode"))  {
                return;
            }
            if (reportInfo.getString("responseCode").equals("1")) {
                JSONArray memberList = reportInfo.getJSONArray("responseData");
                if (memberList != null) {
                    MemberSyncDataModel.getInstance().createMemberDataAndAdd(memberList);
                   // doSendBroadcast("start", activeMemberCount, count++);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Util.getTokenActivate(reportInfo, status, this, "");
            }

        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListener()", "Exception occurred while processing API response callback with Token activate" + e.getMessage());
        }
    }
}