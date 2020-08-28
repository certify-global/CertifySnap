package com.certify.snap.controller;

import android.content.Context;
import android.util.Log;

import com.common.thermalimage.ThermalImageUtil;

public class ApplicationController {
    private static final String TAG = ApplicationController.class.getSimpleName();
    private static ApplicationController instance = null;
    private String endPointUrl = "";
    private String fcmPushToken = "";
    private ThermalImageUtil temperatureUtil = null;

    public static ApplicationController getInstance() {
        if (instance == null)
            instance = new ApplicationController();
        return instance;
    }

    public String getEndPointUrl() {
        return endPointUrl;
    }

    public void setEndPointUrl(String endPointUrl) {
        this.endPointUrl = endPointUrl;
    }

    public String getFcmPushToken() {
        return fcmPushToken;
    }

    public void setFcmPushToken(String fcmPushToken) {
        this.fcmPushToken = fcmPushToken;
    }

    public void initThermalUtil(Context context) {
        Log.d(TAG, "App Init Thermal Util");
        temperatureUtil = new ThermalImageUtil(context);
    }

    public ThermalImageUtil getTemperatureUtil() {
        return temperatureUtil;
    }

    public void releaseThermalUtil() {
        if (temperatureUtil != null) {
            Log.d(TAG, "App Release Thermal Util");
            temperatureUtil.release();
            temperatureUtil = null;
        }
    }
}
