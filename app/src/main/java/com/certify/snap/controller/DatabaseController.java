package com.certify.snap.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.certify.snap.api.response.AccessControlSettings;
import com.certify.snap.api.response.AudioVisualSettings;
import com.certify.snap.api.response.ConfirmationViewSettings;
import com.certify.snap.api.response.DeviceSettingsData;
import com.certify.snap.api.response.GestureQuestionsDb;
import com.certify.snap.api.response.GuideSettings;
import com.certify.snap.api.response.HomePageSettings;
import com.certify.snap.api.response.IdentificationSettings;
import com.certify.snap.api.response.LanguageData;
import com.certify.snap.api.response.PrinterSettings;
import com.certify.snap.api.response.ScanViewSettings;
import com.certify.snap.api.response.TouchlessSettings;
import com.certify.snap.common.Application;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.database.Database;
import com.certify.snap.database.DatabaseStore;
import com.certify.snap.database.secureDB.SQLCipherUtils;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.DeviceKeySettings;
import com.certify.snap.model.MemberSyncDataModel;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.QuestionDataDb;
import com.certify.snap.model.RegisteredFailedMembers;
import com.certify.snap.model.RegisteredMembers;
import com.certify.snap.service.MemberSyncService;

import net.sqlcipher.database.SQLiteException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseController {
    private static final String TAG = DatabaseController.class.getSimpleName();
    private static DatabaseController mInstance = null;
    private static DatabaseStore databaseStore = null;
    public static final int DB_VERSION = 8;
    public static Context mContext;
    private SharedPreferences sharedPreferences;

    public static DatabaseController getInstance() {
        if (mInstance == null) {
            mInstance = new DatabaseController();
        }
        return mInstance;
    }

    public void init(Context context, String passphrase){
        mContext = context;
        sharedPreferences = Util.getSharedPreferences(context);
        databaseStore = Database.create(context, passphrase).databaseStore();
    }

    public boolean isMemberExist(long primaryId) {
        if (databaseStore != null) {
            List<RegisteredMembers> membersList = databaseStore.findMemberByPrimaryId(primaryId);
            return membersList != null && membersList.size() > 0;
        }
        return false;
    }

    public boolean isMemberIdExist(String memberId){
        try {
            if (databaseStore != null) {
                List<RegisteredMembers> membersList = databaseStore.findMemberByMemberId(memberId);
                return membersList != null && membersList.size() > 0;
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return false;
    }

    public boolean isAccessIdExist(String accessId) {
        try {
            if (databaseStore != null) {
                List<RegisteredMembers> membersList = databaseStore.findMemberByAccessId(accessId);
                return membersList != null && membersList.size() > 0;
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return false;
    }

    public List<RegisteredMembers> findMemberByAccessId(String accessId) {
        try {
            if (databaseStore != null) {
                return databaseStore.findMemberByAccessId(accessId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return new ArrayList<>();
    }

    public List<RegisteredMembers> findMember(long primaryId) {
        if (databaseStore != null) {
            try {
                return databaseStore.findMemberByPrimaryId(primaryId);
            } catch (SQLiteException e) {
                if (handleDBException(e)) {
                    if (Util.isServiceRunning(MemberSyncService.class, mContext)) {
                        Util.stopMemberSyncService(mContext);
                        MemberSyncDataModel.getInstance().clear();
                    }
                    if (sharedPreferences != null && (sharedPreferences.getBoolean(GlobalParameters.FACIAL_DETECT, true)
                            || sharedPreferences.getBoolean(GlobalParameters.RFID_ENABLE, false))) {
                        if (sharedPreferences.getBoolean(GlobalParameters.SYNC_ONLINE_MEMBERS, false)) {
                            mContext.startService(new Intent(mContext, MemberSyncService.class));
                            Application.StartService(mContext);
                        }
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    public int deleteMember(long primaryId) {
        try {
            if (databaseStore != null) {
                return databaseStore.deleteMember(primaryId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return -1;
    }

    public List<RegisteredMembers> isUniqueIdExit(String uniqueID) {
        try {
            if (databaseStore != null) {
                return databaseStore.findMemberByUniqueId(uniqueID);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return new ArrayList<>();
    }

    public void insertMemberToDB(RegisteredMembers member) {
        try {
            if (databaseStore != null) {
                databaseStore.insertMember(member);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void insertOfflineMemberIntoDB(OfflineRecordTemperatureMembers offlineRecordTemperatureMembers) {
        try {
            if (databaseStore != null) {
                databaseStore.insertOfflineRecordTemperatureMembers(offlineRecordTemperatureMembers);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void insertRegisterFailMember(RegisteredFailedMembers registeredFailedMembers) {
        if (databaseStore != null) {
            databaseStore.insertRegisteredFailedMember(registeredFailedMembers);
        }
    }

    public List<RegisteredMembers> findAll() {
        try {
            if (databaseStore != null) {
                return databaseStore.findAllRegisterMembersList();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return new ArrayList<>();
    }

    public OfflineRecordTemperatureMembers getLastPrimaryId() {
        try {
            if (databaseStore != null) {
                return databaseStore.OfflineRecordTemperatureMembers();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    private RegisteredMembers getLastPrimaryIdOnMember() {
        try {
            if (databaseStore != null) {
                return databaseStore.getLastMember();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public long lastPrimaryIdOnMember() {
        RegisteredMembers memberRecord = getLastPrimaryIdOnMember();
        if (memberRecord != null) {
            Log.d("MemberRecord", "primaryId memberRecord " + (memberRecord.getPrimaryId() + 1));
            return memberRecord.getPrimaryId() + 1;
        }
        Log.d("MemberRecord", "primaryId lastPrimaryId " + 0L);

        return 1L;
    }

    public void deleteAllMember(){
        try {
            if (databaseStore != null){
                databaseStore.deleteAll();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void updateMember(RegisteredMembers registermember){
        try {
            if (databaseStore != null){
                databaseStore.updateMember(registermember);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public List<OfflineRecordTemperatureMembers> findAllOfflineRecord() {
        try {
            if (databaseStore != null) {
                return databaseStore.findAllOfflineRecordTempMember();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return new ArrayList<>();
    }

    public OfflineRecordTemperatureMembers getFirstOfflineRecord() {
        try {
            if (databaseStore != null) {
                return databaseStore.OfflineRecordTempFirstMember();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public void deleteOfflineRecord(long primaryId){
        try {
            if (databaseStore != null){
                databaseStore.deleteOfflineRecord(primaryId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public int countOfflineTempRecord(){
        if (databaseStore != null){
             return databaseStore.countOfflineRecord();
        }
        return 0;
    }

    public int deleteMemberByCertifyId(String certifyId){
        try {
            if (databaseStore != null){
                return databaseStore.deleteMemberByCertifyId(certifyId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return 0;
    }

    public void deleteAllOfflineRecord() {
        try {
            if (databaseStore != null){
                databaseStore.deleteAllOfflineRecord();
                databaseStore.deleteAllOfflineAccessLogRecords();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void insertOfflineAccessLog(AccessLogOfflineRecord accessLogOfflineRecord) {
        try {
            if (databaseStore != null) {
                databaseStore.insertAccessLogOfflineRecord(accessLogOfflineRecord);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public AccessLogOfflineRecord getFirstOfflineAccessLogRecord() {
        try {
            if (databaseStore != null) {
                return databaseStore.firstAccessLogOfflineRecord();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public void deleteOfflineAccessLogRecord(long primaryId){
        try {
            if (databaseStore != null){
                databaseStore.deleteOfflineAccessLogRecord(primaryId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public AccessLogOfflineRecord getLastAccessLogPrimaryId() {
        try {
            if (databaseStore != null) {
                return databaseStore.OfflineAccessLogRecord();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public List<AccessLogOfflineRecord> findAllOfflineAccessLogRecord() {
        try {
            if (databaseStore != null) {
                return databaseStore.findAllOfflineAccessLogRecord();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return new ArrayList<>();
    }

    public boolean isOfflineRecordTempExist() {
        boolean result = false;
        try {
            OfflineRecordTemperatureMembers firstMember =  getFirstOfflineRecord();
            if (firstMember != null){
                result = true;
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return result;
    }

    public boolean isOfflineAccessLogExist() {
        boolean result = false;
        try {
            AccessLogOfflineRecord firstAccessLogRecord = getFirstOfflineAccessLogRecord();
            if (firstAccessLogRecord != null){
                result = true;
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return result;
    }

    public List<OfflineRecordTemperatureMembers> lastTenOfflineTempRecord() {
        try {
            if (databaseStore != null) {
                return databaseStore.LastTenOfflineTempRecord();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return new ArrayList<>();
    }

    public List<AccessLogOfflineRecord> lastTenOfflineAccessLog() {
        try {
            if (databaseStore != null) {
                return databaseStore.LastTenOfflineAccessLog();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return new ArrayList<>();
    }

    public void insertDeviceKeySettingsToDB(DeviceKeySettings deviceSettings) {
        try {
            if (databaseStore != null) {
                databaseStore.insertDeviceSetting(deviceSettings);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public List<DeviceKeySettings> getDeviceSettings() {
        try {
            if (databaseStore != null) {
                return databaseStore.findAllDeviceSettings();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return new ArrayList<>();
    }

    public String getSetting(String settingName) {
        try {
            if (databaseStore != null) {
                DeviceKeySettings deviceSetting = databaseStore.findDeviceSettingByName(settingName);
                return deviceSetting.settingValue;
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return "";
    }

    public void updateSetting(DeviceKeySettings setting) {
        try {
            if (databaseStore != null) {
                databaseStore.updateSetting(setting);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

   /* public void insertQuestionsToDB(QuestionDataDb questionData) {
        try {
            if (databaseStore != null) {
                databaseStore.insertGestureQuestions(questionData);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public List<QuestionDataDb> getQuestionsFromDb() {
        try {
            if (databaseStore != null) {
                return databaseStore.findAllQuestionsData();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return new ArrayList<>();
    }

    public void deleteQuestionsFromDb() {
        try {
            if (databaseStore != null) {
                databaseStore.deleteAllQuestions();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    } */

    public void insertGestureQuestionList(GestureQuestionsDb gestureQuestionsDb) {
        try {
            if (databaseStore != null) {
                databaseStore.insertGestureQuestionList(gestureQuestionsDb);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public List<GestureQuestionsDb> getGestureQuestionsListFromDb() {
        try {
            if (databaseStore != null) {
                return databaseStore.getGestureQuestionListDb();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return new ArrayList<>();
    }

    public void deleteGestureQuestionsListFromDb() {
        try {
            if (databaseStore != null) {
                databaseStore.deleteAllGestureQuestionList();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void insertLanguagesToDB(LanguageData languageData) {
        try {
            if (databaseStore != null) {
                databaseStore.insertLanguages(languageData);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void updateLanguageDataToDB(LanguageData languageData) {
        try {
            if (databaseStore != null) {
                databaseStore.updateLanguageData(languageData);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public List<LanguageData> getLanguagesFromDb() {
        try {
            if (databaseStore != null) {
                return databaseStore.findAllLanguages();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return new ArrayList<>();
    }

    public LanguageData getLanguageOnId(int languageId) {
        try {
            if (databaseStore != null) {
                return databaseStore.getLanguageOnId(languageId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public LanguageData getLanguageOnCode(String languageCode) {
        try {
            if (databaseStore != null) {
                return databaseStore.getLanguageOnCode(languageCode);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public void deleteLanguagesFromDb() {
        try {
            if (databaseStore != null) {
                databaseStore.deleteAllLanguages();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public List<RegisteredMembers> lastTenMembers() {
        if (databaseStore != null) {
            return databaseStore.lastTenMembers();
        }
        return new ArrayList<>();
    }

    //insert settings method Added
    public void insertDeviceSettingsData(DeviceSettingsData deviceSettingsData) {
        try {
            if (databaseStore != null) {
                databaseStore.insertDeviceSettingsData(deviceSettingsData);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public DeviceSettingsData getDeviceSettingsDataOnId(int languageId) {
        try {
            if (databaseStore != null) {
                return databaseStore.getDeviceSettingsDataOnId(languageId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public void updateDeviceSettingsData(DeviceSettingsData setting) {
        try {
            if (databaseStore != null) {
                databaseStore.updateDeviceSettingsData(setting);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void deleteDeviceSettingsData() {
        try {
            if (databaseStore != null) {
                databaseStore.deleteDeviceSettingsData();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void insertAccessControlSettings(AccessControlSettings accessControlSettings) {
        try {
            if (databaseStore != null) {
                databaseStore.insertAccessControlSettings(accessControlSettings);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public AccessControlSettings getAccessControlSettingsOnId(int languageId) {
        try {
            if (databaseStore != null) {
                return databaseStore.getAccessControlSettingOnId(languageId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public void updateAccessControlSettings(AccessControlSettings setting) {
        try {
            if (databaseStore != null) {
                databaseStore.updateAccessControlSettings(setting);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void deleteAccessControlSettings() {
        try {
            if (databaseStore != null) {
                databaseStore.deleteAccessControlSettings();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void insertAudioVisualSettings(AudioVisualSettings audioVisualSettings) {
        try {
            if (databaseStore != null) {
                databaseStore.insertAudioVisualSettings(audioVisualSettings);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public AudioVisualSettings getAudioVisualSettingsOnId(int languageId) {
        try {
            if (databaseStore != null) {
                return databaseStore.getAudioVisualSettingOnId(languageId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public void updateAudioVisualSettings(AudioVisualSettings setting) {
        try {
            if (databaseStore != null) {
                databaseStore.updateAudioVisualSettings(setting);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void deleteAudioVisualSettings() {
        try {
            if (databaseStore != null) {
                databaseStore.deleteAudioVisualSettings();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void insertConfirmationViewSettings(ConfirmationViewSettings confirmationViewSettings) {
        try {
            if (databaseStore != null) {
                databaseStore.insertConfirmationViewSettings(confirmationViewSettings);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public ConfirmationViewSettings getConfirmationSettingOnId(int languageId) {
        try {
            if (databaseStore != null) {
                return databaseStore.getConfirmationSettingsOnId(languageId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public void updateConfirmationSettings(ConfirmationViewSettings setting) {
        try {
            if (databaseStore != null) {
                databaseStore.updateConfirmationViewSettings(setting);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void deleteConfirmationSettings() {
        try {
            if (databaseStore != null) {
                databaseStore.deleteConfirmationSettings();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void insertGuideSettings(GuideSettings guideSettings) {
        try {
            if (databaseStore != null) {
                databaseStore.insertGuideSettings(guideSettings);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public GuideSettings getGuideSettingsOnId(int languageId) {
        try {
            if (databaseStore != null) {
                return databaseStore.getGuideSettingOnId(languageId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public void updateGuideSettings(GuideSettings setting) {
        try {
            if (databaseStore != null) {
                databaseStore.updateGuideSettings(setting);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void deleteGuideSettings() {
        try {
            if (databaseStore != null) {
                databaseStore.deleteGuideSettings();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void insertHomePageSettings(HomePageSettings homePageSettings) {
        try {
            if (databaseStore != null) {
                databaseStore.insertHomePageSettings(homePageSettings);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public HomePageSettings getHomePageSettingsOnId(int languageId) {
        try {
            if (databaseStore != null) {
                return databaseStore.getHomePageSettingOnId(languageId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public void updateHomePageSettings(HomePageSettings setting) {
        try {
            if (databaseStore != null) {
                databaseStore.updateHomePageSettings(setting);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void deleteHomePageSettings() {
        try {
            if (databaseStore != null) {
                databaseStore.deleteHomePageSettings();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void insertIdentificationSettings(IdentificationSettings identificationSettings) {
        try {
            if (databaseStore != null) {
                databaseStore.insertIdentificationSettings(identificationSettings);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public IdentificationSettings getIdentificationSettingsId(int languageId) {
        try {
            if (databaseStore != null) {
                return databaseStore.getIdentificationSettingOnId(languageId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public void updateIdentificationSettings(IdentificationSettings setting) {
        try {
            if (databaseStore != null) {
                databaseStore.updateIdentificationSettings(setting);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void deleteIdentificationSettingsData() {
        try {
            if (databaseStore != null) {
                databaseStore.deleteIdentificationSettings();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void insertPrinterSettings(PrinterSettings printerSettings) {
        try {
            if (databaseStore != null) {
                databaseStore.insertPrinterSettings(printerSettings);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public PrinterSettings getPrinterSettingsOnId(int languageId) {
        try {
            if (databaseStore != null) {
                return databaseStore.getPrinterSettingOnId(languageId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public void updatePrinterSettings(PrinterSettings setting) {
        try {
            if (databaseStore != null) {
                databaseStore.updatePrinterSettings(setting);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void deletePrinterSettings() {
        try {
            if (databaseStore != null) {
                databaseStore.deletePrinterSettings();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void insertScanViewSettings(ScanViewSettings scanViewSettings) {
        try {
            if (databaseStore != null) {
                databaseStore.insertScanViewSettings(scanViewSettings);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public ScanViewSettings getScanViewSettingsOnId(int languageId) {
        try {
            if (databaseStore != null) {
                return databaseStore.getScanViewSettingOnId(languageId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public void updateScanViewSettings(ScanViewSettings setting) {
        try {
            if (databaseStore != null) {
                databaseStore.updateScanViewSettings(setting);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void deleteScanViewSettings() {
        try {
            if (databaseStore != null) {
                databaseStore.deleteScanSettings();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void insertTouchlessSettings(TouchlessSettings touchlessSettings) {
        try {
            if (databaseStore != null) {
                databaseStore.insertTouchlessSettings(touchlessSettings);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public TouchlessSettings getTouchlessSettingsOnId(int languageId) {
        try {
            if (databaseStore != null) {
                return databaseStore.getTouchlessSettingOnId(languageId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
        return null;
    }

    public void updateTouchlessSettings(TouchlessSettings setting) {
        try {
            if (databaseStore != null) {
                databaseStore.updateTouchlessSettings(setting);
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    public void deleteTouchlessSettings() {
        try {
            if (databaseStore != null) {
                databaseStore.deleteTouchlessSettings();
            }
        } catch (SQLiteException e){
            handleDBException(e);
        }
    }

    private boolean handleDBException(SQLiteException e) {
        if (e.getMessage().contains("file is not a database")) {
            SQLCipherUtils.State state = SQLCipherUtils.getDatabaseState(mContext.getApplicationContext(), Database.DB_NAME);
            if (state == SQLCipherUtils.State.ENCRYPTED){
                validateDB();
                init(mContext, Application.getInstance().getPragmaKey(mContext));
            } else if (state == SQLCipherUtils.State.DOES_NOT_EXIST){
                init(mContext, Application.getInstance().getPragmaKey(mContext));
            }
            return true;
        }
        return false;
    }

    public void validateDB() {
        File databasesDir = new File(mContext.getApplicationInfo().dataDir + "/databases");
        File file = new File(databasesDir, Database.DB_NAME);
        if (file.exists()) {
            file.delete();
            File fileDbShm = new File(databasesDir, "snap_face.db-shm");
            if (fileDbShm.exists()) {
                fileDbShm.delete();
            }
            File fileDbWal = new File(databasesDir, "snap_face.db-wal");
            if (fileDbWal.exists()) {
                fileDbWal.delete();
            }
        }
    }

    private void deleteMemberData() {
        String path = Environment.getExternalStorageDirectory() + "/pic";
        File file = new File(path);
        if (file.isDirectory() && file.exists()) {
            String[] children = file.list();
            for (String child : children) {
                new File(file, child).delete();
            }
            file.delete();
        }
    }

    public void clearAll() {
        if (databaseStore != null) {
            databaseStore.deleteAll();
            databaseStore.deleteAllOfflineRecord();
            databaseStore.deleteAllOfflineAccessLogRecords();
            deleteMemberData();
            FaceServer.getInstance().clearAllFaces(mContext);
        }
    }
}
