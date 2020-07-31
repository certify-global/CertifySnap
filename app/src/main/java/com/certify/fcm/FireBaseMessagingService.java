package com.certify.fcm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

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
import com.certify.snap.service.DeviceHealthService;
import com.certify.snap.service.MemberSyncService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.Map;
import java.util.Random;

import static androidx.core.app.ActivityCompat.finishAffinity;

public class FireBaseMessagingService extends FirebaseMessagingService implements SettingCallback, MemberIDCallback, JSONObjectCallback {
    private static final String TAG = FireBaseMessagingService.class.getSimpleName();
    private static NotificationChannel mChannel;
    private static NotificationManager notifManager;
     SharedPreferences sharedPreferences;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            if (remoteMessage.getNotification() != null) {
                Logger.debug(TAG, "RemoteBody: ", remoteMessage.getNotification().getBody());
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
            String Value1=jsonObject.isNull("certifyId") ? "":jsonObject.getString("certifyId");


                if(command.equals("SETTINGS")){
                    Util.getSettings(this,this);
                }else if(command.equals("ALLMEMBER")){
                        startService(new Intent(this, MemberSyncService.class));
                        Application.StartService(this);
                }else if(command.equals("MEMBER")){
                    String CertifyId=jsonObject.isNull("certifyId") ? "":jsonObject.getString("certifyId");
                    Util.getMemberID(this,CertifyId);
                }else if(command.equals("RESET")){
                  Util.deleteAppData(this);
                }else if(command.equals("RESTART")){
                    Util.restartApp(this);
                }else if(command.equals("DEACTIVATE")){
                    Util.deleteAppData(this);
                    Util.writeBoolean(sharedPreferences,GlobalParameters.ONLINE_MODE,false);
                }else if(command.equals("CHECKHEALTH")){
                 //   if(!sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE,true)) {
//                        startService(new Intent(this, DeviceHealthService.class));
//                        Application.StartService(this);
                   // this.startService(new Intent(this, DeviceHealthService.class));

                     Util.getDeviceHealthCheck(FireBaseMessagingService.this, FireBaseMessagingService.this);

//                    } else{
//                        Logger.toast(this,"Please check your internet connection");
//                    }
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