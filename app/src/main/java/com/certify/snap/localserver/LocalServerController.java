package com.certify.snap.localserver;


import android.content.Context;
import android.content.SharedPreferences;

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

public class LocalServerController {
    private static final String TAG = LocalServerController.class.getSimpleName();
    private static LocalServerController mInstance = null;
    List<OfflineRecordTemperatureMembers> tempDataList = new ArrayList<>();
    private List<AccessLogOfflineRecord> accessLogDataList = new ArrayList<>();
    private List<RegisteredMembers> memberDataList = new ArrayList<>();
    private LocalServerCallbackListener listener;

    public interface LocalServerCallbackListener {
        void onGetMemberRequest(List<RegisteredMembers> list);
    }

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
        tempDataList = DatabaseController.getInstance().lastTenOfflineTempRecord();
        /*try {
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
        }*/
    }

    public void findLastTenOfflineAccessLogRecord() {
        accessLogDataList = DatabaseController.getInstance().lastTenOfflineAccessLog();
       /* try {
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
        }*/
    }

    public String getDeviceHealthCheck(Context context) {
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        JSONObject obj = new JSONObject();
        try {
            obj.put("lastUpdateDateTime", Util.getUTCDate(""));
            obj.put("deviceSN", Util.getSNCode());
            obj.put("deviceInfo", Util.MobileDetails(context));
            obj.put("institutionId", sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, ""));
            obj.put("appState", Util.getAppState());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    public List<RegisteredMembers> findAllMembers(){
        memberDataList = DatabaseController.getInstance().findAll();
        return memberDataList;
    }

   /* public void findAllMembers() {
        try {
            Observable.create(new ObservableOnSubscribe<List<RegisteredMembers>>() {
                @Override
                public void subscribe(ObservableEmitter<List<RegisteredMembers>> emitter) throws Exception {
                    List<RegisteredMembers> membersList = DatabaseController.getInstance().findAll();
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
                                if (listener != null){
                                    listener.onGetMemberRequest(memberDataList);
                                }
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
    }*/

    public String convertJsonMemberData(RegisteredMembers member) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("primaryId", member.getPrimaryId());
            obj.put("firstName", member.getFirstname());
            obj.put("lastname", member.getLastname());
            obj.put("email", member.getEmail());
            obj.put("phoneNumber", member.getMobile());
            obj.put("certifyId", member.getUniqueid());
            obj.put("memberId", member.getMemberid());
            obj.put("accessId", member.getAccessid());
            obj.put("faceTemplate", Util.encodeImagePath(member.getImage()));
            obj.put("status", member.getStatus());
            obj.put("memberType", member.getMemberType());
            obj.put("dateTime", member.getDateTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj.toString();
    }

    public String findUpdateMember(JSONObject member) {
        try {
            long primaryId = member.getLong("primaryId");
            List<RegisteredMembers> list = DatabaseController.getInstance().findMember(primaryId);
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
                findAllMembers();
                return "result: OK";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "result: FAIL";
    }

    public String deleteMember(JSONObject member) {
        try {
            long primaryId = member.getLong("primaryId");
            List<RegisteredMembers> list = DatabaseController.getInstance().findMember(primaryId);
            if (list != null && list.size() > 0) {
                DatabaseController.getInstance().deleteMember(primaryId);
                findAllMembers();
                return "result: OK";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "result: FAIL";
    }

    public void setListener(LocalServerCallbackListener listener) {
        this.listener = listener;
    }
}