package com.certify.snap.model;

public class AppStatusInfo {
    public String appStarted = "";
    public String appClosed = "";
    public String loginSuccess = "";
    public String loginFailed = "";
    public String deviceSettings = "";

    public static final String APP_STARTED = "Application Started";
    public static final String APP_CLOSED = "Application Closed";
    public static final String LOGIN_SUCCESS = "Successful Login";
    public static final String LOGIN_FAILED = "Failed Login";
    public static final String DEVICE_SETTINGS = "Device Settings Updated";


    private static AppStatusInfo instance = null;
    public static AppStatusInfo getInstance() {
        if (instance == null)
            instance = new AppStatusInfo();

        return instance;
    }

    public String getAppStarted() {
        return appStarted;
    }

    public void setAppStarted(String appStarted) {
        this.appStarted = appStarted;
    }

    public String getAppClosed() {
        return appClosed;
    }

    public void setAppClosed(String appClosed) {
        this.appClosed = appClosed;
    }

    public String getLoginSuccess() {
        return loginSuccess;
    }

    public void setLoginSuccess(String loginSuccess) {
        this.loginSuccess = loginSuccess;
    }

    public String getLoginFailed() {
        return loginFailed;
    }

    public void setLoginFailed(String loginFailed) {
        this.loginFailed = loginFailed;
    }

    public String getDeviceSettings() {
        return deviceSettings;
    }

    public void setDeviceSettings(String deviceSettings) {
        this.deviceSettings = deviceSettings;
    }

    public void clear() {
        appStarted = "";
        appClosed = "";
        loginSuccess = "";
        loginFailed = "";
        deviceSettings = "";
    }
}
