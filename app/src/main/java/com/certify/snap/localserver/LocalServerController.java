package com.certify.snap.localserver;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.OfflineRecordTemperatureMembers;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.certify.snap.common.Util.getSharedPreferences;

public class LocalServerController {
    private static final String TAG = LocalServerController.class.getSimpleName();
    private static LocalServerController mInstance = null;
    List<OfflineRecordTemperatureMembers> tempDataList = new ArrayList<>();
    private List<AccessLogOfflineRecord> accessLogDatalist = new ArrayList<>();

    public static LocalServerController getInstance() {
        if (mInstance == null) {
            mInstance = new LocalServerController();
        }
        return mInstance;
    }

    public List<OfflineRecordTemperatureMembers> getOfflineTempDataList() {
        return tempDataList;
    }

    public List<AccessLogOfflineRecord> getAccessLogDatalist() {
        return accessLogDatalist;
    }

    public void findLastTenOfflineTempRecord() {
        try {
            Observable.create(new ObservableOnSubscribe<List<OfflineRecordTemperatureMembers>>() {
                @Override
                public void subscribe(ObservableEmitter<List<OfflineRecordTemperatureMembers>> emitter) throws Exception {
                    List<OfflineRecordTemperatureMembers> offlineRecordList = DatabaseController.getInstance().lastTenOfflineTempRecord();
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
                            if (list != null && list.size() > 0) {
                                tempDataList = list;
                            }
                            disposable.dispose();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "Error in adding the temp record to data model from database");
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

    public String convertJsonData(String tempData) {
        String json = "";
        try {
            JSONObject json1 = new JSONObject(tempData);
            json = JSONObject.wrap(json1) + "\n\n";
            json = json.replaceAll(",", ",\n");
            json = json.replaceAll("\\\\/", "/");
            json = json.replaceAll("\\{", "{\n");
            json = json.replaceAll("\\}", "\n}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public void lastTenOfflineAccessLogRecord() {
        try {
            Observable.create(new ObservableOnSubscribe<List<AccessLogOfflineRecord>>() {
                @Override
                public void subscribe(ObservableEmitter<List<AccessLogOfflineRecord>> emitter) throws Exception {
                    List<AccessLogOfflineRecord> offlineAccessLogRecordList = DatabaseController.getInstance().lastTenOfflineAccessLog();
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
                            if (list != null && list.size() > 0)
                            accessLogDatalist = list;

                            disposable.dispose();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "Error in adding the access log to data model from database");
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

    public String getDeviceHealthCheck(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        JSONObject obj = new JSONObject();
        String json = "";
        try {
            obj.put("lastUpdateDateTime", Util.getUTCDate(""));
            obj.put("deviceSN", Util.getSNCode());
            obj.put("deviceInfo", Util.MobileDetails(context));
            obj.put("institutionId", sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, ""));
            obj.put("appState", Util.getAppState());
            JSONObject json1 = new JSONObject(obj.toString());
            json = JSONObject.wrap(json1) + "\n\n";
            json = json.replaceAll(",", ",\n");
            json = json.replaceAll("\\{", "{\n");
            json = json.replaceAll("\\}", "\n}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
