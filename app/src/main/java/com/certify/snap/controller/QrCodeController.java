package com.certify.snap.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.callback.GetLastCheckinTimeCallback;
import com.certify.snap.async.AsyncGetLastCheckinTime;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.UserExportedData;
import com.certify.snap.common.Util;

import org.json.JSONException;
import org.json.JSONObject;

public class QrCodeController implements GetLastCheckinTimeCallback {
    private final String TAG = QrCodeController.class.getSimpleName();
    private static QrCodeController mInstance = null;
    private final String QR_FORMAT_ON_DATE = "/OD";
    private final String QR_FORMAT_BEFORE_DATE = "/OC";
    private final String QR_FORMAT_GENERIC = "/A:";
    private final String QR_DATE_FORMAT = "yyyyMMdd";
    private boolean isQrCodeMemberMatch = false;
    private UserExportedData data = null;
    private QrCodeListener listener = null;
    private boolean memberCheckedIn = false;

    public interface QrCodeListener {
        void onGetLastCheckInTime(boolean checkedIn);
    }

    public static QrCodeController getInstance() {
        if (mInstance == null) {
            mInstance = new QrCodeController();
        }
        return mInstance;
    }

    public void setListener(QrCodeListener callbackListener) {
        listener = callbackListener;
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

    /*public boolean isFaceSearchedOnQrCode() {
        return (AppSettings.isQrCodeEnabled() && AppSettings.isScanOnQrEnabled() && isQrCodeMemberMatch);
    }*/

    public UserExportedData getData() {
        return data;
    }

    public void setData(UserExportedData data) {
        this.data = data;
    }

    public void getLastCheckInTime(Context context, String certifyId) {
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        try {
            JSONObject obj = new JSONObject();
            obj.put("institutionId", sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, ""));
            obj.put("certifyId", certifyId);
            new AsyncGetLastCheckinTime(obj, this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.GetLastCheckinTime, context).execute();
        } catch (Exception e) {
            Log.d(TAG, "getLanguagesApi" + e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerGetCheckInTime(JSONObject report, String status, JSONObject req) {
        if (report == null) {
            Logger.error(TAG, "onJSONObjectListenerGetCheckInTime", "Get Last Check-in time failed");
            if (listener != null) {
                listener.onGetLastCheckInTime(false);
            }
            return;
        }
        try {
            if (report.getString("responseCode").equals("1")) {
                if (listener != null) {
                    listener.onGetLastCheckInTime(true);
                }
            } else {
                if (listener != null) {
                    listener.onGetLastCheckInTime(false);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error in get last check in response " + e.getMessage());
        }
    }

    public boolean isMemberCheckedIn() {
        return memberCheckedIn;
    }

    public void setMemberCheckedIn(boolean memberCheckedIn) {
        this.memberCheckedIn = memberCheckedIn;
    }

    public boolean isOnlyQrCodeEnabled() {
        boolean result = false;
        if ((AppSettings.getPrimaryIdentifier()
                == CameraController.PrimaryIdentification.QR_CODE.getValue()) &&
                (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.NONE.getValue())) {
            result = true;
        }
        return result;
    }

    public void clearData () {
        this.data = null;
        isQrCodeMemberMatch = false;
        memberCheckedIn = false;
    }
}
