package com.certify.snap.localserver;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.RegisteredMembers;

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
    private List<AccessLogOfflineRecord> accessLogDataList = new ArrayList<>();
    private List<RegisteredMembers> memberDataList = new ArrayList<>();

    public static LocalServerController getInstance() {
        if (mInstance == null) {
            mInstance = new LocalServerController();
        }
        return mInstance;
    }

    public List<OfflineRecordTemperatureMembers> getOfflineTempDataList() {
        return tempDataList;
    }

    public List<AccessLogOfflineRecord> getAccessLogDataList() {
        return accessLogDataList;
    }

    public List<RegisteredMembers> getMemberDataList() {
        return memberDataList;
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

    public void findLastTenOfflineAccessLogRecord() {
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
                            accessLogDataList = list;

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

    public void findLastTenMembers() {
        try {
            Observable.create(new ObservableOnSubscribe<List<RegisteredMembers>>() {
                @Override
                public void subscribe(ObservableEmitter<List<RegisteredMembers>> emitter) throws Exception {
                    List<RegisteredMembers> membersList = DatabaseController.getInstance().lastTenMembers();
                    emitter.onNext(membersList);
                }
            }).subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<RegisteredMembers>>() {
                        Disposable disposable;

                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable = d;
                        }

                        @Override
                        public void onNext(List<RegisteredMembers> list) {
                            if (list != null && list.size() > 0) {
                                memberDataList = list;
                            }
                            disposable.dispose();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "Error in adding the members to data model from database");
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

    public String convertJsonMemberData(RegisteredMembers member) {
        String json = "{\n";
        try {
            json += "\"primaryId\": " + member.getPrimaryId()+ ",\n";
            json += "\"firstName\": " + JSONObject.quote(member.getFirstname()) + ",\n";
            json += "\"lastname\": " + JSONObject.quote(member.getLastname()) + ",\n";
            json += "\"status\": " + member.getStatus() + ",\n";
            json += "\"phoneNumber\": " + JSONObject.quote(member.getMobile()) + ",\n";
            json += "\"certifyId\": " + member.getUniqueid() + ",\n";
            json += "\"faceTemplate\": " + JSONObject.quote(member.getImage()) + ",\n";
            json += "\"memberId\": " + JSONObject.quote(member.getMemberid()) + ",\n";
            json += "\"email\": " + JSONObject.quote(member.getEmail()) + ",\n";
            json += "\"accessId\": " + JSONObject.quote(member.getAccessid()) + ",\n";
            json += "\"memberType\": " + JSONObject.quote(member.getMemberType()) + ",\n";
            json += "\"dateTime\": " + JSONObject.quote(member.getDateTime()) + "\n";
            json = json.replaceAll("\\\\/", "/");

            json += "}\n\n";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    public String findUpdateMember(JSONObject member) {
        try {
            String uniqueId = member.getString("certifyId");
            List<RegisteredMembers> list = DatabaseController.getInstance().isUniqueIdExit(uniqueId);
            if (list != null && list.size() > 0) {
                RegisteredMembers Members = list.get(0);
                Members.setFirstname(member.getString("firstName"));
                Members.setLastname(member.getString("lastname"));
                Members.setMobile(member.getString("phoneNumber"));
                Members.setMemberid(member.getString("memberId"));
                Members.setEmail(member.getString("email"));
                Members.setAccessid(member.getString("accessId"));
                Members.setUniqueid(member.getString("certifyId"));
                Members.setStatus(member.getString("status"));
                Members.setImage(member.getString("faceTemplate"));
                Members.setPrimaryId(member.getLong("primaryId"));
                Members.setDateTime(Util.currentDate());
                DatabaseController.getInstance().updateMember(Members);
                findLastTenMembers();
                return "result: OK";
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "result: FAIL";
        }
        return "result: FAIL";
    }
}
