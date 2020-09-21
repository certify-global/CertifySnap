package com.certify.snap.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.callback.AccessCallback;
import com.certify.snap.async.AsyncJSONObjectAccessLog;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Constants;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.UserExportedData;
import com.certify.snap.common.Util;
import com.certify.snap.model.AccessControlModel;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.RegisteredMembers;
import com.common.pos.api.util.PosUtil;

import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AccessCardController implements AccessCallback {
    private static final String TAG = AccessCardController.class.getSimpleName();
    private static AccessCardController mInstance = null;
    private boolean mEnableRelay = false;
    private boolean mAllowAnonymous = false;
    private boolean mNormalRelayMode = false;
    private boolean mReverseRelayMode = false;
    private boolean mStopRelayOnHighTemp = false;
    private boolean mEnableWeigan = false;
    private int mRelayTime = 0;
    private Timer mRelayTimer;
    private int mWeiganControllerFormat = 26;

    private String mAccessCardID = "";
    private String mAccessIdDb = "";
    private Context context;

    public static AccessCardController getInstance() {
        if (mInstance == null) {
            mInstance = new AccessCardController();
        }
        return mInstance;
    }

    public void init(Context context) {
        this.context = context;
        AccessControlModel.getInstance().clearData();
        mAccessCardID = "";
        mAccessIdDb = "";
    }

    public boolean isEnableRelay() {
        return mEnableRelay;
    }

    public void setEnableRelay(boolean mEnableRelay) {
        this.mEnableRelay = mEnableRelay;
    }

    public boolean isAllowAnonymous() {
        return mAllowAnonymous;
    }

    public void setAllowAnonymous(boolean mAllowAnonymous) {
        this.mAllowAnonymous = mAllowAnonymous;
    }

    public void setNormalRelayMode(boolean mNormalRelayMode) {
        this.mNormalRelayMode = mNormalRelayMode;
    }

    public void setReverseRelayMode(boolean mReverseRelayMode) {
        this.mReverseRelayMode = mReverseRelayMode;
    }

    public void setStopRelayOnHighTemp(boolean mStopRelayOnHighTemp) {
        this.mStopRelayOnHighTemp = mStopRelayOnHighTemp;
    }

    public boolean isWeigandEnabled() {
        return mEnableWeigan;
    }

    public void setEnableWeigan(boolean mEnableWeigan) {
        this.mEnableWeigan = mEnableWeigan;
    }

    public void setRelayTime(int time) {
        mRelayTime = time;
    }

    public void setWeiganControllerFormat(int value) {
        mWeiganControllerFormat = value;
    }

    public void setAccessCardId(String cardId) {
        mAccessCardID = cardId;
    }

    public String getAccessCardID() {
        return mAccessCardID;
    }

    public void setAccessIdDb(String mAccessIdDb) {
        this.mAccessIdDb = mAccessIdDb;
    }

    public void unlockDoor() {
        if (mNormalRelayMode) {
            unLockStandAloneDoor();
        }
        unLockWeiganDoorController();
    }

    public void unlockDoorOnHighTemp() {
        if (mNormalRelayMode) {
            if (!mStopRelayOnHighTemp) {
                unlockDoor();
            }
            return;
        }
        if (mReverseRelayMode) {
            unLockStandAloneDoor();
        }
    }

    private void startRelayTimer() {
        mRelayTimer = new Timer();
        mRelayTimer.schedule(new TimerTask() {
            public void run() {
                lockStandAloneDoor();
                this.cancel();
            }
        }, mRelayTime * 1000);
    }

    private void unLockStandAloneDoor() {
        if (!mEnableRelay) return;
        int result = PosUtil.setRelayPower(1);
        if (result != 0) {
            Log.d(TAG, "Error in opening the door");
            return;
        }
        startRelayTimer();
    }

    private void unLockWeiganDoorController() {
        if (!mEnableWeigan) return;
        //Check if its 34 Bit or 26 Bit Weigan controller and send signal accordingly
        if (mWeiganControllerFormat == 26) {
            unlock26BitDoorController();
        } else if (mWeiganControllerFormat == 34) {
            unlock34BitDoorController();
        }
    }

    public void lockStandAloneDoor() {
        if (mRelayTimer != null) mRelayTimer.cancel();
        int result = PosUtil.setRelayPower(0);
        if (result != 0) {
            Log.d(TAG, "Error in closing the door");
        }
    }

    private void unlock26BitDoorController() {
        if (mAccessCardID.isEmpty()) {
            if (!mAccessIdDb.isEmpty()) {
                sendWg26BitSignal(mAccessIdDb);
            }
            return;
        }
        sendWg26BitSignal(mAccessCardID);
    }

    private void sendWg26BitSignal(String cardId) {
        if (!mEnableWeigan) return;
        int result = PosUtil.getWg26Status(Long.parseLong(cardId));
        if (result != 0) {
            Log.d(TAG, "Error in opening the door");
        }
    }

    private void unlock34BitDoorController() {
        if (mAccessCardID.isEmpty()) {
            if (!mAccessIdDb.isEmpty()) {
                sendWg34BitSignal(mAccessIdDb);
            }
            return;
        }
        sendWg34BitSignal(mAccessCardID);
    }

    private void sendWg34BitSignal(String cardId) {
        if (!mEnableWeigan) return;
        int result = PosUtil.getWg34Status(Long.parseLong(cardId));
        if (result != 0) {
            Log.d(TAG, "Error in opening the door");
        }
    }

    public void processUnlockDoor(List<RegisteredMembers> membersList) {
        if (AppSettings.isFacialDetect()) {
            if (membersList != null && membersList.size() > 0 ) {
                unlockDoor();
            }
            return;
        }
        unlockDoor();
    }

    public void processUnlockDoorHigh(List<RegisteredMembers> membersList) {
        if (AppSettings.isFacialDetect()) {
            if (membersList != null && membersList.size() > 0 ) {
                unlockDoorOnHighTemp();
            }
            return;
        }
        unlockDoorOnHighTemp();
    }

    public void accessCardLog(Context context, RegisteredMembers registeredMembers, float temperature, UserExportedData data) {
        boolean isFacialEnabled = AppSettings.isFacialDetect();
        if (isFacialEnabled) {
            if (data != null) {
                if ((AccessControlModel.getInstance().getRfidScanMatchedMember() == null) ||
                         data.triggerType.equals(CameraController.triggerValue.FACE.toString())) {
                    registeredMembers = data.member;
                }
            }
        }
        if (registeredMembers == null) {
            registeredMembers = new RegisteredMembers();
        }
        try {
            if (mAllowAnonymous && !isFacialEnabled) {
                registeredMembers.setAccessid(AccessCardController.getInstance().getAccessCardID());
            }
            SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
            if ((sharedPreferences.getBoolean(GlobalParameters.RFID_ENABLE, false) && (mEnableRelay || mEnableWeigan))
                    || isFacialEnabled) {
                JSONObject obj = new JSONObject();
                obj.put("id", 0);
                obj.put("firstName", registeredMembers.getFirstname());
                obj.put("lastName", registeredMembers.getLastname());
                obj.put("temperature", temperature);
                obj.put("memberId", registeredMembers.getMemberid());
                obj.put("accessId", registeredMembers.getAccessid());
                obj.put("qrCodeId", "");
                obj.put("deviceId", Util.getSNCode());
                obj.put("deviceName", sharedPreferences.getString(GlobalParameters.DEVICE_NAME, ""));
                obj.put("institutionId", sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, ""));
                obj.put("facilityId", 0);
                obj.put("locationId", 0);
                obj.put("facilityName", "");
                obj.put("locationName", "");
                obj.put("deviceTime", Util.getMMDDYYYYDate());
                obj.put("sourceIP", Util.getLocalIpAddress());
                obj.put("deviceData", Util.MobileDetails(context));
                obj.put("guid", "");
                obj.put("faceParameters", Util.FaceParameters(context, data));
                obj.put("eventType", "");
                obj.put("evenStatus", "");
                obj.put("utcRecordDate", Util.getUTCDate(""));

                if (Util.isOfflineMode(context)){
                    saveOfflineAccessLogRecord(obj);
                } else {
                    new AsyncJSONObjectAccessLog(obj, this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.AccessLogs, context).execute();
                }
            }
        } catch (Exception e) {
            Logger.error(TAG + "AccessLog Error", e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerAccess(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                Logger.error(TAG,"onJSONObjectListenerAccess","Access Log api failed, store is local DB");
                saveOfflineAccessLogRecord(req);
                return;
            }
            if (!reportInfo.getString("responseCode").equals("1") || (reportInfo.has("responseTimeOut"))) {
                saveOfflineAccessLogRecord(req);
            }
        } catch (Exception e) {
            Logger.error(TAG,"onJSONObjectListenerAccess", e.getMessage());
        }
    }

    private void saveOfflineAccessLogRecord(JSONObject obj) {
        AccessLogOfflineRecord accessLogOfflineRecord = new AccessLogOfflineRecord();
        try {
            accessLogOfflineRecord.setPrimaryId(accessLogOfflineRecord.lastPrimaryId());
            accessLogOfflineRecord.setJsonObj(obj.toString());
            DatabaseController.getInstance().insertOfflineAccessLog(accessLogOfflineRecord);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearData() {
        AccessControlModel.getInstance().clearData();
        mAccessCardID = "";
        mAccessIdDb = "";
    }
}
