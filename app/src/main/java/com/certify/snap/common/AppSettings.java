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
    private static boolean allowAll = false;
    private static String tempTestLow = "";
    private static String fToC = "";
    private static String tempTest = "";
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
        allowAll = sharedPreferences.getBoolean(GlobalParameters.ALLOW_ALL, false);
        tempTestLow = sharedPreferences.getString(GlobalParameters.TEMP_TEST_LOW, "93.2");
        fToC = sharedPreferences.getString(GlobalParameters.F_TO_C, "F");
        tempTest = sharedPreferences.getString(GlobalParameters.TEMP_TEST, "99");
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

    public static boolean isAllowAll() {
        return allowAll;
    }

    public static String getTempTestLow() {
        return tempTestLow;
    }

    public static String getfToC() {
        return fToC;
    }

    public static String getTempTest() {
        return tempTest;
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
}
