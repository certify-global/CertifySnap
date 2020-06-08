package com.certify.snap.controller;

import com.certify.snap.model.QrCodeData;

public class CameraController {
    private static CameraController mInstance = null;
    private QrCodeData qrCodeData = null;
    private String qrCodeId = ""; //Optimize to use in QrCodeData

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

    private void clearData() {
        qrCodeData = null;
        qrCodeId = "";
    }
}
