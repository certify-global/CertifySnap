package com.certify.snap.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.snap.api.ApiInterface;
import com.certify.snap.api.RetrofitInstance;
import com.certify.snap.api.request.AccessLogRequest;
import com.certify.snap.api.request.GetMemberDetailsGuidRequest;
import com.certify.snap.api.response.AccessLogResponse;
import com.certify.snap.api.response.GetMemberGuidResponse;
import com.certify.snap.api.response.MemberData;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.UserExportedData;
import com.certify.snap.common.Util;
import com.certify.snap.model.AccessControlModel;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.QrCodeData;
import com.certify.snap.model.RegisteredMembers;
import com.common.pos.api.util.PosUtil;
import com.google.gson.Gson;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccessCardController {
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
    private int checkInResponseCode = -1;
    private RegisteredMembers checkedInOutMember = null;
    private AccessLogRequest accessLogRequest = null;

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

    public enum AccessCheckInOutStatus {
        RESPONSE_CODE_SUCCESS(1),
        RESPONSE_CODE_FAILED(0),
        RESPONSE_CODE_ALREADY(103);

        private final int statusId;

        AccessCheckInOutStatus(int statusId) {
            this.statusId = statusId;
        }

        public int getValue() {
            return statusId;
        }
    }

    public interface AccessCallbackListener {
        void onAccessGranted();

        void onAccessDenied();

        void onCheckInOutStatus();

        void onMemberDetailsReceived(MemberData memberData);
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

    public int getCheckInResponseCode() {
        return checkInResponseCode;
    }

    public void setCheckInResponseCode(int responseCode) {
        if (responseCode == AccessCheckInOutStatus.RESPONSE_CODE_ALREADY.getValue() ||
                responseCode == AccessCheckInOutStatus.RESPONSE_CODE_FAILED.getValue()) {
            // ApplicationController.getInstance().setTimeAttendance(0);
        }
        this.checkInResponseCode = responseCode;
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
                accessLogRequest = new AccessLogRequest();
                String triggerType = CameraController.getInstance().getTriggerType();
                QrCodeData qrCodeData = CameraController.getInstance().getQrCodeData();
                if (triggerType.equals(CameraController.triggerValue.CODEID.toString())) {
                    if (CameraController.getInstance().getQrCodeData() != null) {
                        qrCodeId = CameraController.getInstance().getQrCodeId();
                        accessLogRequest.id = qrCodeData.getUniqueId();
                        accessLogRequest.accessId = qrCodeData.getAccessId();
                        accessLogRequest.firstName = qrCodeData.getFirstName();
                        accessLogRequest.lastName = qrCodeData.getLastName();
                        accessLogRequest.memberId = qrCodeData.getMemberId();
                        accessLogRequest.memberTypeId = String.valueOf(qrCodeData.getMemberTypeId());
                        accessLogRequest.memberTypeName = qrCodeData.getMemberTypeName();
                    } else {
                        qrCodeId = CameraController.getInstance().getQrCodeId();
                        accessLogRequest.id = "0";
                        accessLogRequest.accessId = "";
                        if (sharedPreferences.getString(GlobalParameters.anonymousFirstName, "").isEmpty()) {
                            accessLogRequest.firstName = "Anonymous";
                            accessLogRequest.lastName = "";
                        } else {
                            accessLogRequest.firstName = sharedPreferences.getString(GlobalParameters.anonymousFirstName, "");
                            accessLogRequest.lastName = sharedPreferences.getString(GlobalParameters.anonymousLastName, "");
                        }
                        accessLogRequest.memberId = "";
                        accessLogRequest.memberTypeId = "";
                        accessLogRequest.memberTypeName = "";
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
                    accessLogRequest.id = "0";
                    accessLogRequest.accessId = registeredMember.getAccessid();
                    accessLogRequest.firstName = registeredMember.getFirstname();
                    accessLogRequest.lastName = registeredMember.getLastname();
                    accessLogRequest.memberId = registeredMember.getMemberid();
                    accessLogRequest.memberTypeId = registeredMember.getMemberType();
                    accessLogRequest.memberTypeName = registeredMember.getMemberTypeName();
                    accessLogRequest.networkId = registeredMember.getNetworkId();
                } else if (triggerType.equals(CameraController.triggerValue.FACE.toString())) {
                    registeredMember = data.member;
                    if (registeredMember != null) {
                        accessLogRequest.id = "0";
                        accessLogRequest.accessId = registeredMember.getAccessid();
                        accessLogRequest.firstName = registeredMember.getFirstname();
                        accessLogRequest.lastName = registeredMember.getLastname();
                        accessLogRequest.memberId = registeredMember.getMemberid();
                        accessLogRequest.memberTypeId = registeredMember.getMemberType();
                        accessLogRequest.memberTypeName = registeredMember.getMemberTypeName();
                        accessLogRequest.networkId = registeredMember.getNetworkId();
                    } else {
                        accessLogRequest.firstName = "Anonymous";
                    }
                } else if (triggerType.equals(CameraController.triggerValue.WAVE.toString())) {
                    registeredMember = data.member;
                    if (registeredMember != null) {
                        accessLogRequest.id = "0";
                        accessLogRequest.accessId = registeredMember.getAccessid();
                        accessLogRequest.firstName = registeredMember.getFirstname();
                        accessLogRequest.lastName = registeredMember.getLastname();
                        accessLogRequest.memberId = registeredMember.getMemberid();
                        accessLogRequest.memberTypeId = registeredMember.getMemberType();
                        accessLogRequest.memberTypeName = registeredMember.getMemberTypeName();
                        accessLogRequest.networkId = registeredMember.getNetworkId();
                    } else {
                        accessLogRequest.firstName = "Anonymous";
                    }
                } else {
                    accessLogRequest.firstName = "Anonymous";
                }
                accessLogRequest.temperature = temperature;
                accessLogRequest.qrCodeId = qrCodeId;
                accessLogRequest.deviceId = Util.getSNCode(context);
                accessLogRequest.deviceName = sharedPreferences.getString(GlobalParameters.DEVICE_NAME, "");
                accessLogRequest.institutionId = sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, "");
                accessLogRequest.facilityId = 0;
                accessLogRequest.locationId = 0;
                accessLogRequest.facilityName = "";
                accessLogRequest.locationName = "";
                accessLogRequest.deviceTime = Util.getMMDDYYYYDate();
                accessLogRequest.timezone = Util.getDateTimeZone();
                accessLogRequest.sourceIP = Util.getLocalIpAddress();
                accessLogRequest.deviceData = Util.getDeviceInfo(context);
                accessLogRequest.guid = "";
                accessLogRequest.faceParameters = Util.FaceParameters(context, data);
                accessLogRequest.eventType = "";
                accessLogRequest.evenStatus = "";
                accessLogRequest.utcRecordDate = Util.getUTCDate("");
                accessLogRequest.loggingMode = AppSettings.getAccessControlLogMode();
                accessLogRequest.accessOption = AppSettings.getAccessControlScanMode();
                if (AppSettings.getTimeAndAttendance() == 1) {
                    accessLogRequest.attendanceMode = ApplicationController.getInstance().getTimeAttendance();
                }
                if ((isAccessSignalEnabled() || mAllowAnonymous) && data != null) {
                    accessLogRequest.allowAccess = getAllowAccessValue(data);
                }
                int syncStatus = -1;
                if (Util.isOfflineMode(context)) {
                    if ((AppSettings.getTimeAndAttendance() == 1) && listener != null) {
                        listener.onCheckInOutStatus();
                        setCheckInResponseCode(AccessCheckInOutStatus.RESPONSE_CODE_FAILED.getValue());
                    }
                    syncStatus = 1;
                    accessLogRequest.offlineSync = syncStatus;
                    accessLogRequest.utcOfflineDateTime = accessLogRequest.utcRecordDate;
                    saveOfflineAccessLogRecord(context, accessLogRequest, data, syncStatus);
                } else {
                    ApiInterface apiInterface = RetrofitInstance.getInstance().getApiInterface();
                    Call<AccessLogResponse> call = apiInterface.sendAccessLog(accessLogRequest);
                    call.enqueue(new Callback<AccessLogResponse>() {
                        @Override
                        public void onResponse(Call<AccessLogResponse> call, Response<AccessLogResponse> response) {
                            if (response.body() != null) {
                                if (response.body().responseCode == 1) {
                                    Log.d(TAG, "Access logs response success");
                                    if (AppSettings.getTimeAndAttendance() == 1) {
                                        if (response.body().responseSubCode != null && response.body().responseSubCode.equals("103")) {
                                            // setCheckInResponseCode(AccessCheckInOutStatus.RESPONSE_CODE_ALREADY.getValue());
                                            if (listener != null) listener.onCheckInOutStatus();
                                        } else {
                                            // setCheckInResponseCode(AccessCheckInOutStatus.RESPONSE_CODE_SUCCESS.getValue());
                                            if (listener != null) {
                                                listener.onCheckInOutStatus();
                                            }
                                        }
                                    }
                                } else if (response.body().responseCode == 0) {
                                    if (response.body().responseSubCode != null && response.body().responseSubCode.equals("102")) {
                                        if (AppSettings.getTimeAndAttendance() == 1) {
                                            if (listener != null) {
                                                listener.onCheckInOutStatus();
                                            }
                                        }
                                    }
                                }
                                return;
                            }
                            if (AppSettings.getTimeAndAttendance() == 1) {
                                setCheckInResponseCode(AccessCheckInOutStatus.RESPONSE_CODE_FAILED.getValue());
                                if (listener != null) {
                                    listener.onCheckInOutStatus();
                                }
                            }
                            accessLogRequest.offlineSync = 1;
                            accessLogRequest.utcOfflineDateTime = accessLogRequest.utcRecordDate;
                            saveOfflineAccessLogRecord(context, accessLogRequest, data, 1);
                        }

                        @Override
                        public void onFailure(Call<AccessLogResponse> call, Throwable t) {
                            Log.d(TAG, "Access logs response error " + t.getMessage());
                            if (AppSettings.getTimeAndAttendance() == 1) {
                                setCheckInResponseCode(AccessCheckInOutStatus.RESPONSE_CODE_FAILED.getValue());
                                if (listener != null) {
                                    listener.onCheckInOutStatus();
                                }
                            }
                            accessLogRequest.offlineSync = 1;
                            accessLogRequest.utcOfflineDateTime = accessLogRequest.utcRecordDate;
                            saveOfflineAccessLogRecord(context, accessLogRequest, data, 0);
                        }
                    });
                }
            } catch (Exception e) {
                Logger.error(TAG + " AccessLogInvalid Error ", e.getMessage());
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
                accessLogRequest = new AccessLogRequest();
                if (triggerType.equals(CameraController.triggerValue.CODEID.toString())) {
                    qrCodeId = CameraController.getInstance().getQrCodeId();
                    accessLogRequest.id = "0";
                    accessLogRequest.accessId = "";
                    accessLogRequest.firstName = "Anonymous";
                    accessLogRequest.lastName = "";
                    accessLogRequest.memberId = "";
                    accessLogRequest.memberTypeId = "";
                    accessLogRequest.memberTypeName = "";
                } else if (triggerType.equals(CameraController.triggerValue.ACCESSID.toString())) {
                    accessId = mAccessCardID;
                    accessLogRequest.id = "0";
                    accessLogRequest.accessId = accessId;
                    accessLogRequest.firstName = registeredMembers.getFirstname();
                    accessLogRequest.lastName = registeredMembers.getLastname();
                    accessLogRequest.memberId = registeredMembers.getMemberid();
                    accessLogRequest.memberTypeId = registeredMembers.getMemberType();
                    accessLogRequest.memberTypeName = registeredMembers.getMemberTypeName();
                    accessLogRequest.networkId = registeredMembers.getNetworkId();
                } else {
                    accessLogRequest.id = "0";
                    accessLogRequest.accessId = accessId;
                    accessLogRequest.firstName = registeredMembers.getFirstname();
                    accessLogRequest.lastName = registeredMembers.getLastname();
                    accessLogRequest.memberId = registeredMembers.getMemberid();
                    accessLogRequest.memberTypeId = registeredMembers.getMemberType();
                    accessLogRequest.memberTypeName = registeredMembers.getMemberTypeName();
                    accessLogRequest.networkId = registeredMembers.getNetworkId();
                }
                accessLogRequest.temperature = temperature;
                accessLogRequest.qrCodeId = qrCodeId;
                accessLogRequest.deviceId = Util.getSNCode(context);
                accessLogRequest.deviceName = sharedPreferences.getString(GlobalParameters.DEVICE_NAME, "");
                accessLogRequest.institutionId = sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, "");
                accessLogRequest.facilityId = 0;
                accessLogRequest.locationId = 0;
                accessLogRequest.facilityName = "";
                accessLogRequest.locationName = "";
                accessLogRequest.deviceTime = Util.getMMDDYYYYDate();
                accessLogRequest.timezone = Util.getDateTimeZone();
                accessLogRequest.sourceIP = Util.getLocalIpAddress();
                accessLogRequest.deviceData = Util.getDeviceInfo(context);
                accessLogRequest.guid = "";
                accessLogRequest.faceParameters = Util.FaceParameters(context, data);
                accessLogRequest.eventType = "";
                accessLogRequest.evenStatus = "";
                accessLogRequest.utcRecordDate = Util.getUTCDate("");
                accessLogRequest.loggingMode = AppSettings.getAccessControlLogMode();
                accessLogRequest.accessOption = AppSettings.getAccessControlScanMode();

                int syncStatus = -1;
                if (Util.isOfflineMode(context)) {
                    syncStatus = 1;
                    accessLogRequest.offlineSync = syncStatus;
                    accessLogRequest.utcOfflineDateTime = accessLogRequest.utcRecordDate;
                    saveOfflineAccessLogRecord(context, accessLogRequest, data, syncStatus);
                } else {
                    ApiInterface apiInterface = RetrofitInstance.getInstance().getApiInterface();
                    Call<AccessLogResponse> call = apiInterface.sendAccessLog(accessLogRequest);
                    call.enqueue(new Callback<AccessLogResponse>() {
                        @Override
                        public void onResponse(Call<AccessLogResponse> call, Response<AccessLogResponse> response) {
                            if (response.body() != null) {
                                if (response.body().responseCode != 1) {
                                    saveOfflineAccessLogRecord(context, accessLogRequest, data, 0);
                                }
                            } else {
                                saveOfflineAccessLogRecord(context, accessLogRequest, data, 0);
                            }
                        }

                        @Override
                        public void onFailure(Call<AccessLogResponse> call, Throwable t) {
                            Log.e(TAG, "Error in sending the access logs " + t.getMessage());
                            saveOfflineAccessLogRecord(context, accessLogRequest, data, 0);
                        }
                    });
                }
            } catch (Exception e) {
                Logger.error(TAG + "AccessLogInvalid Error", e.getMessage());
            }
        }
    }

    private void saveOfflineAccessLogRecord(Context context, AccessLogRequest obj, UserExportedData data, int syncStatus) {
        if (AppSettings.isLogOfflineDataEnabled()) {
            Log.d(TAG, "Save Access Logs");
            AccessLogOfflineRecord accessLogOfflineRecord = new AccessLogOfflineRecord();
            try {
                accessLogOfflineRecord.setPrimaryId(accessLogOfflineRecord.lastPrimaryId());
                Gson gson = new Gson();
                accessLogOfflineRecord.setJsonObj(gson.toJson(obj));
                accessLogOfflineRecord.setOfflineSync(syncStatus);
                accessLogOfflineRecord.setDeviceTime(obj.deviceTime);
                accessLogOfflineRecord.setUtcTime(obj.utcRecordDate);
                if (data != null && data.member != null) {
                    if (data.member.memberid != null) {
                        accessLogOfflineRecord.setMemberId(data.member.getMemberid());
                    }
                    if (data.member.firstname != null) {
                        accessLogOfflineRecord.setFirstName(data.member.getFirstname());
                        if (data.member.lastname != null) {
                            accessLogOfflineRecord.setLastName(data.member.getLastname());
                        }
                    } else {
                        accessLogOfflineRecord.setFirstName("Anonymous");
                    }
                    if (data.member.image != null) {
                        accessLogOfflineRecord.setImagePath(data.member.image);
                    }
                } else {
                    accessLogOfflineRecord.setFirstName("Anonymous");
                    accessLogOfflineRecord.setLastName("");
                }
                DatabaseController.getInstance().insertOfflineAccessLog(accessLogOfflineRecord);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public RegisteredMembers getCheckedInOutMember() {
        return checkedInOutMember;
    }

    public void setCheckedInOutMember(RegisteredMembers checkedInOutMember) {
        this.checkedInOutMember = checkedInOutMember;
    }

    public boolean isCheckedIn(RegisteredMembers member) {
        boolean result = false;
        if (AppSettings.getTimeAndAttendance() == 1) {
            if (((ApplicationController.getInstance().getTimeAttendance() == 1) &&
                    !member.getDateTimeCheckInOut().isEmpty() && Util.differenceInTimeTwoDates(member.getDateTimeCheckInOut()))) {
                result = true;
            }
        }
        return result;
    }

    public boolean isCheckedOut(RegisteredMembers member) {
        boolean result = false;
        if (AppSettings.getTimeAndAttendance() == 1) {
            if ((ApplicationController.getInstance().getTimeAttendance() == 2) &&
                    !member.getDateTimeCheckOut().isEmpty()) {
                result = true;
            }
        }
        return result;
    }

    public void getMemberDetailsByGuid(String guid) {
        ApiInterface apiInterface = RetrofitInstance.getInstance().getApiInterface();
        GetMemberDetailsGuidRequest memberDetailRequest = new GetMemberDetailsGuidRequest();
        memberDetailRequest.certifyUniversalGuid = guid;
        Call<GetMemberGuidResponse> call = apiInterface.getMemberDetailsByGuid(Util.getSNCode(context), memberDetailRequest);
        call.enqueue(new Callback<GetMemberGuidResponse>() {
            @Override
            public void onResponse(Call<GetMemberGuidResponse> call, Response<GetMemberGuidResponse> response) {
                if (response.body() != null && response.body().responseCode == 1) {
                    MemberData memberData = response.body().memberData;
                    if (listener != null) {
                        listener.onMemberDetailsReceived(memberData);
                    }
                }
            }

            @Override
            public void onFailure(Call<GetMemberGuidResponse> call, Throwable t) {
                Log.e(TAG, "Error in GetMemberDetails " + t.getMessage());
            }
        });
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
