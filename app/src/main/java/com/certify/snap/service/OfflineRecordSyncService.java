package com.certify.snap.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.certify.callback.RecordTemperatureCallback;
import com.certify.snap.async.AsyncRecordUserTemperature;
import com.certify.snap.async.AsyncTaskExecutorService;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.controller.DatabaseController;
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
        Log.d(TAG, "OfflineRecord Service started");
        context = this;
        sp = Util.getSharedPreferences(this);
        AsyncTaskExecutorService executorService = new AsyncTaskExecutorService();
        taskExecutorService = executorService.getExecutorService();
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
                            if (datalist != null) {
                                uploadRecordData(datalist, index);
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
        return super.onStartCommand(intent, flags, startId);
    }

    private void uploadRecordData(List<OfflineRecordTemperatureMembers> list, int i) {
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
            jsonObject.put("firstName", list.get(i).getFirstName());
            jsonObject.put("lastName", list.get(i).getLastName());
            jsonObject.put("memberId", list.get(i).getMemberId());
            jsonObject.put("maskStatus", json.getString("maskStatus"));
            jsonObject.put("faceScore", json.getString("faceScore"));
            jsonObject.put("faceParameters", json.getString("faceParameters"));

            jsonObject.put("irTemplate", json.getString("irTemplate"));
            jsonObject.put("rgbTemplate", json.getString("rgbTemplate"));
            jsonObject.put("thermalTemplate", json.getString("thermalTemplate"));
            if (list.get(i).getOfflineSync() ==1){
                jsonObject.put("utcOfflineDateTime", list.get(i).getDeviceTime());
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
                        //OfflineRecordTemperatureMembers firstMember = LitePal.findFirst(OfflineRecordTemperatureMembers.class);
                        if (firstMember != null) {
                            Log.d(TAG, "OfflineRecord successfully sent with primaryId " + primaryid);
                            //LitePal.deleteAll(OfflineRecordTemperatureMembers.class, "primaryid = ?", String.valueOf(primaryid));
                            DatabaseController.getInstance().deleteOfflineRecord(primaryid);
                        } else {
                            stopService(new Intent(context, OfflineRecordSyncService.class));
                        }
                } catch (Exception e) {
                    Log.e(TAG, "OfflineRecord Exception occurred while querying for first member from db");
                }
            }
            index++;
            if(index < datalist.size()) {
                uploadRecordData(datalist, index);
            } else {
                stopService(new Intent(context, OfflineRecordSyncService.class));
            }
        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListenerTemperature(JSONObject reportInfo, String status, JSONObject req)", e.getMessage());
        }
    }
}
