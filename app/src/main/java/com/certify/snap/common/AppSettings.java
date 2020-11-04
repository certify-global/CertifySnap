package com.certify.snap.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.certify.snap.R;

public class AppSettings {
    private static final String TAG = AppSettings.class.getSimpleName();
    private static AppSettings instance = null;

    private static String thermalScanTitle = "";
    private static String thermalScanSubTitle = "";
    private static boolean livenessDetect = false;
    private static String imageIcon = "";
    private static boolean captureTemperature = false;
    private static boolean allowLow = false;
    private static String tempTestLow = "";
    private static String fToC = "";
    private static String temperatureThreshold = "";
    private static boolean guideScreen = false;
    private static String guideText1 = "";
    private static String guideText2 = "";
    private static String guideText3 = "";
    private static int orientation = 0;
    private static boolean confirmScreenAbove = false;
    private static boolean confirmScreenBelow = false;
    private static String confirmScreenDelayValue = "";
    private static boolean facialDetect = false;
    private static boolean captureImagesAll = false;
    private static boolean captureImagesAboveThreshold = false;
    private static boolean proSettings = false;
    private static boolean displayInfoConfirmScreen = false;
    private static boolean qrSoundValid = false;
    private static  boolean qrSoundInvalid = false;
    private static boolean bleLightNormalTemperature = false;
    private static boolean bleLightHighTemperature = false;
    private static boolean anonymousQREnable = false;
    private static boolean isNavigationBarOn = false;
    private static boolean enablePrinter = false;
    private static float temperatureCompensation = 0;
    private static boolean enableVoice = false;
    private static boolean enableHandGesture = false;
    private static float displayTemperatureThreshold = 0;
    private static boolean setTemperatureThreshold = false;
    private static boolean printUsbEnabled = false;
    private static boolean printAllScan = false;
    private static boolean printAccessCardUsers = false;
    private static boolean printQrCodeUsers = false;
    private static boolean printWaveUsers = false;
    private static boolean printHighTemperatureUsers = false;
    private static boolean acknowledgementScreen = false;
    private static boolean temperatureResultBar = false;
    private static String tempResultBarNormal = "";
    private static String tempResultBarHigh = "";
    private static boolean qrCodeEnabled = false;
    private static boolean rfidEnabled = false;
    private static boolean accessLogEnabled = false;
    private static boolean gestureProgressEnabled = false;
    private static String gestureMessage = "";
    private static String gestureWorkFlow = "";
    private static int scanType = 1;
    private static boolean allowTempScan = true;
    private static boolean logOfflineData = false;

    public static AppSettings getInstance() {
        if (instance == null) {
            instance = new AppSettings();
        }
        return instance;
    }

    public void getSettingsFromSharedPref(Context context) {
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        thermalScanTitle = sharedPreferences.getString(GlobalParameters.Thermalscan_title, "THERMAL SCAN");
        thermalScanSubTitle = sharedPreferences.getString(GlobalParameters.Thermalscan_subtitle, "");
        livenessDetect = sharedPreferences.getBoolean(GlobalParameters.LivingType, true);
        imageIcon = sharedPreferences.getString(GlobalParameters.IMAGE_ICON, "");
        captureTemperature = sharedPreferences.getBoolean(GlobalParameters.CAPTURE_TEMPERATURE, true);
        allowLow = sharedPreferences.getBoolean(GlobalParameters.ALLOW_ALL, false);
        tempTestLow = sharedPreferences.getString(GlobalParameters.TEMP_TEST_LOW, "93.2");
        fToC = sharedPreferences.getString(GlobalParameters.F_TO_C, "F");
        temperatureThreshold = sharedPreferences.getString(GlobalParameters.TEMP_TEST, "99");
        guideScreen = sharedPreferences.getBoolean(GlobalParameters.GUIDE_SCREEN, true);
        guideText1 = sharedPreferences.getString(GlobalParameters.GUIDE_TEXT1, context.getString(R.string.text_value1));
        guideText2 = sharedPreferences.getString(GlobalParameters.GUIDE_TEXT2, context.getString(R.string.text_value2));
        guideText3 = sharedPreferences.getString(GlobalParameters.GUIDE_TEXT3, context.getString(R.string.text_value3));
        orientation = sharedPreferences.getInt(GlobalParameters.Orientation, 0);
        confirmScreenAbove = sharedPreferences.getBoolean(GlobalParameters.CONFIRM_SCREEN_ABOVE, true);
        confirmScreenBelow = sharedPreferences.getBoolean(GlobalParameters.CONFIRM_SCREEN_BELOW, true);
        confirmScreenDelayValue = sharedPreferences.getString(GlobalParameters.DELAY_VALUE, "3");
        facialDetect = sharedPreferences.getBoolean(GlobalParameters.FACIAL_DETECT, false);
        captureImagesAll = sharedPreferences.getBoolean(GlobalParameters.CAPTURE_IMAGES_ALL, false);
        captureImagesAboveThreshold = sharedPreferences.getBoolean(GlobalParameters.CAPTURE_IMAGES_ABOVE, false);
        proSettings = sharedPreferences.getBoolean(GlobalParameters.PRO_SETTINGS, false);
        displayInfoConfirmScreen = sharedPreferences.getBoolean(GlobalParameters.DISPLAY_IMAGE_CONFIRMATION, false);
        qrSoundValid = sharedPreferences.getBoolean(GlobalParameters.QR_SOUND_VALID, false);
        qrSoundInvalid= sharedPreferences.getBoolean(GlobalParameters.QR_SOUND_INVALID, false);
        bleLightNormalTemperature = sharedPreferences.getBoolean(GlobalParameters.BLE_LIGHT_NORMAL,false);
        bleLightHighTemperature = sharedPreferences.getBoolean(GlobalParameters.BLE_LIGHT_HIGH,false);
        anonymousQREnable = sharedPreferences.getBoolean(GlobalParameters.ANONYMOUS_ENABLE,false);
        isNavigationBarOn = sharedPreferences.getBoolean(GlobalParameters.NavigationBar,false);
        enablePrinter = sharedPreferences.getBoolean(GlobalParameters.BROTHER_BLUETOOTH_PRINTER,false);
        temperatureCompensation = sharedPreferences.getFloat(GlobalParameters.COMPENSATION, 0);
        enableVoice = sharedPreferences.getBoolean(GlobalParameters.VISUAL_RECOGNITION,false);
        enableHandGesture = sharedPreferences.getBoolean(GlobalParameters.HAND_GESTURE,false);
        displayTemperatureThreshold = sharedPreferences.getFloat(GlobalParameters.DISPLAY_TEMP_THRESHOLD, 0);
        setTemperatureThreshold = sharedPreferences.getBoolean(GlobalParameters.TEMPERATURE_THRESHOLD,false);
        printUsbEnabled = sharedPreferences.getBoolean(GlobalParameters.TOSHIBA_USB_PRINTER,false);
        printAllScan = sharedPreferences.getBoolean(GlobalParameters.PRINT_ALL_SCAN,false);
        printAccessCardUsers = sharedPreferences.getBoolean(GlobalParameters.PRINT_ACCESS_CARD_USERS,false);
        printQrCodeUsers = sharedPreferences.getBoolean(GlobalParameters.PRINT_QR_CODE_USERS,false);
        printWaveUsers = sharedPreferences.getBoolean(GlobalParameters.PRINT_WAVE_USERS,false);
        printHighTemperatureUsers = sharedPreferences.getBoolean(GlobalParameters.PRINT_HIGH_TEMPERATURE,false);
        acknowledgementScreen = sharedPreferences.getBoolean(GlobalParameters.ACKNOWLEDGEMENT_SCREEN, false);
        temperatureResultBar = sharedPreferences.getBoolean(GlobalParameters.RESULT_BAR, true);
        tempResultBarNormal = sharedPreferences.getString(GlobalParameters.RESULT_BAR_NORMAL, StringConstants.TEMPERATURE_NORMAL);
        tempResultBarHigh = sharedPreferences.getString(GlobalParameters.RESULT_BAR_HIGH, StringConstants.TEMPERATURE_HIGH);
        qrCodeEnabled = (sharedPreferences.getBoolean(GlobalParameters.QR_SCREEN, false) ||
                sharedPreferences.getBoolean(GlobalParameters.ANONYMOUS_ENABLE, false));
        rfidEnabled = sharedPreferences.getBoolean(GlobalParameters.RFID_ENABLE, false);
        accessLogEnabled = sharedPreferences.getBoolean(GlobalParameters.ACCESS_LOGGING, false);
        gestureProgressEnabled = sharedPreferences.getBoolean(GlobalParameters.PROGRESS_BAR, false);
        gestureMessage = sharedPreferences.getString(GlobalParameters.WAVE_INDICATOR, StringConstants.GESTURE_MESSAGE);
        gestureWorkFlow = sharedPreferences.getString(GlobalParameters.Touchless_setting_id,"");
        scanType = sharedPreferences.getInt(GlobalParameters.ScanType,1);
        allowTempScan = sharedPreferences.getBoolean(GlobalParameters.AllowTempScan, true);
        logOfflineData = sharedPreferences.getBoolean(GlobalParameters.LogOfflineData, false);
    }

    public static String getThermalScanTitle() {
        return thermalScanTitle;
    }

    public static String getThermalScanSubTitle() {
        return thermalScanSubTitle;
    }

    public static boolean isLivenessDetect() {
        return livenessDetect;
    }

    public static String getImageIcon() {
        return imageIcon;
    }

    public static boolean isCaptureTemperature() {
        return captureTemperature;
    }

    public static boolean isAllowLow() {
        return allowLow;
    }

    public static String getTempTestLow() {
        return tempTestLow;
    }

    public static String getfToC() {
        return fToC;
    }

    public static String getTemperatureThreshold() {
        return temperatureThreshold;
    }

    public static boolean isGuideScreen() {
        return guideScreen;
    }

    public static String getGuideText1() {
        return guideText1;
    }

    public static String getGuideText2() {
        return guideText2;
    }

    public static String getGuideText3() {
        return guideText3;
    }

    public static int getOrientation() {
        return orientation;
    }

    public static boolean isConfirmScreenAbove() {
        return confirmScreenAbove;
    }

    public static boolean isConfirmScreenBelow() {
        return confirmScreenBelow;
    }

    public static String getConfirmScreenDelayValue() {
        return confirmScreenDelayValue;
    }

    public static boolean isFacialDetect() {
        return facialDetect;
    }

    public static boolean isCaptureImagesAll() {
        return captureImagesAll;
    }

    public static boolean isCaptureImagesAboveThreshold() {
        return captureImagesAboveThreshold;
    }

    public static boolean isProSettings() {
        return proSettings;
    }

    public static void setProSettings(boolean proSettings) {
        AppSettings.proSettings = proSettings;
    }

    public static boolean isDisplayInfoConfirmScreen() {
        return displayInfoConfirmScreen;
    }

    public static boolean isQrSoundValid() {
        return qrSoundValid;
    }

    public static boolean isQrSoundInvalid() {
        return qrSoundInvalid;
    }

    public static boolean isBleLightNormalTemperature() {
        return bleLightNormalTemperature;
    }

    public static boolean isBleLightHighTemperature() {
        return bleLightHighTemperature;
    }

    public static boolean isAnonymousQREnable() {
        return anonymousQREnable;
    }

    public static boolean isIsNavigationBarOn() {
        return isNavigationBarOn;
    }

    public static boolean isEnablePrinter() {
        return enablePrinter;
    }

    public static float getTemperatureCompensation() {
        return temperatureCompensation;
    }

    public static boolean isEnableVoice() {
        return enableVoice;
    }

    public static boolean isEnableHandGesture() {
        return enableHandGesture;
    }

    public static float getDisplayTemperatureThreshold() {
        return displayTemperatureThreshold;
    }

    public static boolean isSetTemperatureThreshold() {
        return setTemperatureThreshold;
    }

    public static boolean isPrintUsbEnabled() {
        return printUsbEnabled;
    }

    public static boolean isPrintAllScan() {
        return printAllScan;
    }

    public static boolean isPrintAccessCardUsers() {
        return printAccessCardUsers;
    }

    public static boolean isPrintQrCodeUsers() {
        return printQrCodeUsers;
    }

    public static boolean isPrintWaveUsers() {
        return printWaveUsers;
    }

    public static boolean isPrintHighTemperatureUsers() {
        return printHighTemperatureUsers;
    }

    public static boolean isAcknowledgementScreen() {
        return acknowledgementScreen;
    }

    public static boolean isTemperatureResultBar() {
        return temperatureResultBar;
    }

    public static String getTempResultBarNormal() {
        return tempResultBarNormal;
    }

    public static String getTempResultBarHigh() {
        return tempResultBarHigh;
    }

    public static boolean isQrCodeEnabled() {
        return qrCodeEnabled;
    }

    public static boolean isRfidEnabled() {
        return rfidEnabled;
    }

    public static boolean isAccessLogEnabled() {
        return accessLogEnabled;
    }

    public static boolean isGestureProgressEnabled() {
        return gestureProgressEnabled;
    }

    public static String getGestureMessage() {
        return gestureMessage;
    }

    public static String getGestureWorkFlow() {
        return gestureWorkFlow;
    }

    public static int getScanType() {
        return scanType;
    }

    public static boolean isAllowTempScan() {
        return allowTempScan;
    }

    public static boolean isLogOfflineDataEnabled() {
        return logOfflineData;
    }
}
