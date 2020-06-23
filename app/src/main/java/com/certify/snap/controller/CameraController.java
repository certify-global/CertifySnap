package com.certify.snap.controller;

import android.util.Log;

import com.certify.snap.faceserver.CompareResult;
import com.certify.snap.model.QrCodeData;

public class CameraController {
    private static CameraController mInstance = null;
    private QrCodeData qrCodeData = null;
    private String qrCodeId = ""; //Optimize to use in QrCodeData
    private boolean isFaceVisible = false; //flag to let know when the face is detected
    public enum triggerValue {Face, Accessid, Codeid, Camera}
    private CompareResult compareResult = null;
    private boolean isFaceNotMatchedOnRetry = false;

    public static CameraController getInstance() {
        if (mInstance == null) {
            mInstance = new CameraController();
        }
        return mInstance;
    }

    public void init() {
        clearData();
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

    public void clearData() {
        qrCodeData = null;
        qrCodeId = "";
        isFaceVisible = false;
        compareResult = null;
        isFaceNotMatchedOnRetry = false;
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
}
