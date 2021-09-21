package com.certify.snap.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.callback.AccessCallback;
import com.certify.snap.async.AsyncJSONObjectAccessLog;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.UserExportedData;
import com.certify.snap.common.Util;
import com.certify.snap.model.AccessControlModel;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.QrCodeData;
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
    private boolean mEnableWiegandPt = false;
    private int mRelayTime = 0;
    private Timer mRelayTimer;
    private int mWeiganControllerFormat = 26;

    private String mAccessCardID = "";
    private String mAccessIdDb = "";
    private Context context;
    int tapCount = 0;
    private AccessCallbackListener listener;
    private boolean isAccessFaceNotMatch = false;
    private boolean allowAccessValue = true;

    public enum AccessControlScanMode {
        ID_ONLY(1),
        FACE_ONLY(2),
        ID_AND_FACE(3),
        ID_OR_FACE(4);

        private final int id;

        AccessControlScanMode(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }
    }

    public interface AccessCallbackListener {
        void onAccessGranted();

        void onAccessDenied();
    }

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
        this.mReverseRelayMode = !mReverseRelayMode;
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

    public boolean isEnableWiegandPt() {
        return mEnableWiegandPt;
    }

    public void setEnableWiegandPt(boolean mEnableWiegandPt) {
        this.mEnableWiegandPt = mEnableWiegandPt;
    }

    public void setAccessFaceNotMatch(boolean accessFaceNotMatch) {
        isAccessFaceNotMatch = accessFaceNotMatch;
    }

    public boolean isACFaceSearchDisabled() {
        boolean result = false;
        if (AppSettings.isRfidEnabled() && isAccessSignalEnabled() &&
                (AppSettings.getAccessControlScanMode() == AccessControlScanMode.ID_ONLY.getValue())) {
            result = true;
        }
        return result;
    }

    public void setCallbackListener(AccessCallbackListener listener) {
        this.listener = listener;
    }

    public boolean isDoMemberMatch() {
        boolean result = false;
        if ((!mAllowAnonymous && (mEnableRelay || mEnableWeigan))
                || mEnableWiegandPt) {
            result = true;
        }
        return result;
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
            unLockWeiganDoorController();
        }
    }

    private boolean isAccessSignalEnabled() {
        return (mEnableRelay || mEnableWeigan || mEnableWiegandPt);
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
        if (!mEnableWeigan && !mEnableWiegandPt) return;
        //Check if its 34 Bit, 48 Bit or 26 Bit Weigan controller and send signal accordingly
        if (mWeiganControllerFormat == 26) {
            unlock26BitDoorController();
        } else if (mWeiganControllerFormat == 34) {
            unlock34BitDoorController();
        } else if (mWeiganControllerFormat == 48) {
            unlock48BitDoorController();
        }
    }

    public void lockStandAloneDoor() {
        if (mRelayTimer != null) {
            mRelayTimer.cancel();
            mRelayTimer = null;
        }
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
        try {
            int result = PosUtil.getWg26Status(Long.parseLong(cardId));
            if (result != 0) {
                Log.d(TAG, "Error in opening the door");
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception occurred while opening the door");
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

    private void unlock48BitDoorController() {
        if (mAccessCardID.isEmpty()) {
            if (!mAccessIdDb.isEmpty()) {
                sendWg48BitSignal(mAccessIdDb);
            }
            return;
        }
        sendWg48BitSignal(mAccessCardID);
    }

    private void sendWg34BitSignal(String cardId) {
        if (!mEnableWeigan) return;
        try {
            int result = PosUtil.getWg34Status(Long.parseLong(cardId));
            if (result != 0) {
                Log.d(TAG, "Error in opening the door");
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception occurred while opening the door");
        }
    }

    private void sendWg48BitSignal(String cardId) {
        if (!mEnableWeigan) return;
        try {
            String binaryValue = Long.toBinaryString(Long.parseLong(cardId));
            String binaryConvertedValue = String.format("%46s", binaryValue).replace(' ', '0');
            binaryConvertedValue = "1" + binaryConvertedValue + "0";
            int result = PosUtil.sendSpecialWG(binaryConvertedValue);
            if (result != 0) {
                Log.d(TAG, "Error in opening the door");
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception occurred while opening the door");
        }
    }

    public void processUnlockDoor(List<RegisteredMembers> membersList) {
        if (isAccessSignalEnabled()) {
            if (mReverseRelayMode) {
                denyAccess();
                return;
            }
            if (isAllowAnonymous()) {
                allowAccess();
                return;
            }
            if (AppSettings.getPrimaryIdentifier() != CameraController.PrimaryIdentification.NONE.getValue()
                    && !QrCodeController.getInstance().isOnlyQrCodeEnabled()) {
                if ((membersList != null && membersList.size() > 0)) {
                    if (isAccessTimeExpired(membersList.get(0))) {
                        denyAccess();
                    } else {
                        allowAccess();
                    }
                } else {
                    if (isEnableWiegandPt()) {
                        allowAccess();
                    } else {
                        denyAccess();
                    }
                }
            }
        }
    }

    public void processUnlockDoorHigh(List<RegisteredMembers> membersList) {
        if (isAccessSignalEnabled()) {
            if (isAllowAnonymous()) {
                if (isBlockAccessOnHighTempEnabled()) {
                    denyAccess();
                } else {
                    allowAccessOnHighTemp();
                }
                return;
            }
            if (AppSettings.getPrimaryIdentifier() != CameraController.PrimaryIdentification.NONE.getValue()
                    && !QrCodeController.getInstance().isOnlyQrCodeEnabled()) {
                if ((membersList != null && membersList.size() > 0)) {
                    if (isAccessTimeExpired(membersList.get(0)) || isBlockAccessOnHighTempEnabled()) {
                        denyAccess();
                    } else {
                        allowAccess();
                    }
                } else {
                    if (isEnableWiegandPt() && !isBlockAccessOnHighTempEnabled()) {
                        allowAccess();
                    } else {
                        denyAccess();
                    }
                }
            }
        }
    }

    public void sendAccessLogValid(Context context, float temperature, UserExportedData data) {
        if (!Util.isInstitutionIdValid(context)) return;
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        RegisteredMembers registeredMember = new RegisteredMembers();
        if (AppSettings.getPrimaryIdentifier() != CameraController.PrimaryIdentification.NONE.getValue()) {
            try {
                String qrCodeId = "";
                String accessId = "";
                String triggerType = CameraController.getInstance().getTriggerType();
                QrCodeData qrCodeData = CameraController.getInstance().getQrCodeData();
                JSONObject obj = new JSONObject();
                if (triggerType.equals(CameraController.triggerValue.CODEID.toString())) {
                    if (CameraController.getInstance().getQrCodeData() != null) {
                        qrCodeId = CameraController.getInstance().getQrCodeId();
                        obj.put("id", qrCodeData.getUniqueId());
                        obj.put("accessId", qrCodeData.getAccessId());
                        obj.put("firstName", qrCodeData.getFirstName());
                        obj.put("lastName", qrCodeData.getLastName());
                        obj.put("memberId", qrCodeData.getMemberId());
                        obj.put("memberTypeId", qrCodeData.getMemberTypeId());
                        obj.put("memberTypeName", qrCodeData.getMemberTypeName());
                    } else {
                        qrCodeId = CameraController.getInstance().getQrCodeId();
                        obj.put("id", 0);
                        obj.put("accessId", "");
                        obj.put("firstName", "Anonymous");
                        obj.put("lastName", "");
                        obj.put("memberId", "");
                        obj.put("memberTypeId", 0);
                        obj.put("memberTypeName", "");
                    }
                } else if (triggerType.equals(CameraController.triggerValue.ACCESSID.toString())) {
                    if (AccessControlModel.getInstance().getRfidScanMatchedMember() != null) {
                        registeredMember = AccessControlModel.getInstance().getRfidScanMatchedMember();
                    } else if ((AppSettings.getPrimaryIdentifier() == CameraController.PrimaryIdentification.FACE.getValue())
                            || (AppSettings.getPrimaryIdentifier() == CameraController.PrimaryIdentification.FACE_OR_RFID.getValue())) {
                        registeredMember = data.member;
                        registeredMember.setAccessid(mAccessCardID);
                    } else {
                        registeredMember.setFirstname("Anonymous");
                        registeredMember.setAccessid(mAccessCardID);
                    }
                    obj.put("id", 0);
                    obj.put("firstName", registeredMember.getFirstname());
                    obj.put("lastName", registeredMember.getLastname());
                    obj.put("accessId", registeredMember.getAccessid());
                    obj.put("memberId", registeredMember.getMemberid());
                    obj.put("memberTypeId", registeredMember.getMemberType());
                    obj.put("memberTypeName", registeredMember.getMemberTypeName());
                    obj.put("networkId", registeredMember.getNetworkId());
                } else if (triggerType.equals(CameraController.triggerValue.FACE.toString())) {
                    registeredMember = data.member;
                    obj.put("id", 0);
                    obj.put("firstName", registeredMember.getFirstname());
                    obj.put("lastName", registeredMember.getLastname());
                    obj.put("accessId", registeredMember.getAccessid());
                    obj.put("memberId", registeredMember.getMemberid());
                    obj.put("memberTypeId", registeredMember.getMemberType());
                    obj.put("memberTypeName", registeredMember.getMemberTypeName());
                    obj.put("networkId", registeredMember.getNetworkId());
                } else if (triggerType.equals(CameraController.triggerValue.WAVE.toString())) {
                    registeredMember = data.member;
                    if (registeredMember != null) {
                        obj.put("id", 0);
                        obj.put("firstName", registeredMember.getFirstname());
                        obj.put("lastName", registeredMember.getLastname());
                        obj.put("accessId", registeredMember.getAccessid());
                        obj.put("memberId", registeredMember.getMemberid());
                        obj.put("memberTypeId", registeredMember.getMemberType());
                        obj.put("memberTypeName", registeredMember.getMemberTypeName());
                        obj.put("networkId", registeredMember.getNetworkId());
                    }
                }
                obj.put("temperature", temperature);
                obj.put("qrCodeId", qrCodeId);
                obj.put("deviceId", Util.getSNCode(context));
                obj.put("deviceName", sharedPreferences.getString(GlobalParameters.DEVICE_NAME, ""));
                obj.put("institutionId", sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, ""));
                obj.put("facilityId", 0);
                obj.put("locationId", 0);
                obj.put("facilityName", "");
                obj.put("locationName", "");
                obj.put("deviceTime", Util.getMMDDYYYYDate());
                obj.put("timezone", Util.getDateTimeZone());
                obj.put("sourceIP", Util.getLocalIpAddress());
                obj.put("deviceData", Util.MobileDetails(context));
                obj.put("guid", "");
                obj.put("faceParameters", Util.FaceParameters(context, data));
                obj.put("eventType", "");
                obj.put("evenStatus", "");
                obj.put("utcRecordDate", Util.getUTCDate(""));
                obj.put("loggingMode", AppSettings.getAccessControlLogMode());
                obj.put("accessOption", AppSettings.getAccessControlScanMode());
                obj.put("attendanceMode", ApplicationController.getInstance().getTimeAttendance());

                if ((isAccessSignalEnabled() || mAllowAnonymous) && data != null) {
                    obj.put("allowAccess", getAllowAccessValue(data));
                }

                int syncStatus = -1;
                if (Util.isOfflineMode(context)) {
                    syncStatus = 1;
                    saveOfflineAccessLogRecord(obj, syncStatus);
                } else {
                    new AsyncJSONObjectAccessLog(obj, this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.AccessLogs, context).execute();
                }
            } catch (Exception e) {
                Logger.error(TAG + "AccessLogInvalid Error", e.getMessage());
            }
        }
    }

    public void sendAccessLogInvalid(Context context, RegisteredMembers registeredMembers, float temperature, UserExportedData data) {
        if (!Util.isInstitutionIdValid(context)) return;
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        if (AppSettings.getPrimaryIdentifier() != CameraController.PrimaryIdentification.NONE.getValue()) {
            try {
                String qrCodeId = "";
                String accessId = "";
                String triggerType = CameraController.getInstance().getTriggerType();
                QrCodeData qrCodeData = CameraController.getInstance().getQrCodeData();
                JSONObject obj = new JSONObject();
                if (triggerType.equals(CameraController.triggerValue.CODEID.toString())) {
                    qrCodeId = CameraController.getInstance().getQrCodeId();
                    obj.put("id", 0);
                    obj.put("firstName", "Anonymous");
                    obj.put("lastName", "");
                    obj.put("accessId", "");
                    obj.put("memberId", "");
                    obj.put("memberTypeId", "");
                    obj.put("memberTypeName", "");
                } else if (triggerType.equals(CameraController.triggerValue.ACCESSID.toString())) {
                    accessId = mAccessCardID;
                    obj.put("id", 0);
                    obj.put("firstName", registeredMembers.getFirstname());
                    obj.put("lastName", registeredMembers.getLastname());
                    obj.put("accessId", accessId);
                    obj.put("memberId", registeredMembers.getMemberid());
                    obj.put("memberTypeId", registeredMembers.getMemberType());
                    obj.put("memberTypeName", registeredMembers.getMemberTypeName());
                    obj.put("networkId", registeredMembers.getNetworkId());
                } else {
                    obj.put("id", 0);
                    obj.put("firstName", registeredMembers.getFirstname());
                    obj.put("lastName", registeredMembers.getLastname());
                    obj.put("accessId", accessId);
                    obj.put("memberId", registeredMembers.getMemberid());
                    obj.put("memberTypeId", registeredMembers.getMemberType());
                    obj.put("memberTypeName", registeredMembers.getMemberTypeName());
                    obj.put("networkId", registeredMembers.getNetworkId());
                }
                obj.put("temperature", temperature);
                obj.put("qrCodeId", qrCodeId);
                obj.put("deviceId", Util.getSNCode(context));
                obj.put("deviceName", sharedPreferences.getString(GlobalParameters.DEVICE_NAME, ""));
                obj.put("institutionId", sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, ""));
                obj.put("facilityId", 0);
                obj.put("locationId", 0);
                obj.put("facilityName", "");
                obj.put("locationName", "");
                obj.put("deviceTime", Util.getMMDDYYYYDate());
                obj.put("timezone", Util.getDateTimeZone());
                obj.put("sourceIP", Util.getLocalIpAddress());
                obj.put("deviceData", Util.MobileDetails(context));
                obj.put("guid", "");
                obj.put("faceParameters", Util.FaceParameters(context, data));
                obj.put("eventType", "");
                obj.put("evenStatus", "");
                obj.put("utcRecordDate", Util.getUTCDate(""));
                obj.put("loggingMode", AppSettings.getAccessControlLogMode());
                obj.put("accessOption", AppSettings.getAccessControlScanMode());
                obj.put("attendanceMode", AppSettings.getTimeAndAttendance());
                int syncStatus = -1;
                if (Util.isOfflineMode(context)) {
                    syncStatus = 1;
                    saveOfflineAccessLogRecord(obj, syncStatus);
                } else {
                    new AsyncJSONObjectAccessLog(obj, this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.AccessLogs, context).execute();
                }
            } catch (Exception e) {
                Logger.error(TAG + "AccessLogInvalid Error", e.getMessage());
            }
        }
    }

    @Override
    public void onJSONObjectListenerAccess(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                Logger.error(TAG, "onJSONObjectListenerAccess", "Access Log api failed, store is local DB");
                saveOfflineAccessLogRecord(req, 0);
                return;
            }
            if (!reportInfo.getString("responseCode").equals("1")) {
                saveOfflineAccessLogRecord(req, 0);
            }
        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListenerAccess", e.getMessage());
        }
    }

    private void saveOfflineAccessLogRecord(JSONObject obj, int syncStatus) {
        if (!Util.getSharedPreferences(context).getBoolean(GlobalParameters.ONLINE_MODE, true)
                && !AppSettings.isLogOfflineDataEnabled()) {
            return;
        }
        AccessLogOfflineRecord accessLogOfflineRecord = new AccessLogOfflineRecord();
        try {
            accessLogOfflineRecord.setPrimaryId(accessLogOfflineRecord.lastPrimaryId());
            accessLogOfflineRecord.setJsonObj(obj.toString());
            accessLogOfflineRecord.setOfflineSync(syncStatus);
            DatabaseController.getInstance().insertOfflineAccessLog(accessLogOfflineRecord);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getTapCount() {
        return tapCount;
    }

    public void setTapCount(int tapCount) {
        this.tapCount = tapCount;
    }

    private void allowAccess() {
        if (listener != null) {
            listener.onAccessGranted();
        }
        unlockDoor();
    }

    private void allowAccessOnHighTemp() {
        if (listener != null) {
            listener.onAccessGranted();
        }
        unlockDoorOnHighTemp();
    }

    private void denyAccess() {
        allowAccessValue = false;
        if (listener != null) {
            listener.onAccessDenied();
        }
    }

    private boolean isBlockAccessOnHighTempEnabled() {
        return (mNormalRelayMode && mStopRelayOnHighTemp);
    }

    public boolean isAccessTimeExpired(RegisteredMembers member) {
        boolean result = false;
        if (member != null) {
            if ((member.getAccessFromTime() != null && !member.getAccessFromTime().isEmpty())
                    && (member.getAccessToTime() != null && !member.getAccessToTime().isEmpty())) {
                if (!Util.isDateBigger(member.getAccessToTime(), member.getAccessFromTime(), "yyyy-MM-dd'T'HH:mm:ss")) {
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean getAllowAccessValue(UserExportedData data) {
        if (allowAccessValue) {
            if (data.exceedsThreshold) {
                if (mNormalRelayMode && mStopRelayOnHighTemp) {
                    allowAccessValue = false;
                }
            } else {
                if (mReverseRelayMode) {
                    allowAccessValue = false;
                }
            }
        }
        return allowAccessValue;
    }

    public boolean isAccessDenied(RegisteredMembers registeredMembers) {
        boolean result = false;
        if ((AppSettings.getAccessControlScanMode() == AccessControlScanMode.FACE_ONLY.getValue()) ||
                (AppSettings.getAccessControlScanMode() == AccessControlScanMode.ID_OR_FACE.getValue())) {
            if (isAccessTimeExpired(registeredMembers)) {
                result = true;
            }
        } else if (AppSettings.getAccessControlScanMode() == AccessControlScanMode.ID_AND_FACE.getValue()) {
            RegisteredMembers rfidScanMatchMember = AccessControlModel.getInstance().getRfidScanMatchedMember();
            if (rfidScanMatchMember != null) {
                if ((rfidScanMatchMember.primaryid != registeredMembers.primaryid) || isAccessTimeExpired(rfidScanMatchMember)) {
                    setAccessFaceNotMatch(true);
                    result = true;
                }
            } else if (isAccessTimeExpired(registeredMembers)) {
                result = true;
            }
        }
        return result;
    }

    public void clearData() {
        AccessControlModel.getInstance().clearData();
        mAccessCardID = "";
        mAccessIdDb = "";
        tapCount = 0;
        isAccessFaceNotMatch = false;
        allowAccessValue = true;
    }
}
