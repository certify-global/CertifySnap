package com.certify.snap.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;

import com.certify.callback.JSONObjectCallback;
import com.certify.callback.MemberIDCallback;
import com.certify.callback.MemberListCallback;
import com.certify.snap.async.AsyncGetMemberData;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.MemberUtilData;
import com.certify.snap.common.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import static android.os.SystemClock.elapsedRealtime;

public class MemberSyncService extends Service implements MemberListCallback, MemberIDCallback {
    protected static final String LOG = "MemberSyncService - ";
    private final static int BACKGROUND_INTERVAL_10_MINUTES = 60;
    private AlarmManager alarmService;
    private PendingIntent restartServicePendingIntent;
    private SharedPreferences sharedPreferences;
    ArrayList<String> certifyIDList=new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
        try {
            sharedPreferences = Util.getSharedPreferences(this);
            //sendNotificationEvent(getString(R.string.app_name), "Alert Background MyRabbit", "", getApplicationContext());
            restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, new Intent(this, MemberSyncService.class), PendingIntent.FLAG_ONE_SHOT);
            alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Calendar cal = Calendar.getInstance();
            long sysTime = elapsedRealtime();
            cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + (BACKGROUND_INTERVAL_10_MINUTES - (cal.get(Calendar.MINUTE) % BACKGROUND_INTERVAL_10_MINUTES)));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long currTime = Util.getCurrentTimeLong();
            if (alarmService != null)
                alarmService.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, sysTime + (cal.getTimeInMillis() - currTime), restartServicePendingIntent);
            Util.getmemberList(this, this);
        } catch (Exception e) {
            Logger.error(LOG + "onStartCommand(Intent intent, int flags, int startId)", e.getMessage());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alarmService != null && restartServicePendingIntent != null) {
            alarmService.cancel(restartServicePendingIntent);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onJSONObjectListenerMemberList(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo.isNull("responseCode")) return;
            if (reportInfo.getString("responseCode").equals("1")) {
                JSONArray memberList = reportInfo.getJSONArray("responseData");
                //  totalMemberCount = memberList.length();
                Logger.debug("length",""+memberList.length());

                for (int i = 0; i < memberList.length(); i++) {
                    JSONObject c = memberList.getJSONObject(i);

                    String certifyId = c.getString("id");
                    // String memberId = c.getString("memberId");
                    // String accessId = c.getString("accessId");

                    certifyIDList.add(certifyId);



                }
                if(certifyIDList.size()>0){
                    getMemberID(certifyIDList.get(0));

                }

            } else {
                Logger.toast(this, "Something went wrong please try again");
            }
        } catch (Exception e) {
            Logger.error(LOG + "onJSONObjectListenerMemberList(String report, String status, JSONObject req)", e.getMessage());

        }
    }

    private void getMemberID(String certifyId) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", certifyId);
            new AsyncGetMemberData(obj, this, sharedPreferences.getString(GlobalParameters.URL,
                    EndPoints.prod_url) + EndPoints.GetMemberById, this).execute();
        }catch (Exception e){
            Logger.error(" getMemberID()",e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerMemberID(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo.isNull("responseCode")) return;
            if (reportInfo.getString("responseCode").equals("1")) {
                JSONArray memberList = reportInfo.getJSONArray("responseData");
                Logger.debug("length ID",""+memberList.length());


                MemberUtilData.MemberData(memberList,MemberSyncService.this);
                if(certifyIDList.size()>0){
                    certifyIDList.remove(0);
                    if(certifyIDList.size()>0){
                        getMemberID(certifyIDList.get(0));
                    }
                }
            }
        } catch (JSONException e) {

        }
    }
}