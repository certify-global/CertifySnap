package com.certify.snap.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.callback.AccessCallback;
import com.certify.snap.activity.SettingActivity;
import com.certify.snap.async.AsyncJSONObjectAccessLog;
import com.certify.snap.async.AsyncJSONObjectPush;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.UserExportedData;
import com.certify.snap.common.Util;
import com.certify.snap.model.AccessControlModel;
import com.certify.snap.model.AppStatusInfo;
import com.certify.snap.model.RegisteredMembers;
import com.common.pos.api.util.PosUtil;

import org.json.JSONObject;

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

    public static AccessCardController getInstance() {
        if (mInstance == null) {
            mInstance = new AccessCardController();
        }
        return mInstance;
    }

    public void init() {
        //mNfcAdapter = M1CardUtils.isNfcAble(context);
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

    public void clearData() {
        AccessControlModel.getInstance().clearData();
        mAccessCardID = "";
        mAccessIdDb = "";
    }

    public void accessCardLog(Context context, RegisteredMembers  registeredMembers,float temperature) {
        try {
            SharedPreferences sharedPreferences = Util.getSharedPreferences(context);

            JSONObject obj = new JSONObject();
            obj.put("id", 0);
            obj.put("FirstName", registeredMembers.getFirstname());
            obj.put("LastName", registeredMembers.getLastname());
            obj.put("Temperature",temperature);
            obj.put("MemberId", registeredMembers.getMemberid());
            obj.put("AccessId", registeredMembers.getAccessid());
            obj.put("QrCodeId", sharedPreferences.getString(GlobalParameters.QRCODE_ID, ""));
            obj.put("DeviceId", Util.getSNCode());
            obj.put("DeviceName", "");
            obj.put("InstitutionId",  sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, ""));
            obj.put("FacilityId", 0);
            obj.put("LocationId", 0);
            obj.put("FacilityName", "");
            obj.put("LocationName", "");
            obj.put("DeviceTime", Util.getMMDDYYYYDate());
            obj.put("SourceIP", Util.getLocalIpAddress());
            obj.put("deviceData", Util.MobileDetails(context));
            obj.put("Guid", "");
            obj.put("FaceParameters", "");
            obj.put("EventType", "");
            obj.put("EvenStatus", "");
            obj.put("UtcRecordDate", Util.getUTCDate(""));


            new AsyncJSONObjectAccessLog(obj,this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.AccessLogs, context).execute();

        } catch (Exception e) {
            Util.switchRgbOrIrActivity(context, true);
            Logger.error(TAG + "accessCardLog", e.getMessage());

        }
    }

    @Override
    public void onJSONObjectListenerAccess(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
                if (reportInfo.isNull("responseCode")) return;
                if (reportInfo.getString("responseCode").equals("1")) {

                } else {
                    Logger.error(TAG,"onJSONObjectListenerAccess","Access Log api failed");
                }

        } catch (Exception e) {
            Logger.error(TAG,"onJSONObjectListenerAccess", e.getMessage());
        }

    }
}
