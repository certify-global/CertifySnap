package com.certify.snap.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.util.Log;

import com.certify.snap.common.Constants;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.StringConstants;
import com.certify.snap.common.Util;
import com.certify.snap.model.DeviceKeySettings;
import com.common.thermalimage.ThermalImageUtil;

import java.util.List;

public class ApplicationController {
    private static final String TAG = ApplicationController.class.getSimpleName();
    private static ApplicationController instance = null;
    private String endPointUrl = "";
    private String fcmPushToken = "";
    private ThermalImageUtil temperatureUtil = null;
    private boolean isDeviceBoot = false;
    public int timeAttendance = 0;
    private ApplicationCallbackListener listener = null;
    private CountDownTimer healthCheckTimer = null;
    private boolean healthCheckInterval = false;
    private String appRestartTime = "";

    public interface ApplicationCallbackListener {
        void onHealthCheckNoResponse(int min, int sec);
        void onHealthCheckTimeout();
        void onHealthCheckTimerCancelled();
    }

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
        DeviceKeySettings deviceSetting = new DeviceKeySettings();
        deviceSetting.id = 1;
        deviceSetting.settingName = StringConstants.API_URL;
        deviceSetting.settingValue = endPointUrl;
        DatabaseController.getInstance().updateSetting(deviceSetting);
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

    public void initDeviceSettings(SharedPreferences sharedPreferences) {
        List<DeviceKeySettings> deviceSettingList = DatabaseController.getInstance().getDeviceSettings();
        if (deviceSettingList == null || deviceSettingList.isEmpty()) {
            DeviceKeySettings settings = new DeviceKeySettings();
            settings.id = 1;
            settings.settingName = StringConstants.API_URL;
            String value = "";
            if (sharedPreferences != null) {
                value = sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url);
            } else {
                value = EndPoints.prod_url;
            }
            settings.settingValue = value;
            DatabaseController.getInstance().insertDeviceKeySettingsToDB(settings);
            return;
        }
        String apiUrl = DatabaseController.getInstance().getSetting(StringConstants.API_URL);
        Util.writeString(sharedPreferences, GlobalParameters.URL, apiUrl);
    }

    /**
     * Method that releases the Thermal Util
     */
    public void releaseThermalUtil() {
        if (temperatureUtil != null) {
            try {
                if (temperatureUtil.getUsingModule() != null &&
                        temperatureUtil.getIsConnected()) {
                    Log.d(TAG, "App Release Thermal Util");
                    temperatureUtil.release();
                    temperatureUtil = null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Thermal release error " + e.getMessage());
            }
        }
    }

    public void clearSharedPrefData(Context context) {
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().apply();
            Util.writeBoolean(sharedPreferences, GlobalParameters.CLEAR_SHARED_PREF, true);
        }
    }

    public int getTimeAttendance() {
        return timeAttendance;
    }

    public void setTimeAttendance(int timeAttendance) {
        this.timeAttendance = timeAttendance;
    }

    public void setListener(ApplicationCallbackListener callbackListener) {
        listener = callbackListener;
    }

    public String getAppRestartTime() {
        return appRestartTime;
    }

    public void setAppRestartTime(String appRestartTime) {
        this.appRestartTime = appRestartTime;
    }

    public void startHealthCheckTimer(Context context) {
        if (healthCheckTimer != null) {
            Log.d(TAG, "Health check timer on");
            return;
        }
        healthCheckTimer = new CountDownTimer(Constants.HEALTH_CHECK_INIT_TIME, Constants.HEALTH_CHECK_INTERVAL) {
            @Override
            public void onTick(long remTime) {
                if (listener != null && healthCheckInterval) {
                    Log.d(TAG, "Health check api no response");
                    int minute = (int) (remTime/1000)/60;
                    int second = (int) (remTime/1000)%60;
                    listener.onHealthCheckNoResponse(minute, second);
                    return;
                }
                healthCheckInterval = true;
            }

            @Override
            public void onFinish() {
                healthCheckTimer.cancel();
                if (listener != null) {
                    listener.onHealthCheckTimeout();
                }
            }
        };
        Log.d(TAG, "Health Start timer ");
        healthCheckTimer.start();
    }

    public void cancelHealthCheckTimer(Context context) {
        if (healthCheckTimer != null) {
            Log.d(TAG, "Health cancel timer ");
            healthCheckTimer.cancel();
            healthCheckTimer = null;
            if (listener != null) {
                listener.onHealthCheckTimerCancelled();
            }
        }
        appRestartTime = "";
        healthCheckInterval = false;
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        Util.writeBoolean(sharedPreferences, GlobalParameters.HEALTH_CHECK_OFFLINE, false);
    }
}
