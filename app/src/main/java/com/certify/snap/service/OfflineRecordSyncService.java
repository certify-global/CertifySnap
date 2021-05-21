package com.certify.snap.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.certify.callback.AccessCallback;
import com.certify.callback.RecordTemperatureCallback;
import com.certify.snap.activity.IrCameraActivity;
import com.certify.snap.async.AsyncJSONObjectAccessLog;
import com.certify.snap.async.AsyncRecordUserTemperature;
import com.certify.snap.async.AsyncTaskExecutorService;
import com.certify.snap.common.Constants;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.OfflineRecordTemperatureMembers;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class OfflineRecordSyncService extends Service implements RecordTemperatureCallback, AccessCallback {

    private static final String TAG = OfflineRecordSyncService.class.getSimpleName();
    private List<OfflineRecordTemperatureMembers> datalist = new ArrayList<>();
    private List<AccessLogOfflineRecord> accessLogDatalist = new ArrayList<>();
    public ExecutorService taskExecutorService;
    private SharedPreferences sp;
    private final Object obj = new Object();
    private Context context;
    private Long primaryid;
    private int index = 0;
    private int logIndex = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "OfflineRecord Service started");
        context = this;
        sp = Util.getSharedPreferences(this);
        AsyncTaskExecutorService executorService = new AsyncTaskExecutorService();
        taskExecutorService = executorService.getExecutorService();
        if (DatabaseController.getInstance().isOfflineRecordTempExist()){
            findAllOfflineTempRecord();
        } else if (DatabaseController.getInstance().isOfflineAccessLogExist()){
            findAllOfflineAccessLogRecord();
        } else {
            stopService(new Intent(context, OfflineRecordSyncService.class));
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void findAllOfflineTempRecord() {
        try {
            Observable.create(new ObservableOnSubscribe<List<OfflineRecordTemperatureMembers>>() {
                @Override
                public void subscribe(ObservableEmitter<List<OfflineRecordTemperatureMembers>> emitter) throws Exception {
                    List<OfflineRecordTemperatureMembers> offlineRecordList = DatabaseController.getInstance().findAllOfflineRecord();
                    emitter.onNext(offlineRecordList);
                }
            }).subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<OfflineRecordTemperatureMembers>>() {
                        Disposable disposable;
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable = d;
                        }

                        @Override
                        public void onNext(List<OfflineRecordTemperatureMembers> list) {
                            datalist = list;
                            if (datalist != null && datalist.size() > 0) {
                                uploadTemperatureRecordData(datalist, index);
                            }
                            disposable.dispose();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "Error in adding the member to data model from database");
                        }

                        @Override
                        public void onComplete() {
                            disposable.dispose();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findAllOfflineAccessLogRecord() {
        try {
            Observable.create(new ObservableOnSubscribe<List<AccessLogOfflineRecord>>() {
                @Override
                public void subscribe(ObservableEmitter<List<AccessLogOfflineRecord>> emitter) throws Exception {
                    List<AccessLogOfflineRecord> offlineAccessLogRecordList = DatabaseController.getInstance().findAllOfflineAccessLogRecord();
                    emitter.onNext(offlineAccessLogRecordList);
                }
            }).subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<AccessLogOfflineRecord>>() {
                        Disposable disposable;
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable = d;
                        }

                        @Override
                        public void onNext(List<AccessLogOfflineRecord> list) {
                            accessLogDatalist = list;
                            if (accessLogDatalist != null && accessLogDatalist.size() > 0) {
                                uploadAccessLogData(accessLogDatalist, logIndex);
                            }
                            disposable.dispose();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "Error in adding the member to data model from database");
                        }

                        @Override
                        public void onComplete() {
                            disposable.dispose();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadTemperatureRecordData(List<OfflineRecordTemperatureMembers> list, int i) {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject json = new JSONObject(list.get(i).getJsonObj());
            jsonObject.put("deviceId", json.getString("deviceId"));
            jsonObject.put("temperature", list.get(i).getTemperature());
            jsonObject.put("institutionId", json.getString("institutionId"));
            jsonObject.put("facilityId", json.getString("facilityId"));
            jsonObject.put("locationId", json.getString("locationId"));
            jsonObject.put("deviceTime", list.get(i).getDeviceTime());
            jsonObject.put("trigger", json.getString("trigger"));

            //images

            //jsonObject.put("deviceData", json.getString("deviceData"));
            jsonObject.put("deviceParameters", json.getString("deviceParameters"));
            jsonObject.put("temperatureFormat", json.getString("temperatureFormat"));
            jsonObject.put("exceedThreshold", json.getString("exceedThreshold"));
            jsonObject.put("trqStatus", json.getString("trqStatus"));
            jsonObject.put("qrCodeId", json.getString("qrCodeId"));
            if (json.has("accessId")) {
                jsonObject.put("accessId", json.getString("accessId"));
            }
            if (json.has("allowAccess")) {
                jsonObject.put("allowAccess", json.getString("allowAccess"));
            }
            jsonObject.put("firstName", list.get(i).getFirstName());
            jsonObject.put("lastName", list.get(i).getLastName());
            jsonObject.put("memberId", list.get(i).getMemberId());
            jsonObject.put("maskStatus", json.getString("maskStatus"));
            jsonObject.put("faceScore", json.getString("faceScore"));
            jsonObject.put("faceParameters", json.getString("faceParameters"));
            if (json.has("memberTypeId")) {
                jsonObject.put("memberTypeId", json.getString("memberTypeId"));
            }
            if (json.has("memberTypeName")) {
                jsonObject.put("memberTypeName", json.getString("memberTypeName"));
            }
            if (json.has("networkId")) {
                jsonObject.put("networkId", json.getString("networkId"));
            }

            if (json.has("irTemplate")){
                jsonObject.put("irTemplate", json.getString("irTemplate"));
                jsonObject.put("rgbTemplate", json.getString("rgbTemplate"));
                jsonObject.put("thermalTemplate", json.getString("thermalTemplate"));
            }
            if (list.get(i).getOfflineSync() ==1){
                jsonObject.put("utcOfflineDateTime", list.get(i).getUtcTime());
                jsonObject.put("offlineSync", list.get(i).getOfflineSync());
            }

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
        Log.d(TAG, "OfflineRecord Service stopped");
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
                    OfflineRecordTemperatureMembers firstMember = DatabaseController.getInstance().getFirstOfflineRecord();
                    if (firstMember != null) {
                        Log.d(TAG, "OfflineRecord successfully sent with primaryId " + primaryid);
                        DatabaseController.getInstance().deleteOfflineRecord(primaryid);
                    } else {
                        if (DatabaseController.getInstance().isOfflineAccessLogExist())
                            findAllOfflineAccessLogRecord();
                        else
                            stopService(new Intent(context, OfflineRecordSyncService.class));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "OfflineRecord Exception occurred while querying for first member from db");
                }
            }
            index++;
            if(index < datalist.size()) {
                uploadTemperatureRecordData(datalist, index);
            } else {
                if (DatabaseController.getInstance().isOfflineAccessLogExist())
                    findAllOfflineAccessLogRecord();
                else
                    stopService(new Intent(context, OfflineRecordSyncService.class));
            }
        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListenerTemperature(JSONObject reportInfo, String status, JSONObject req)", e.getMessage());
        }
    }

    private void uploadAccessLogData(List<AccessLogOfflineRecord> logList, int i) {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject json = new JSONObject(logList.get(i).getJsonObj());
            if (json.has("id")) {
                jsonObject.put("id", json.getInt("id"));
            }
            if (json.has("firstName")) {
                jsonObject.put("firstName", json.getString("firstName"));
            }
            if (json.has("lastName")) {
                jsonObject.put("lastName", json.getString("lastName"));
            }
            jsonObject.put("temperature", json.getString("temperature"));
            if (json.has("memberId")) {
                jsonObject.put("memberId", json.getString("memberId"));
            }
            if (json.has("accessId")) {
                jsonObject.put("accessId", json.getString("accessId"));
            }
            jsonObject.put("qrCodeId", json.getString("qrCodeId"));
            jsonObject.put("deviceName", json.getString("deviceName"));
            jsonObject.put("institutionId", json.getString("institutionId"));
            jsonObject.put("facilityId", json.getString("facilityId"));
            jsonObject.put("locationId", json.getString("locationId"));
            jsonObject.put("facilityName", json.getString("facilityName"));
            jsonObject.put("locationName", json.getString("locationName"));

            jsonObject.put("deviceTime", json.getString("deviceTime"));
            if (json.has("timezone")) {
                jsonObject.put("timezone", json.getString("timezone"));
            }
            if (json.has("timezone")) {
                jsonObject.put("timezone", json.getString("timezone"));
            }
            jsonObject.put("deviceData", json.getString("deviceData"));
            jsonObject.put("guid", json.getString("guid"));
            jsonObject.put("faceParameters", json.getString("faceParameters"));
            jsonObject.put("eventType", json.getString("eventType"));
            jsonObject.put("evenStatus", json.getString("evenStatus"));
            jsonObject.put("utcRecordDate", json.getString("utcRecordDate"));
            if (json.has("memberTypeId")) {
                jsonObject.put("memberTypeId", json.getString("memberTypeId"));
            }
            if (json.has("memberTypeName")) {
                jsonObject.put("memberTypeName", json.getString("memberTypeName"));
            }
            if (json.has("networkId")) {
                jsonObject.put("networkId", json.getString("networkId"));
            }
            
            if (logList.get(i).getOfflineSync()==1){
                jsonObject.put("utcOfflineDateTime",json.getString("utcRecordDate"));
                jsonObject.put("offlineSync", logList.get(i).getOfflineSync());
            }

            primaryid = logList.get(i).getPrimaryId();
            if (taskExecutorService != null) {
                new AsyncJSONObjectAccessLog(jsonObject, (AccessCallback) context, sp.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.AccessLogs, context).executeOnExecutor(taskExecutorService);
            } else {
                new AsyncJSONObjectAccessLog(jsonObject, (AccessCallback) context, sp.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.AccessLogs, context).execute();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onJSONObjectListenerAccess(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
            if (reportInfo.getString("responseCode").equals("1")) {
                try {
                    AccessLogOfflineRecord firstAccessLogRecord = DatabaseController.getInstance().getFirstOfflineAccessLogRecord();
                    if (firstAccessLogRecord != null) {
                        Log.d(TAG, "OfflineRecord successfully sent with id " + primaryid);
                        DatabaseController.getInstance().deleteOfflineAccessLogRecord(primaryid);
                    } else {
                        stopService(new Intent(context, OfflineRecordSyncService.class));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "OfflineRecord Exception occurred while querying for first member from db");
                }
            }
            logIndex++;
            if(logIndex < accessLogDatalist.size()) {
                uploadAccessLogData(accessLogDatalist, logIndex);
            } else {
                stopService(new Intent(context, OfflineRecordSyncService.class));
            }
        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListenerTemperature(JSONObject reportInfo, String status, JSONObject req)", e.getMessage());
        }
    }
}
