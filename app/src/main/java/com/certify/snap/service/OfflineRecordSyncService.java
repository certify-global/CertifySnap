package com.certify.snap.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.certify.snap.api.ApiInterface;
import com.certify.snap.api.RetrofitInstance;
import com.certify.snap.api.request.AccessLogRequest;
import com.certify.snap.api.request.TemperatureRecordRequest;
import com.certify.snap.api.response.AccessLogResponse;
import com.certify.snap.api.response.TemperatureRecordResponse;
import com.certify.snap.async.AsyncTaskExecutorService;
import com.certify.snap.common.Util;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.google.gson.Gson;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfflineRecordSyncService extends Service {

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
        Gson gson = new Gson();
        TemperatureRecordRequest request = gson.fromJson(list.get(i).jsonObj, TemperatureRecordRequest.class);

        primaryid = list.get(i).getPrimaryid();
        ApiInterface apiInterface = RetrofitInstance.getInstance().getApiInterface();
        Call<TemperatureRecordResponse> call = apiInterface.recordUserTemperature(request);
        call.enqueue(new Callback<TemperatureRecordResponse>() {
            @Override
            public void onResponse(Call<TemperatureRecordResponse> call, Response<TemperatureRecordResponse> response) {
                if (response.body() != null) {
                    if (response.body().responseCode == 1) {
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
                    }
                    index++;
                    if (index < datalist.size()) {
                        uploadTemperatureRecordData(datalist, index);
                    } else {
                        if (DatabaseController.getInstance().isOfflineAccessLogExist())
                            findAllOfflineAccessLogRecord();
                        else
                            stopService(new Intent(context, OfflineRecordSyncService.class));
                    }
                }
            }

            @Override
            public void onFailure(Call<TemperatureRecordResponse> call, Throwable t) {
                Log.e(TAG, "Error in uploading record user temperature " + t.getMessage());
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OfflineRecord Service stopped");
        if (taskExecutorService != null) {
            taskExecutorService = null;
        }
    }

    private void uploadAccessLogData(List<AccessLogOfflineRecord> logList, int i) {
        Gson gson = new Gson();
        AccessLogRequest request = gson.fromJson(logList.get(i).jsonObj, AccessLogRequest.class);
        primaryid = logList.get(i).primaryId;
        ApiInterface apiInterface = RetrofitInstance.getInstance().getApiInterface();
        Call<AccessLogResponse> call = apiInterface.sendAccessLog(request);
        call.enqueue(new Callback<AccessLogResponse>() {
            @Override
            public void onResponse(Call<AccessLogResponse> call, Response<AccessLogResponse> response) {
                if (response.body() != null) {
                    if (response.body().responseCode == 1) {
                        AccessLogOfflineRecord firstAccessLogRecord = DatabaseController.getInstance().getFirstOfflineAccessLogRecord();
                        if (firstAccessLogRecord != null) {
                            Log.d(TAG, "OfflineRecord successfully sent with id " + primaryid);
                            DatabaseController.getInstance().deleteOfflineAccessLogRecord(primaryid);
                        } else {
                            stopService(new Intent(context, OfflineRecordSyncService.class));
                        }
                    }
                    logIndex++;
                    if(logIndex < accessLogDatalist.size()) {
                        uploadAccessLogData(accessLogDatalist, logIndex);
                    } else {
                        stopService(new Intent(context, OfflineRecordSyncService.class));
                    }
                }
            }

            @Override
            public void onFailure(Call<AccessLogResponse> call, Throwable t) {
                Log.e(TAG, "Error in sending the access logs " + t.getMessage());
            }
        });
    }

}
