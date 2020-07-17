package com.certify.snap.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.certify.callback.JSONObjectCallback;
import com.certify.callback.RecordTemperatureCallback;
import com.certify.snap.async.AsyncRecordUserTemperature;
import com.certify.snap.async.AsyncTaskExecutorService;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.model.OfflineRecordTemperatureMembers;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.crud.callback.FindMultiCallback;
import org.litepal.exceptions.LitePalSupportException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class OfflineRecordSyncService extends Service implements RecordTemperatureCallback {

    private static final String TAG = OfflineRecordSyncService.class.getSimpleName();
    private List<OfflineRecordTemperatureMembers> datalist = new ArrayList<>();
    private ExecutorService taskExecutorService;
    private SharedPreferences sp;
    private final Object obj = new Object();
    private Context context;
    private Long primaryid;
    private int index = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        sp = Util.getSharedPreferences(this);
        AsyncTaskExecutorService executorService = new AsyncTaskExecutorService();
        taskExecutorService = executorService.getExecutorService();
        if (LitePal.getDatabase() != null) {
            LitePal.findAllAsync(OfflineRecordTemperatureMembers.class).listen(new FindMultiCallback<OfflineRecordTemperatureMembers>() {
                @Override
                public void onFinish(List<OfflineRecordTemperatureMembers> list) {
                    datalist = list;
                    if (datalist != null) {
                        uploadRecordData(datalist, index);
                    }

                }
            });
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void uploadRecordData(List<OfflineRecordTemperatureMembers> list, int i) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("firstName", list.get(i).getFirstName());
            jsonObject.put("lastName", list.get(i).getLastName());
            jsonObject.put("memberId", list.get(i).getMemberId());
            jsonObject.put("temperature", list.get(i).getTemperature());
            jsonObject.put("deviceTime", list.get(i).getDeviceTime());
            JSONObject json = new JSONObject(list.get(i).getJsonObj());
            jsonObject.put("deviceId", json.getString("deviceId"));
            jsonObject.put("institutionId", json.getString("institutionId"));
            jsonObject.put("facilityId", json.getString("facilityId"));
            jsonObject.put("locationId", json.getString("locationId"));
            jsonObject.put("trigger", json.getString("trigger"));
            jsonObject.put("temperatureFormat", json.getString("temperatureFormat"));
            jsonObject.put("exceedThreshold", json.getString("exceedThreshold"));
            jsonObject.put("deviceParameters", json.getString("deviceParameters"));
            jsonObject.put("trqStatus", json.getString("trqStatus"));
            jsonObject.put("maskStatus", json.getString("maskStatus"));
            jsonObject.put("faceScore", json.getString("faceScore"));
            jsonObject.put("faceParameters", json.getString("faceParameters"));
            jsonObject.put("qrCodeId", json.getString("qrCodeId"));
            try {
                if (json.getString("accessId") != null) {
                    jsonObject.put("accessId", json.getString("accessId"));
                }
            } catch (JSONException e) {
                Log.d(TAG, "JsonException " + e);
            }
            jsonObject.put("deviceData", json.getString("deviceData"));
            primaryid = list.get(i).getPrimaryid();
            if (taskExecutorService != null) {
                new AsyncRecordUserTemperature(jsonObject, (RecordTemperatureCallback) context, sp.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.RecordTemperature, context).executeOnExecutor(taskExecutorService);
            } else {
                new AsyncRecordUserTemperature(jsonObject, (RecordTemperatureCallback) context, sp.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.RecordTemperature, context).execute();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (taskExecutorService != null) {
            taskExecutorService = null;
        }
    }

    @Override
    public void onJSONObjectListenerTemperature(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
            if (reportInfo.getString("responseCode").equals("1")) {
                try {
                    if (LitePal.isExist(OfflineRecordTemperatureMembers.class)) {
                        OfflineRecordTemperatureMembers firstMember = LitePal.findFirst(OfflineRecordTemperatureMembers.class);
                        if (firstMember != null) {
                            LitePal.deleteAll(OfflineRecordTemperatureMembers.class, "primaryid = ?", String.valueOf(primaryid));
                        } else {
                            stopService(new Intent(context, OfflineRecordSyncService.class));
                        }
                    }
                } catch (LitePalSupportException exception) {
                    Log.e(TAG, "Exception occurred while querying for first member from db");
                }
            }
            index++;
            if(index < datalist.size()) {
                uploadRecordData(datalist, index);
            } else {
                stopService(new Intent(context, OfflineRecordSyncService.class));
            }
            if (reportInfo.getString("Message").contains("token expired"))
                Util.getToken((JSONObjectCallback) this, this);

        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListenerTemperature(JSONObject reportInfo, String status, JSONObject req)", e.getMessage());
        }
    }
}
