package com.certify.snap.controller;

import android.util.Log;

import com.certify.snap.common.AppSettings;
import com.certify.snap.common.UserExportedData;
import com.certify.snap.common.Util;

public class QrCodeController {
    private final String TAG = QrCodeController.class.getSimpleName();
    private static QrCodeController mInstance = null;
    private final String QR_FORMAT_ON_DATE = "/OD";
    private final String QR_FORMAT_BEFORE_DATE = "/OC";
    private final String QR_FORMAT_GENERIC = "/A:";
    private final String QR_DATE_FORMAT = "yyyyMMdd";
    private boolean isQrCodeMemberMatch = false;
    private UserExportedData data = null;

    public static QrCodeController getInstance() {
        if (mInstance == null) {
            mInstance = new QrCodeController();
        }
        return mInstance;
    }

    /**
     * Method that checks for the Qr code format
     * @param qrCode Qr code input
     * @return true or false accordingly
     */
    public boolean isQrCodeDated(String qrCode) {
        boolean result = false;
        if (qrCode.contains(QR_FORMAT_ON_DATE) ||
                qrCode.contains(QR_FORMAT_BEFORE_DATE) ||
                qrCode.contains(QR_FORMAT_GENERIC)) {
            result = true;
        }
        return result;
    }

    /**
     * Method that validates if the Qr code data is in range (today or expired)
     * @param qrCode qrCode Qr code input
     * @return true or false accordingly
     */
    public boolean validateDatedQrCode(String qrCode) {
        boolean result = false;
        if (qrCode.contains(QR_FORMAT_ON_DATE)) {
            String strSplit[] = qrCode.split("/");
            int size = strSplit.length;
            String strFormatDate = strSplit[size-1];
            String strDate = strFormatDate.substring(2);
            if (!strDate.isEmpty() && strDate.equals(Util.currentDate(QR_DATE_FORMAT))) {
                result = true;
            }
        } else if (qrCode.contains(QR_FORMAT_BEFORE_DATE)) {
            String strSplit[] = qrCode.split("/");
            int size = strSplit.length;
            String strFormatDate = strSplit[size-1];
            String strDate = strFormatDate.substring(2);
            String currentDate = Util.currentDate(QR_DATE_FORMAT);
            if (!strDate.isEmpty() && currentDate != null) {
                if (Util.isDateBigger(strDate, currentDate, QR_DATE_FORMAT)) {
                    result = true;
                }
            }
        } else if (qrCode.contains(QR_FORMAT_GENERIC)) {
            result = true;
        }
        return result;
    }

    public void setQrCodeMemberMatch(boolean value) {
        isQrCodeMemberMatch = value;
    }

    public boolean isFaceSearchedOnQrCode() {
        return (AppSettings.isQrCodeEnabled() && AppSettings.isScanOnQrEnabled() && isQrCodeMemberMatch);
    }

    public UserExportedData getData() {
        return data;
    }

    public void setData(UserExportedData data) {
        this.data = data;
    }

    public void clearData () {
        this.data = null;
        isQrCodeMemberMatch = false;
    }
}
