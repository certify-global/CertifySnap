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
    private static boolean gestureProgressEnabled = false;
    private static String gestureMessage = "";
    private static String gestureWorkFlow = "";
    private static int scanType = 1;
    private static boolean enableTempScan = true;
    private static boolean logOfflineData = false;
    private static boolean printLabelFace = false;
    private static boolean printLabelName = false;
    private static boolean printLabelUnidentifiedName = false;
    private static boolean printLabelNormalTemperature = false;
    private static boolean printLabelHighTemperature = false;
    private static boolean printLabelWaveAnswers = false;
    private static String editTextNameLabel = "";
    private static boolean maskEnforced = false;
    private static String maskEnforceMessage = "";
    private static boolean printLabelQRAnswers = false;
    private static String editTextPrintQRAnswers = "";
    private static String editTextPrintPassName = "";
    private static String editTextPrintWaveYes = "";
    private static String editTextPrintWaveNo = "";
    private static boolean gestureExitOnNegativeOp = false;
    private static String gestureExitConfirmText = "";
    private static int accessControlLogMode = Constants.DEFAULT_ACCESS_CONTROL_LOG_MODE;
    private static int accessControlScanMode = Constants.DEFAULT_ACCESS_CONTROL_SCAN_MODE;
    private static String memberSyncGroupId = "0";
    private static boolean memberGroupSyncEnabled = false;
    private static String languageType = "en";

    public static AppSettings getInstance() {
        if (instance == null) {
            instance = new AppSettings();
        }
        return instance;
    }

    public void getSettingsFromSharedPref(Context context) {
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        thermalScanTitle = sharedPreferences.getString(GlobalParameters.Thermalscan_title, context.getString(R.string.thermal_scan));
        thermalScanSubTitle = sharedPreferences.getString(GlobalParameters.Thermalscan_subtitle, "");
        livenessDetect = sharedPreferences.getBoolean(GlobalParameters.LivingType, false);
        imageIcon = sharedPreferences.getString(GlobalParameters.IMAGE_ICON, "");
        captureTemperature = sharedPreferences.getBoolean(GlobalParameters.CAPTURE_TEMPERATURE, true);
        allowLow = sharedPreferences.getBoolean(GlobalParameters.ALLOW_ALL, false);
        tempTestLow = sharedPreferences.getString(GlobalParameters.TEMP_TEST_LOW, "93.2");
        fToC = sharedPreferences.getString(GlobalParameters.F_TO_C, "F");
        temperatureThreshold = sharedPreferences.getString(GlobalParameters.TEMP_TEST, "100.4");
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
        tempResultBarNormal = sharedPreferences.getString(GlobalParameters.RESULT_BAR_NORMAL, context.getString(R.string.temperature_normal_msg));
        tempResultBarHigh = sharedPreferences.getString(GlobalParameters.RESULT_BAR_HIGH, context.getString(R.string.temperature_high_msg));
        qrCodeEnabled = (sharedPreferences.getBoolean(GlobalParameters.QR_SCREEN, false) ||
                sharedPreferences.getBoolean(GlobalParameters.ANONYMOUS_ENABLE, false));
        rfidEnabled = sharedPreferences.getBoolean(GlobalParameters.RFID_ENABLE, false);
        gestureProgressEnabled = sharedPreferences.getBoolean(GlobalParameters.PROGRESS_BAR, false);
        gestureMessage = sharedPreferences.getString(GlobalParameters.WAVE_INDICATOR, context.getString(R.string.gesture_msg));
        gestureWorkFlow = sharedPreferences.getString(GlobalParameters.Touchless_setting_id,"");
        scanType = sharedPreferences.getInt(GlobalParameters.ScanType,1);
        enableTempScan = sharedPreferences.getBoolean(GlobalParameters.EnableTempScan, true);
        logOfflineData = sharedPreferences.getBoolean(GlobalParameters.LogOfflineData, false);
        printLabelFace = sharedPreferences.getBoolean(GlobalParameters.PRINT_LABEL_FACE, false);
        printLabelName = sharedPreferences.getBoolean(GlobalParameters.PRINT_LABEL_NAME, false);
        printLabelNormalTemperature = sharedPreferences.getBoolean(GlobalParameters.PRINT_LABEL_NORMAL_TEMPERATURE, false);
        printLabelHighTemperature = sharedPreferences.getBoolean(GlobalParameters.PRINT_LABEL_HIGH_TEMPERATURE, false);
        printLabelWaveAnswers = sharedPreferences.getBoolean(GlobalParameters.PRINT_LABEL_WAVE_ANSWERS, false);
        printLabelUnidentifiedName = sharedPreferences.getBoolean(GlobalParameters.PRINT_LABEL_UNIDENTIFIED_NAME, false);
        editTextNameLabel = sharedPreferences.getString(GlobalParameters.PRINT_LABEL_WAVE_EDIT_NAME,"");
        maskEnforced = sharedPreferences.getBoolean(GlobalParameters.MASK_ENFORCEMENT, false);
        maskEnforceMessage = sharedPreferences.getString(GlobalParameters.MASK_ENFORCE_INDICATOR, context.getString(R.string.mask_enforce_msg));
        printLabelQRAnswers = sharedPreferences.getBoolean(GlobalParameters.PRINT_QR_CODE_FOR_WAVE_INDICATOR, false);
        editTextPrintQRAnswers = sharedPreferences.getString(GlobalParameters.PRINT_LABEL_WAVE_EDIT_QR_ANSWERS,"XXXX");
        editTextPrintPassName = sharedPreferences.getString(GlobalParameters.PRINT_LABEL_EDIT_PASS_NAME,context.getString(R.string.pass));
        editTextPrintWaveYes = sharedPreferences.getString(GlobalParameters.PRINT_LABEL_WAVE_YES_ANSWER,"1");
        editTextPrintWaveNo = sharedPreferences.getString(GlobalParameters.PRINT_LABEL_WAVE_NO_ANSWER,"0");
        gestureExitOnNegativeOp = sharedPreferences.getBoolean(GlobalParameters.GESTURE_EXIT_NEGATIVE_OP, false);
        gestureExitConfirmText = sharedPreferences.getString(GlobalParameters.GESTURE_EXIT_CONFIRM_TEXT, context.getString(R.string.gesture_exit_msg));
        accessControlLogMode = sharedPreferences.getInt(GlobalParameters.AccessControlLogMode,Constants.DEFAULT_ACCESS_CONTROL_LOG_MODE);
        accessControlScanMode = sharedPreferences.getInt(GlobalParameters.AccessControlScanMode,Constants.DEFAULT_ACCESS_CONTROL_SCAN_MODE);
        memberSyncGroupId = sharedPreferences.getString(GlobalParameters.MEMBER_GROUP_ID,"0");
        memberGroupSyncEnabled = sharedPreferences.getBoolean(GlobalParameters.MEMBER_GROUP_SYNC,false);
        languageType = sharedPreferences.getString(GlobalParameters.LANGUAGE_TYPE, "en");
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
        return (accessControlLogMode != 0);
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

    public static boolean isTemperatureScanEnabled() {
        return enableTempScan;
    }

    public static boolean isLogOfflineDataEnabled() {
        return logOfflineData;
    }

    public static boolean isPrintLabelFace() {
        return printLabelFace;
    }

    public static boolean isPrintLabelName() {
        return printLabelName;
    }

    public static boolean isPrintLabelNormalTemperature() {
        return printLabelNormalTemperature;
    }

    public static boolean isPrintLabelHighTemperature() {
        return printLabelHighTemperature;
    }

    public static boolean isPrintLabelWaveAnswers() {
        return printLabelWaveAnswers;
    }

    public static String getEditTextNameLabel() {
        return editTextNameLabel;
    }

    public static boolean isPrintLabelUnidentifiedName() {
        return printLabelUnidentifiedName;
    }

    public static boolean isMaskEnforced() {
        return maskEnforced;
    }

    public static String getMaskEnforceMessage() {
        return maskEnforceMessage;
    }


    public static boolean isPrintLabelQRAnswers() {
        return printLabelQRAnswers;
    }

    public static String getEditTextPrintQRAnswers() {
        return editTextPrintQRAnswers;
    }

    public static String getEditTextPrintPassName() {
        return editTextPrintPassName;
    }

    public static String getEditTextPrintWaveYes() {
        return editTextPrintWaveYes;
    }

    public static String getEditTextPrintWaveNo() {
        return editTextPrintWaveNo;
    }

    public static boolean isGestureExitOnNegativeOp() {
        return gestureExitOnNegativeOp;
    }

    public static String getGestureExitConfirmText() {
        return gestureExitConfirmText;
    }

    public static int getAccessControlLogMode() {
        return accessControlLogMode;
    }

    public static int getAccessControlScanMode() {
        return accessControlScanMode;
    }

    public static String getMemberSyncGroupId() {
        return memberSyncGroupId;
    }

    public static boolean isMemberGroupSyncEnabled() {
        return memberGroupSyncEnabled;
    }

    public static String getLanguageType() {
        return languageType;
    }

    public static void setLanguageType(String languageType) {
        AppSettings.languageType = languageType;
    }
}
