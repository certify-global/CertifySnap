package com.certify.snap.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.callback.GetLanguagesCallback;
import com.certify.snap.api.response.AccessControlSettings;
import com.certify.snap.api.response.AudioVisualSettings;
import com.certify.snap.api.response.ConfirmationViewSettings;
import com.certify.snap.api.response.DeviceSettings;
import com.certify.snap.api.response.DeviceSettingsApi;
import com.certify.snap.api.response.DeviceSettingsData;
import com.certify.snap.api.response.GuideSettings;
import com.certify.snap.api.response.HomePageSettings;
import com.certify.snap.api.response.IdentificationSettings;
import com.certify.snap.api.response.LanguageData;
import com.certify.snap.api.response.LanguageListResponse;
import com.certify.snap.api.response.PrinterSettings;
import com.certify.snap.api.response.ScanViewSettings;
import com.certify.snap.api.response.TouchlessSettings;
import com.certify.snap.async.AsyncGetLanguages;
import com.certify.snap.common.Constants;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class DeviceSettingsController implements GetLanguagesCallback {
    private static final String TAG = DeviceSettingsController.class.getSimpleName();
    private static DeviceSettingsController instance = null;
    private Context context;
    private GetLanguagesListener listener;

    public interface GetLanguagesListener {
        void onGetLanguages();
    }

    public static DeviceSettingsController getInstance() {
        if (instance == null) {
            instance = new DeviceSettingsController();
        }
        return instance;
    }

    public void init(Context context, GetLanguagesListener callbackListener) {
        this.context = context;
        this.listener = callbackListener;
    }

    public void getLanguages() {
        getLanguagesApi(0);
    }

    public void getLanguagesApi(int languageId) {
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        try {
            JSONObject obj = new JSONObject();
            obj.put("languageId", languageId);
            new AsyncGetLanguages(obj, this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.GetLanguages, context).execute();
        } catch (Exception e) {
            Log.d(TAG, "getLanguagesApi" + e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerGetLanguages(JSONObject report, String status, JSONObject req) {
        try {
            if (report == null) {
                Logger.error(TAG, "onJSONObjectListenerGetLanguages", "GetLanguages Log api failed");
                if (listener != null) {
                    listener.onGetLanguages();
                }
                return;
            }
            Gson gson = new Gson();
            LanguageListResponse response = gson.fromJson(String.valueOf(report), LanguageListResponse.class);
            if (response.responseCode != null && response.responseCode.equals("1")) {
                List<LanguageData> languageList = response.languageList;
                if (languageList.size() > 0) {
                    DatabaseController.getInstance().deleteLanguagesFromDb();
                    for (int i = 0; i < languageList.size(); i++) {
                        DatabaseController.getInstance().insertLanguagesToDB(languageList.get(i));
                        if (listener != null) {
                            listener.onGetLanguages();
                        }
                    }
                }
                Log.d(TAG, "Get Languages list updated");
                return;
            }
            if (listener != null) {
                listener.onGetLanguages();
            }
        } catch (Exception e) {
            Log.d(TAG, "onJSONObjectListenerGetLanguages" + e.getMessage());
        }
    }

    public void addLanguageDataToDb(int languageId) {
        LanguageData languageDataDb = DatabaseController.getInstance().getLanguageOnId(languageId);
        if (languageDataDb != null && languageDataDb.offline) {
            DatabaseController.getInstance().deleteLanguagesFromDb();
        }
        LanguageData languageData = getLanguageData(languageId, false);
        DatabaseController.getInstance().insertLanguagesToDB(languageData);
    }

    public void updateLanguageDataToDb(int languageId) {
        LanguageData languageDataDb = DatabaseController.getInstance().getLanguageOnId(languageId);
        if (languageDataDb != null && languageDataDb.offline) {
            DatabaseController.getInstance().deleteLanguagesFromDb();
        }
        LanguageData languageData = getLanguageData(languageId, false);
        DatabaseController.getInstance().updateLanguageDataToDB(languageData);
    }

    public void handleAddUpdateLanguageApi(int primaryId, DeviceSettingsApi deviceSettingsApi) {
        LanguageData languageData = DatabaseController.getInstance().getLanguageOnId(primaryId);
        if (languageData != null) {
            updateLanguageSettingsInDb(primaryId, deviceSettingsApi);
        } else {
            addLanguageSettingsToDb(primaryId, deviceSettingsApi);
        }
    }

    public void handleAdditionalLanguagesApi(List<DeviceSettings> languageSettingList) {
        if (languageSettingList != null) {
            for (int i = 0; i < languageSettingList.size(); i++) {
                DeviceSettings deviceSettings = languageSettingList.get(i);
                int languageCode = Integer.parseInt(deviceSettings.deviceSettingsData.languageId);
                LanguageData languageData = DatabaseController.getInstance().getLanguageOnId(languageCode);
                if (languageData != null) {
                    updateLanguageDataToDb(languageCode);
                    updateLanguageSettingsInDb(languageCode, deviceSettings);
                } else {
                    addLanguageDataToDb(languageCode);
                    addLanguageSettingsToDb(languageCode, deviceSettings);
                }
            }
        }
    }

    public void addLanguageSettingsToDb(int primaryId, DeviceSettingsApi deviceSettingsApi) {
        deviceSettingsApi.deviceSettingsData.primaryId = primaryId;
        deviceSettingsApi.accessControl.primaryId = primaryId;
        deviceSettingsApi.audioVisualAlerts.primaryId = primaryId;
        deviceSettingsApi.confirmationView.primaryId = primaryId;
        deviceSettingsApi.guideMessages.primaryId = primaryId;
        deviceSettingsApi.identificationSettings.primaryId = primaryId;
        deviceSettingsApi.printerSettings.primaryId = primaryId;
        deviceSettingsApi.homePageView.primaryId = primaryId;
        deviceSettingsApi.scanView.primaryId = primaryId;
        deviceSettingsApi.touchlessInteraction.primaryId = primaryId;
        DatabaseController.getInstance().insertDeviceSettingsData(deviceSettingsApi.deviceSettingsData);
        DatabaseController.getInstance().insertScanViewSettings(deviceSettingsApi.scanView);
        DatabaseController.getInstance().insertHomePageSettings(deviceSettingsApi.homePageView);
        DatabaseController.getInstance().insertConfirmationViewSettings(deviceSettingsApi.confirmationView);
        DatabaseController.getInstance().insertIdentificationSettings(deviceSettingsApi.identificationSettings);
        DatabaseController.getInstance().insertAudioVisualSettings(deviceSettingsApi.audioVisualAlerts);
        DatabaseController.getInstance().insertPrinterSettings(deviceSettingsApi.printerSettings);
        DatabaseController.getInstance().insertAccessControlSettings(deviceSettingsApi.accessControl);
        DatabaseController.getInstance().insertGuideSettings(deviceSettingsApi.guideMessages);
        DatabaseController.getInstance().insertTouchlessSettings(deviceSettingsApi.touchlessInteraction);
    }

    public void addLanguageSettingsToDb(int primaryId, DeviceSettings deviceSettings) {
        deviceSettings.deviceSettingsData.primaryId = primaryId;
        deviceSettings.accessControl.primaryId = primaryId;
        deviceSettings.audioVisualAlerts.primaryId = primaryId;
        deviceSettings.confirmationView.primaryId = primaryId;
        deviceSettings.guideMessages.primaryId = primaryId;
        deviceSettings.identificationSettings.primaryId = primaryId;
        deviceSettings.printerSettings.primaryId = primaryId;
        deviceSettings.homePageView.primaryId = primaryId;
        deviceSettings.scanView.primaryId = primaryId;
        deviceSettings.touchlessInteraction.primaryId = primaryId;
        DatabaseController.getInstance().insertDeviceSettingsData(deviceSettings.deviceSettingsData);
        DatabaseController.getInstance().insertScanViewSettings(deviceSettings.scanView);
        DatabaseController.getInstance().insertHomePageSettings(deviceSettings.homePageView);
        DatabaseController.getInstance().insertConfirmationViewSettings(deviceSettings.confirmationView);
        DatabaseController.getInstance().insertIdentificationSettings(deviceSettings.identificationSettings);
        DatabaseController.getInstance().insertAudioVisualSettings(deviceSettings.audioVisualAlerts);
        DatabaseController.getInstance().insertPrinterSettings(deviceSettings.printerSettings);
        DatabaseController.getInstance().insertAccessControlSettings(deviceSettings.accessControl);
        DatabaseController.getInstance().insertGuideSettings(deviceSettings.guideMessages);
        DatabaseController.getInstance().insertTouchlessSettings(deviceSettings.touchlessInteraction);
    }

    public void updateLanguageSettingsInDb(int primaryId, DeviceSettingsApi deviceSettingsApi) {
        DatabaseController.getInstance().updateDeviceSettingsData(deviceSettingsApi.deviceSettingsData);
        DatabaseController.getInstance().updateScanViewSettings(deviceSettingsApi.scanView);
        DatabaseController.getInstance().updateHomePageSettings(deviceSettingsApi.homePageView);
        DatabaseController.getInstance().updateConfirmationSettings(deviceSettingsApi.confirmationView);
        DatabaseController.getInstance().updateIdentificationSettings(deviceSettingsApi.identificationSettings);
        DatabaseController.getInstance().updateAudioVisualSettings(deviceSettingsApi.audioVisualAlerts);
        DatabaseController.getInstance().updatePrinterSettings(deviceSettingsApi.printerSettings);
        DatabaseController.getInstance().updateAccessControlSettings(deviceSettingsApi.accessControl);
        DatabaseController.getInstance().updateGuideSettings(deviceSettingsApi.guideMessages);
        DatabaseController.getInstance().updateTouchlessSettings(deviceSettingsApi.touchlessInteraction);
    }

    public void updateLanguageSettingsInDb(int primaryId, DeviceSettings deviceSettings) {
        DatabaseController.getInstance().updateDeviceSettingsData(deviceSettings.deviceSettingsData);
        DatabaseController.getInstance().updateScanViewSettings(deviceSettings.scanView);
        DatabaseController.getInstance().updateHomePageSettings(deviceSettings.homePageView);
        DatabaseController.getInstance().updateConfirmationSettings(deviceSettings.confirmationView);
        DatabaseController.getInstance().updateIdentificationSettings(deviceSettings.identificationSettings);
        DatabaseController.getInstance().updateAudioVisualSettings(deviceSettings.audioVisualAlerts);
        DatabaseController.getInstance().updatePrinterSettings(deviceSettings.printerSettings);
        DatabaseController.getInstance().updateAccessControlSettings(deviceSettings.accessControl);
        DatabaseController.getInstance().updateGuideSettings(deviceSettings.guideMessages);
        DatabaseController.getInstance().updateTouchlessSettings(deviceSettings.touchlessInteraction);
    }

    public void getSettingsFromDb(int languageId) {
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);

        //Device Settings
        DeviceSettingsData deviceSettings = DatabaseController.getInstance().getDeviceSettingsDataOnId(languageId);
        if (deviceSettings != null) {
            if (deviceSettings.doNotSyncMembers.equals("1")) {
                Util.writeBoolean(sharedPreferences, GlobalParameters.SYNC_ONLINE_MEMBERS, true);
            } else {
                Util.writeBoolean(sharedPreferences, GlobalParameters.SYNC_ONLINE_MEMBERS, false);
            }
            Util.writeBoolean(sharedPreferences, GlobalParameters.MEMBER_GROUP_SYNC, deviceSettings.syncMemberGroup.equals("1"));
            if (deviceSettings.groupId.isEmpty()) {
                deviceSettings.groupId = "0";
            }
            Util.writeString(sharedPreferences, GlobalParameters.MEMBER_GROUP_ID, deviceSettings.groupId);
            Util.writeString(sharedPreferences, GlobalParameters.deviceSettingMasterCode, deviceSettings.deviceMasterCode);
            Util.writeBoolean(sharedPreferences, GlobalParameters.NavigationBar, deviceSettings.navigationBar.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.PRO_SETTINGS, deviceSettings.multipleScanMode.equals("1"));
            String languageType = DeviceSettingsController.getInstance().getLanguageOnId(Integer.parseInt(deviceSettings.languageId));
            Util.writeString(sharedPreferences, GlobalParameters.LANGUAGE_TYPE, languageType);
        }

        //HomeView settings
        HomePageSettings homePageSettings = DatabaseController.getInstance().getHomePageSettingsOnId(languageId);
        if (homePageSettings != null) {
            Util.writeString(sharedPreferences, GlobalParameters.IMAGE_ICON, homePageSettings.logo);
            Util.writeString(sharedPreferences, GlobalParameters.Thermalscan_title, homePageSettings.line1);
            Util.writeString(sharedPreferences, GlobalParameters.Thermalscan_subtitle, homePageSettings.line2);
            Util.writeBoolean(sharedPreferences, GlobalParameters.HOME_TEXT_IS_ENABLE, homePageSettings.enableHomeScreen.equals("1"));
            Util.writeInt(sharedPreferences, GlobalParameters.HOME_DISPLAY_TIME, Integer.parseInt(homePageSettings.viewIntervalDelay));
            Util.writeBoolean(sharedPreferences, GlobalParameters.HOME_TEXT_ONLY_IS_ENABLE, homePageSettings.enableTextOnly.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.HOME_TEXT_ONLY_MESSAGE, homePageSettings.homeText);
        }

        //ScanView settings
        ScanViewSettings scanViewSettings = DatabaseController.getInstance().getScanViewSettingsOnId(languageId);
        if (scanViewSettings != null) {
            Util.writeString(sharedPreferences, GlobalParameters.DELAY_VALUE, scanViewSettings.viewDelay);
            Util.writeBoolean(sharedPreferences, GlobalParameters.CAPTURE_IMAGES_ABOVE, scanViewSettings.captureUserImageAboveThreshold.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.CAPTURE_IMAGES_ALL, scanViewSettings.captureAllUsersImage.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.TEMPERATURE_SOUND_NORMAL, scanViewSettings.enableSoundOnNormalTemperature.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.TEMPERATURE_SOUND_HIGH, scanViewSettings.enableSoundOnHighTemperature.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.CAPTURE_TEMPERATURE, scanViewSettings.displayTemperatureDetail.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.TEMP_TEST, scanViewSettings.temperatureThreshold);
            Util.writeString(sharedPreferences, GlobalParameters.F_TO_C, scanViewSettings.temperatureFormat);
            Util.writeString(sharedPreferences, GlobalParameters.TEMP_TEST_LOW, scanViewSettings.lowTemperatureThreshold);
            Util.writeBoolean(sharedPreferences, GlobalParameters.ALLOW_ALL, scanViewSettings.allowLowTemperatureScanning.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.MASK_DETECT, scanViewSettings.enableMaskDetection.equals("1"));
            Util.writeFloat(sharedPreferences, GlobalParameters.COMPENSATION, Float.parseFloat(scanViewSettings.temperatureCompensation));
            Util.writeBoolean(sharedPreferences, GlobalParameters.ScanProximity, scanViewSettings.closeProximityScan.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.RESULT_BAR, scanViewSettings.displayResultBar.equals("1"));
            Util.writeInt(sharedPreferences, GlobalParameters.ScanType, Integer.parseInt(scanViewSettings.scanType));
            Util.writeBoolean(sharedPreferences, GlobalParameters.EnableTempScan, scanViewSettings.enableTemperatureScan.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.LivingType, scanViewSettings.enableLiveness.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.RESULT_BAR_NORMAL, scanViewSettings.temperatureNormal);
            Util.writeString(sharedPreferences, GlobalParameters.RESULT_BAR_HIGH, scanViewSettings.temperatureHigh);

            if (scanViewSettings.audioForNormalTemperature != null && !scanViewSettings.audioForNormalTemperature.isEmpty()) {
                SoundController.getInstance().saveAudioFile(scanViewSettings.audioForNormalTemperature, "Normal.mp3");
            } else {
                SoundController.getInstance().deleteAudioFile("Normal.mp3");
            }
            if (scanViewSettings.audioForHighTemperature != null && !scanViewSettings.audioForHighTemperature.isEmpty()) {
                SoundController.getInstance().saveAudioFile(scanViewSettings.audioForHighTemperature, "High.mp3");
            } else {
                SoundController.getInstance().deleteAudioFile("High.mp3");
            }
        }

        //ConfirmationView settings
        ConfirmationViewSettings confirmationSettings = DatabaseController.getInstance().getConfirmationSettingOnId(languageId);
        if (confirmationSettings != null) {
            Util.writeBoolean(sharedPreferences, GlobalParameters.CONFIRM_SCREEN_BELOW, confirmationSettings.enableConfirmationScreen.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.CONFIRM_SCREEN_ABOVE, confirmationSettings.enableConfirmationScreenAboveThreshold.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.DELAY_VALUE_CONFIRM_BELOW, confirmationSettings.viewDelay);
            Util.writeString(sharedPreferences, GlobalParameters.DELAY_VALUE_CONFIRM_ABOVE, confirmationSettings.viewDelayAboveThreshold);
            Util.writeString(sharedPreferences, GlobalParameters.Confirm_title_below, confirmationSettings.normalViewLine1);
            Util.writeString(sharedPreferences, GlobalParameters.Confirm_subtitle_below, confirmationSettings.normalViewLine2);
            Util.writeString(sharedPreferences, GlobalParameters.Confirm_title_above, confirmationSettings.aboveThresholdViewLine1);
            Util.writeString(sharedPreferences, GlobalParameters.Confirm_subtitle_above, confirmationSettings.temperatureAboveThreshold2);
        }

        //GuideView Settings
        GuideSettings guideSettings = DatabaseController.getInstance().getGuideSettingsOnId(languageId);
        if (guideSettings != null) {
            Util.writeBoolean(sharedPreferences, GlobalParameters.GUIDE_SCREEN, guideSettings.enableGuideMessages.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.GUIDE_TEXT1, guideSettings.message1);
            Util.writeString(sharedPreferences, GlobalParameters.GUIDE_TEXT2, guideSettings.message2);
            Util.writeString(sharedPreferences, GlobalParameters.GUIDE_TEXT3, guideSettings.message3);
        }

        //Identification Settings
        IdentificationSettings identificationSettings = DatabaseController.getInstance().getIdentificationSettingsId(languageId);
        if (identificationSettings != null) {
            Util.writeBoolean(sharedPreferences, GlobalParameters.QR_SCREEN, identificationSettings.enableQRCodeScanner.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.RFID_ENABLE, identificationSettings.enableRFIDScanner.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.Timeout, identificationSettings.identificationTimeout);
            Util.writeBoolean(sharedPreferences, GlobalParameters.FACIAL_DETECT, identificationSettings.enableFacialRecognition.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.FACIAL_THRESHOLD, identificationSettings.facialThreshold);
            Util.writeBoolean(sharedPreferences, GlobalParameters.DISPLAY_IMAGE_CONFIRMATION, identificationSettings.enableConfirmationNameAndImage.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.ANONYMOUS_ENABLE, identificationSettings.enableAnonymousQRCode.equals("1"));
            Util.writeInt(sharedPreferences, GlobalParameters.ScanMode, Integer.parseInt(identificationSettings.cameraScanMode));
            Util.writeBoolean(sharedPreferences, GlobalParameters.ACKNOWLEDGEMENT_SCREEN, identificationSettings.enableAcknowledgementScreen.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.ACKNOWLEDGEMENT_TEXT, identificationSettings.acknowledgementText);
        }

        //AccessControl Settings
        AccessControlSettings accessControlSettings = DatabaseController.getInstance().getAccessControlSettingsOnId(languageId);
        if (accessControlSettings != null) {
            Util.writeBoolean(sharedPreferences, GlobalParameters.EnableRelay, accessControlSettings.enableAutomaticDoors.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.AllowAnonymous, accessControlSettings.allowAnonymous.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.RelayNormalMode, accessControlSettings.relayMode.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.StopRelayOnHighTemp, accessControlSettings.blockAccessHighTemperature.equals("true"));
            Util.writeInt(sharedPreferences, GlobalParameters.RelayTime, accessControlSettings.doorControlTimeWired);
            Util.writeBoolean(sharedPreferences, GlobalParameters.EnableWeigand, accessControlSettings.enableAccessControl.equals("1"));
            Util.writeInt(sharedPreferences, GlobalParameters.WeiganFormatMessage, accessControlSettings.accessControllerCardFormat);
            Util.writeBoolean(sharedPreferences, GlobalParameters.EnableWeigandPassThrough, accessControlSettings.enableWeigandPassThrough.equals("1"));
            Util.writeInt(sharedPreferences, GlobalParameters.AccessControlLogMode, accessControlSettings.loggingMode);
            Util.writeInt(sharedPreferences, GlobalParameters.AccessControlScanMode, accessControlSettings.validAccessOption);
        }

        //Audio-Visual settings
        AudioVisualSettings audioVisualSettings = DatabaseController.getInstance().getAudioVisualSettingsOnId(languageId);
        if (audioVisualSettings != null) {
            Util.writeBoolean(sharedPreferences, GlobalParameters.QR_SOUND_VALID, audioVisualSettings.enableSoundForValidQRCode.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.QR_SOUND_INVALID, audioVisualSettings.enableSoundForInvalidQRCode.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.BLE_LIGHT_NORMAL, audioVisualSettings.enableLightOnNormalTemperature.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.BLE_LIGHT_HIGH, audioVisualSettings.enableLightOnHighTemperature.equals("1"));

            if (audioVisualSettings.audioForValidQRCode != null && !audioVisualSettings.audioForValidQRCode.isEmpty()) {
                SoundController.getInstance().saveAudioFile(audioVisualSettings.audioForValidQRCode, "Valid.mp3");
            } else {
                SoundController.getInstance().deleteAudioFile("Valid.mp3");
            }
            if (audioVisualSettings.audioForInvalidQRCode != null && !audioVisualSettings.audioForInvalidQRCode.isEmpty()) {
                SoundController.getInstance().saveAudioFile(audioVisualSettings.audioForInvalidQRCode, "Invalid.mp3");
            } else {
                SoundController.getInstance().deleteAudioFile("Invalid.mp3");
            }
        }

        //Printer settings
        PrinterSettings printerSettings = DatabaseController.getInstance().getPrinterSettingsOnId(languageId);
        if (printerSettings != null) {
            Util.writeBoolean(sharedPreferences, GlobalParameters.BROTHER_BLUETOOTH_PRINTER, printerSettings.enableWBPrint.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.TOSHIBA_USB_PRINTER, printerSettings.enableUSBPrint.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_ALL_SCAN, printerSettings.printAllScan.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_ACCESS_CARD_USERS, printerSettings.printAccessCard.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_QR_CODE_USERS, printerSettings.printQRCode.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_WAVE_USERS, printerSettings.printWaveUsers.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_HIGH_TEMPERATURE, printerSettings.printHighTempScans.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_LABEL_FACE, printerSettings.printFace.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_LABEL_NAME, printerSettings.printName.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_LABEL_UNIDENTIFIED_NAME, printerSettings.unidentifiedPrintText.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.PRINT_LABEL_WAVE_EDIT_NAME, printerSettings.unidentifiedPrintTextValue);
            Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_LABEL_NORMAL_TEMPERATURE, printerSettings.printNormalTemperature.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_LABEL_HIGH_TEMPERATURE, printerSettings.printHighTemperature.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_LABEL_WAVE_ANSWERS, printerSettings.printWaveAnswers.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.PRINT_LABEL_WAVE_YES_ANSWER, printerSettings.printWaveAnswerYes);
            Util.writeString(sharedPreferences, GlobalParameters.PRINT_LABEL_WAVE_NO_ANSWER, printerSettings.printWaveAnswerNo);
            Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_QR_CODE_FOR_WAVE_INDICATOR, printerSettings.printIndicatorForQR.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.PRINT_LABEL_WAVE_EDIT_QR_ANSWERS, printerSettings.defaultBottomBarText);
            Util.writeString(sharedPreferences, GlobalParameters.PRINT_LABEL_EDIT_PASS_NAME, printerSettings.defaultResultPrint);
        }

        //Touchless Interaction settings
        TouchlessSettings touchlessSettings = DatabaseController.getInstance().getTouchlessSettingsOnId(languageId);
        if (touchlessSettings != null) {
            Util.writeBoolean(sharedPreferences, GlobalParameters.HAND_GESTURE, touchlessSettings.enableWave.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.WAVE_QUESTIONS, touchlessSettings.enableQuestionAndAnswer.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.Touchless_setting_id, touchlessSettings.settingId);
            Util.writeBoolean(sharedPreferences, GlobalParameters.MASK_ENFORCEMENT, touchlessSettings.enableMaskEnforcement.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.VISUAL_RECOGNITION, touchlessSettings.enableVoice.equals("1"));
            Util.writeBoolean(sharedPreferences, GlobalParameters.PROGRESS_BAR, touchlessSettings.showWaveProgress.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.WAVE_INDICATOR, touchlessSettings.waveIndicatorInstructions);
            Util.writeBoolean(sharedPreferences, GlobalParameters.WAVE_IMAGE, touchlessSettings.showWaveImage.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.MASK_ENFORCE_INDICATOR, touchlessSettings.maskEnforceText);
            Util.writeBoolean(sharedPreferences, GlobalParameters.GESTURE_EXIT_NEGATIVE_OP, touchlessSettings.exitOnNegativeOutcome.equals("1"));
            Util.writeString(sharedPreferences, GlobalParameters.GESTURE_EXIT_CONFIRM_TEXT, touchlessSettings.messageForNegativeOutcome);
        }
    }

    public HashMap<Integer, String> getLanguageMapFromDb() {
        LinkedHashMap<Integer, String> languageMap = new LinkedHashMap<>();
        List<LanguageData> list = DatabaseController.getInstance().getLanguagesFromDb();
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                 LanguageData languageData = list.get(i);
                 languageMap.put(languageData.languageId, languageData.name);
            }
        }
        return languageMap;
    }

    public String getLanguageOnId(int languageId) {
        String language = "en";
        LanguageData languageData = DatabaseController.getInstance().getLanguageOnId(languageId);
        if (languageData != null) {
            language = languageData.languageCode;
        }
        return language;
    }

    public String getLanguageNameOnCode(String languageCode) {
        String languageName = "English";
        LanguageData languageData = DatabaseController.getInstance().getLanguageOnCode(languageCode);
        if (languageData != null) {
            languageName = languageData.name;
        }
        return languageName;
    }

    public boolean isLanguagesInDBEmpty() {
        return DatabaseController.getInstance().getLanguagesFromDb().isEmpty();
    }

    public void addOfflineLanguages() {
        for (int i = 1; i <= Constants.LANGUAGES_MAX_COUNT; i++) {
            LanguageData languageData = getLanguageData(i, true);
            DatabaseController.getInstance().insertLanguagesToDB(languageData);
        }
    }

    private LanguageData getLanguageData(int value, boolean offlineVal) {
        LanguageData languageData = new LanguageData();
        switch (value) {
            case 1: {
                languageData.languageId = value;
                languageData.name = "English";
                languageData.languageCode = "en";
                languageData.fileCode = "";
                languageData.offline = offlineVal;
            }
            break;

            case 2: {
                languageData.languageId = value;
                languageData.name = "Spanish";
                languageData.languageCode = "es";
                languageData.fileCode = "es-rES";
                languageData.offline = offlineVal;
            }
            break;

            case 3: {
                languageData.languageId = value;
                languageData.name = "German";
                languageData.languageCode = "de";
                languageData.fileCode = "de-rDE";
                languageData.offline = offlineVal;
            }
            break;

            case 4: {
                languageData.languageId = value;
                languageData.name = "French";
                languageData.languageCode = "fr";
                languageData.fileCode = "fr-rFR";
                languageData.offline = offlineVal;
            }
            break;

            case 5: {
                languageData.languageId = value;
                languageData.name = "Italian";
                languageData.languageCode = "it";
                languageData.fileCode = "it-rIT";
                languageData.offline = offlineVal;
            }
            break;

            case 6: {
                languageData.languageId = value;
                languageData.name = "Hindi";
                languageData.languageCode = "hi";
                languageData.fileCode = "hi-rIN";
                languageData.offline = offlineVal;
            }
            break;

            case 7: {
                languageData.languageId = value;
                languageData.name = "Tamil";
                languageData.languageCode = "ta";
                languageData.fileCode = "ta-rIN";
                languageData.offline = offlineVal;
            }
            break;

            case 8: {
                languageData.languageId = value;
                languageData.name = "Telugu";
                languageData.languageCode = "te";
                languageData.fileCode = "te-rIN";
                languageData.offline = offlineVal;
            }
            break;
        }
        return languageData;
    }

}
