package com.certify.snap.controller;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;

import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Constants;
import com.certify.snap.common.Util;
import com.certify.snap.faceserver.CompareResult;
import com.certify.snap.model.FaceParameters;
import com.certify.snap.model.QrCodeData;
import com.common.thermalimage.ThermalImageUtil;

public class CameraController {
    private final String TAG = CameraController.class.getSimpleName();
    private static CameraController mInstance = null;
    private QrCodeData qrCodeData = null;
    private String qrCodeId = ""; //Optimize to use in QrCodeData
    private boolean isFaceVisible = false; //flag to let know when the face is detected
    public enum triggerValue {FACE, ACCESSID, CODEID, CAMERA, WAVE, MULTIUSER}
    private CompareResult compareResult = null;
    private boolean isFaceNotMatchedOnRetry = false;
    private boolean isScanCloseProximityEnabled = false;
    public int CAMERA_PREVIEW_HEIGHT = 1208;
    private FaceParameters faceParameters;
    private boolean isCameraOnForRfid = false;
    private boolean isAppExitTriggered = false;
    public static int IMAGE_PROCESS_COMPLETE = 1;
    public ScanState scanState = ScanState.IDLE;
    private  int deviceMode =0;
    private long scannerRemainingTime = 0;

    public enum ScanState {
        IDLE,
        FACIAL_SCAN,
        THERMAL_SCAN,
        GESTURE_SCAN,
        COMPLETE
    }

    public static CameraController getInstance() {
        if (mInstance == null) {
            mInstance = new CameraController();
        }
        return mInstance;
    }

    public void init() {
        clearData();
        faceParameters = new FaceParameters();
        if (AppSettings.isEnableHandGesture() || AppSettings.isEnableVoice()) {
            scanState = ScanState.GESTURE_SCAN;
        }
    }

    public QrCodeData getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(QrCodeData data) {
        qrCodeData = data;
    }

    public String getQrCodeId() {
        return qrCodeId;
    }

    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
    }

    public boolean isFaceVisible() {
        return isFaceVisible;
    }

    public void setFaceVisible(boolean faceVisible) {
        isFaceVisible = faceVisible;
    }

    public CompareResult getCompareResult() {
        return compareResult;
    }

    public void setCompareResult(CompareResult compareResult) {
        this.compareResult = compareResult;
    }

    public boolean isFaceNotMatchedOnRetry() {
        return isFaceNotMatchedOnRetry;
    }

    public void setFaceNotMatchedOnRetry(boolean faceNotMatchedOnRetry) {
        isFaceNotMatchedOnRetry = faceNotMatchedOnRetry;
    }

    public boolean isScanCloseProximityEnabled() {
        return isScanCloseProximityEnabled;
    }

    public void setScanCloseProximityEnabled(boolean scanCloseProximityEnabled) {
        isScanCloseProximityEnabled = scanCloseProximityEnabled;
    }

    public FaceParameters getFaceParameters() {
        return faceParameters;
    }

    public boolean isCameraOnRfid() {
        return isCameraOnForRfid;
    }

    public void setCameraOnRfid(boolean cameraOn) {
        isCameraOnForRfid = cameraOn;
    }

    public ScanState getScanState() {
        return scanState;
    }

    public void setScanState(ScanState scanState) {
        this.scanState = scanState;
    }

    public boolean isAppExitTriggered() {
        return isAppExitTriggered;
    }

    public void setAppExitTriggered(boolean appExitTriggered) {
        isAppExitTriggered = appExitTriggered;
    }

    public void initDeviceMode() {
        ThermalImageUtil thermalImageUtil = ApplicationController.getInstance().getTemperatureUtil();
        if (thermalImageUtil != null && thermalImageUtil.getUsingModule() != null) {
            deviceMode = thermalImageUtil.getUsingModule()[0];
        }
    }

    public int getDeviceMode() {
        return deviceMode;
    }

    public long getScannerRemainingTime() {
        return scannerRemainingTime;
    }

    public void setScannerRemainingTime(long scannerRemainingTime) {
        this.scannerRemainingTime = scannerRemainingTime;
    }

    public void startProDeviceInitTimer(Context context) {
        if (scannerRemainingTime > 0) {
            long timeDuration = (scannerRemainingTime * 60 * 1000);
            new CountDownTimer(timeDuration, Constants.PRO_SCANNER_INIT_INTERVAL) {
                @Override
                public void onTick(long remTime) {
                    scannerRemainingTime = ((remTime/1000)/60);
                }

                @Override
                public void onFinish() {
                    scannerRemainingTime = 0;
                    ApplicationController.getInstance().setProDeviceBootTime(Util.getSharedPreferences(context), Util.currentDate());
                }
            }.start();
        }
    }

    public float getOnlyTextSize(int length) {
        Log.i("getOnlyTextSize  ", "" + length);
        if (length > 2000)
            return 16;
        else if (length > 1000)
            return 20;
        else if (length > 350)
            return 24;
        return 34;
    }

    public void clearData() {
        qrCodeData = null;
        qrCodeId = "";
        isFaceVisible = false;
        compareResult = null;
        isFaceNotMatchedOnRetry = false;
        if (faceParameters != null) {
            faceParameters.clear();
        }
        isAppExitTriggered = false;
        scanState = ScanState.IDLE;
    }

}
