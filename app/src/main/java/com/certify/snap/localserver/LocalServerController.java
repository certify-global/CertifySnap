package com.certify.snap.localserver;


import android.util.Log;

import com.certify.snap.controller.DatabaseController;
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

public class LocalServerController {
    private static final String TAG = LocalServerController.class.getSimpleName();
    private static LocalServerController mInstance = null;
    List<OfflineRecordTemperatureMembers> dataList = new ArrayList<>();

    public static LocalServerController getInstance() {
        if (mInstance == null) {
            mInstance = new LocalServerController();
        }
        return mInstance;
    }

    public List<OfflineRecordTemperatureMembers> getDataList() {
        return dataList;
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
                                dataList = list;
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

    public String convertJsonData(OfflineRecordTemperatureMembers tempData) {
        String json = "";
        try {
            JSONObject json1 = new JSONObject(tempData.getJsonObj());
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
}
