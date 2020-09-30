package com.certify.snap.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.common.thermalimage.ThermalImageUtil;

public class ApplicationController {
    private static final String TAG = ApplicationController.class.getSimpleName();
    private static ApplicationController instance = null;
    private String endPointUrl = "";
    private String fcmPushToken = "";
    private ThermalImageUtil temperatureUtil = null;
    private boolean isDeviceBoot = false;
    private boolean isTempServiceBound = false;

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

    public boolean isDeviceBoot() {
        return isDeviceBoot;
    }

    public void setDeviceBoot(boolean deviceBoot) {
        isDeviceBoot = deviceBoot;
    }

    /**
     * Method that initializes the Thermal util
     * @param context context
     */
    public void initThermalUtil(Context context) {
        Log.d(TAG, "App Init Thermal Util");
        temperatureUtil = new ThermalImageUtil(context);
        isTempServiceBound = true;
    }

    public ThermalImageUtil getTemperatureUtil() {
        return temperatureUtil;
    }

    /**
     * Method that sets the scanner time for Pro device
     * @param sharedPreferences SharedPref
     * @param proDeviceBootTime DateTime
     */
    public void setProDeviceBootTime(SharedPreferences sharedPreferences, String proDeviceBootTime) {
        if (sharedPreferences != null) {
            Util.writeString(sharedPreferences, GlobalParameters.PRO_DEVICE_BOOT_TIME, proDeviceBootTime);
        }
    }

    /**
     * Method that checks for the if the scanner time set for the Pro device
     * @param sharedPreferences sharedPref
     * @return true or false accordingly
     */
    public boolean isProDeviceStartScannerTimer(SharedPreferences sharedPreferences) {
        boolean result = true;
        if (sharedPreferences != null) {
            String dateTime = sharedPreferences.getString(GlobalParameters.PRO_DEVICE_BOOT_TIME, "");
            if (dateTime != null && !dateTime.isEmpty()) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Method that releases the Thermal Util
     */
    public void releaseThermalUtil() {
        if (temperatureUtil != null) {
            if (temperatureUtil.getUsingModule() != null && isTempServiceBound) {
                Log.d(TAG, "App Release Thermal Util");
                temperatureUtil.release();
                temperatureUtil = null;
                isTempServiceBound = false;
            }
        }
    }
}
