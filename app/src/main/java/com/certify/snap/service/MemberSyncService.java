package com.certify.snap.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.certify.callback.MemberIDCallback;
import com.certify.callback.MemberListCallback;
import com.certify.snap.async.AsyncGetMemberData;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.model.MemberSyncDataModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import static android.os.SystemClock.elapsedRealtime;

public class MemberSyncService extends Service implements MemberListCallback, MemberIDCallback {
    protected static final String LOG = "MemberSyncService - ";
    private final static int BACKGROUND_INTERVAL_MINUTES = 240;
    private AlarmManager alarmService;
    private PendingIntent restartServicePendingIntent;
    private SharedPreferences sharedPreferences;
    int totalMemberCount;
    int count;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            sharedPreferences = Util.getSharedPreferences(this);
            //sendNotificationEvent(getString(R.string.app_name), "Alert Background MyRabbit", "", getApplicationContext());
            restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, new Intent(this, MemberSyncService.class), PendingIntent.FLAG_ONE_SHOT);
            alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Calendar cal = Calendar.getInstance();
            long sysTime = elapsedRealtime();
            cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + (BACKGROUND_INTERVAL_MINUTES - (cal.get(Calendar.MINUTE) % BACKGROUND_INTERVAL_MINUTES)));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long currTime = Util.getCurrentTimeLong();
            if (alarmService != null)
                alarmService.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, sysTime + (cal.getTimeInMillis() - currTime), restartServicePendingIntent);
            MemberSyncDataModel.getInstance().init(this);
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
            count=1;
            if (reportInfo.isNull("responseCode")) return;
            if (reportInfo.getString("responseCode").equals("1")) {
                JSONArray memberList = reportInfo.getJSONArray("responseData");
                totalMemberCount = memberList.length();
                Logger.debug("length",""+memberList.length());

                MemberSyncDataModel.getInstance().setNumOfRecords(memberList.length());
                for (int i = 0; i < memberList.length(); i++) {
                    JSONObject c = memberList.getJSONObject(i);
                    getMemberID(c.getString("id"));
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
                    EndPoints.prod_url) + EndPoints.GetMemberById, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Logger.error(" getMemberID()",e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerMemberID(JSONObject reportInfo, String status, JSONObject req) {
        if (reportInfo == null) {
            Logger.error(LOG, "onJSONObjectListenerMemberID reportInfo nul");
            return;
        }

        try {
            if (reportInfo.isNull("responseCode"))  {
                onMemberIdErrorResponse(req);
                return;
            }
            if (reportInfo.getString("responseCode").equals("1")) {
                JSONArray memberList = reportInfo.getJSONArray("responseData");
                if (memberList != null) {
                    MemberSyncDataModel.getInstance().createMemberDataAndAdd(memberList);
                    doSendBroadcast("start", totalMemberCount, count++);
                }
            } else {
                onMemberIdErrorResponse(req);
            }
        } catch (JSONException e) {

        }
    }

    private void doSendBroadcast(String message,int memberCount,int count) {
        Intent event_snackbar = new Intent("EVENT_SNACKBAR");

        if (!TextUtils.isEmpty(message))
            event_snackbar.putExtra("message",message);
        event_snackbar.putExtra("memberCount",memberCount);
        event_snackbar.putExtra("count",count);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(event_snackbar);
    }

    private void onMemberIdErrorResponse(JSONObject req) {
        if (req != null) {
            try {
                String certifyId = req.getString("id");
                if (!certifyId.isEmpty()) {
                    getMemberID(certifyId);
                }
            } catch (JSONException e) {
                Log.e("MemberSyncService", "Error in fetching the certify id from Json");
            }
        }
    }
}