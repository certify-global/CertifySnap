package com.certify.snap.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.util.Log;

import com.certify.callback.MemberIDCallback;
import com.certify.callback.MemberListCallback;
import com.certify.snap.api.response.MemberListData;
import com.certify.snap.api.response.MemberListResponse;
import com.certify.snap.async.AsyncGetMemberData;
import com.certify.snap.async.AsyncTaskExecutorService;
import com.certify.snap.common.ContextUtils;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.controller.DeviceSettingsController;
import com.certify.snap.model.MemberSyncDataModel;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import static android.os.SystemClock.elapsedRealtime;

public class MemberSyncService extends Service implements MemberListCallback, MemberIDCallback {
    protected static final String TAG = MemberSyncService.class.getSimpleName();
    private final static int BACKGROUND_INTERVAL_MINUTES = 240;
    private AlarmManager alarmService;
    private PendingIntent restartServicePendingIntent;
    private SharedPreferences sharedPreferences;
    int totalMemberCount;
    int count = 1;
    private ExecutorService taskExecutorService;
    private int activeMemberCount = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale localeToSwitchTo = new Locale(DeviceSettingsController.getInstance().getLanguageToUpdate());
        ContextWrapper localeUpdatedContext = ContextUtils.updateLocale(newBase, localeToSwitchTo);
        super.attachBaseContext(localeUpdatedContext);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (!MemberSyncDataModel.getInstance().isSyncing()) {
                sharedPreferences = Util.getSharedPreferences(getApplicationContext());
                //sendNotificationEvent(getString(R.string.app_name), "Alert Background MyRabbit", "", getApplicationContext());
                restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, new Intent(this, MemberSyncService.class), PendingIntent.FLAG_ONE_SHOT);
                alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                Calendar cal = Calendar.getInstance();
                long sysTime = elapsedRealtime();
                cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + (BACKGROUND_INTERVAL_MINUTES - (cal.get(Calendar.MINUTE) % BACKGROUND_INTERVAL_MINUTES)));
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long currTime = Util.getCurrentTimeLong();
                if (alarmService != null) {
                    alarmService.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, sysTime + (cal.getTimeInMillis() - currTime), restartServicePendingIntent);
                }
                resetCounters();
                MemberSyncDataModel.getInstance().init(getApplicationContext());
                AsyncTaskExecutorService executorService = new AsyncTaskExecutorService();
                taskExecutorService = executorService.getExecutorService();
                Util.getmemberList(this, getApplicationContext());
            }
        } catch (Exception e) {
            Logger.error(TAG + "onStartCommand(Intent intent, int flags, int startId)", e.getMessage());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alarmService != null && restartServicePendingIntent != null) {
            alarmService.cancel(restartServicePendingIntent);
        }
        if (taskExecutorService != null) {
            taskExecutorService = null;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onJSONObjectListenerMemberList(JSONObject reportInfo, String status, JSONObject req) {
        if (reportInfo != null) {
            Gson gson = new Gson();
            MemberListResponse response = gson.fromJson(String.valueOf(reportInfo), MemberListResponse.class);
            if (response.responseCode != null && response.responseCode.equals("1")) {
                List<MemberListData> memberList = response.memberList;
                totalMemberCount = memberList.size();
                Log.d(TAG, "MemberList Size " + memberList.size());
                MemberSyncDataModel.getInstance().setNumOfRecords(memberList.size());
                for (int i = 0; i < memberList.size(); i++) {
                    if (!MemberSyncDataModel.getInstance().isMemberInactive(memberList.get(i).id, String.valueOf(memberList.get(i).status))) {
                        activeMemberCount++;
                        getMemberID(memberList.get(i).id);
                    }
                }
                if (activeMemberCount > 0) {
                    MemberSyncDataModel.getInstance().setNumOfRecords(activeMemberCount);
                    doSendBroadcast(MemberSyncDataModel.SYNC_START, activeMemberCount, count);
                }
                return;
            }
            Log.e(TAG, "MemberList response = " + response.responseCode);
        }
        Log.e(TAG, "MemberList null response");
    }

    private void getMemberID(String certifyId) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", certifyId);
            if (taskExecutorService != null) {
                new AsyncGetMemberData(obj, this, sharedPreferences.getString(GlobalParameters.URL,
                        EndPoints.prod_url) + EndPoints.GetMemberById, getApplicationContext()).executeOnExecutor(taskExecutorService);
            } else {
                new AsyncGetMemberData(obj, this, sharedPreferences.getString(GlobalParameters.URL,
                        EndPoints.prod_url) + EndPoints.GetMemberById, getApplicationContext()).execute();
            }
        } catch (Exception e) {
            Logger.error(" getMemberID()", e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerMemberID(JSONObject reportInfo, String status, JSONObject req) {
        Logger.debug(TAG, "reportInfo: " + reportInfo + ", req" + req);
        if (reportInfo == null) {
            onMemberIdErrorResponse(req);
            Logger.error(TAG, "onJSONObjectListenerMemberID reportInfo nul");
            return;
        }

            try {
                if (reportInfo.isNull("responseCode")) {
                    onMemberIdErrorResponse(req);
                    return;
                }
                if (reportInfo.getString("responseCode").equals("1")) {

                    JSONArray memberList = reportInfo.getJSONArray("responseData");
                    if (memberList != null) {
                        MemberSyncDataModel.getInstance().createMemberDataAndAdd(memberList);
                        doSendBroadcast(MemberSyncDataModel.SYNC_START, activeMemberCount, count++);
                    }
                } else {
                    onMemberIdErrorResponse(req);
                }
            } catch (JSONException e) {
                Logger.error(TAG, "onJSONObjectListenerMemberID ->" + e.getMessage());
            }

    }

    private void doSendBroadcast(int actionCode, int memberCount, int count) {
        Intent event_snackbar = new Intent("EVENT_SNACKBAR");

        event_snackbar.putExtra("actionCode", actionCode);
        event_snackbar.putExtra("memberCount", memberCount);
        event_snackbar.putExtra("count", count);

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

    private void resetCounters() {
        totalMemberCount = 0;
        count = 1;
        activeMemberCount = 0;
    }
}