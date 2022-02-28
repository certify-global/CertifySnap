package com.certify.snap.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

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
import com.certify.snap.bean.QRCodeIssuer;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.DeviceKeySettings;
import com.certify.snap.model.GuestMembers;
import com.certify.snap.model.QrCodeStore;
import com.certify.snap.model.WaveSkipDb;
import com.certify.snap.model.OfflineGuestMembers;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.OfflineVerifyMembers;
import com.certify.snap.model.RegisteredFailedMembers;
import com.certify.snap.model.RegisteredMembers;

import java.util.List;

@Dao
public interface DatabaseStore {

    @Insert
    void insertMember(RegisteredMembers registeredMembers);

    @Insert
    void insertOfflineVerifyMember(OfflineVerifyMembers offlineVerifyMembers);

    @Insert
    void insertOfflineGuestMember(OfflineGuestMembers... offlineGuestMembers);

    @Insert
    void insertOfflineRecordTemperatureMembers(OfflineRecordTemperatureMembers... offlineRecordTemperatureMembers);

    @Insert
    void insertRegisteredFailedMember(RegisteredFailedMembers... registeredFailedMembers);

    @Insert
    void insertGuestMembers(GuestMembers guestMembers);

    @Insert
    void insertDeviceSetting(DeviceKeySettings deviceSettings);

   /* @Insert
    void insertGestureQuestions(QuestionDataDb questionData);*/

    @Insert
    void insertGestureQuestionList(GestureQuestionsDb gestureQuestionsDb);

    @Insert
    void insertSkipLogicList(List<WaveSkipDb> waveSkipDb);

    @Insert
    void insertLanguages(LanguageData questionData);

    @Insert
    void insertDeviceSettingsData(DeviceSettingsData deviceSettingsData);

    @Insert
    void insertAccessControlSettings(AccessControlSettings accessControlSettings);

    @Insert
    void insertAudioVisualSettings(AudioVisualSettings audioVisualSettings);

    @Insert
    void insertConfirmationViewSettings(ConfirmationViewSettings confirmationViewSettings);

    @Insert
    void insertGuideSettings(GuideSettings guideSettings);

    @Insert
    void insertHomePageSettings(HomePageSettings homePageSettings);

    @Insert
    void insertIdentificationSettings(IdentificationSettings identificationSettings);

    @Insert
    void insertPrinterSettings(PrinterSettings printerSettings);

    @Insert
    void insertScanViewSettings(ScanViewSettings scanViewSettings);

    @Insert
    void insertTouchlessSettings(TouchlessSettings touchlessSettings);

    @Insert
    void  insertQRIssuer(QRCodeIssuer qrIssuer);

    @Insert
    void insertQrCodeData(QrCodeStore qrCodeData);

    @Query("SELECT * FROM QRCodeIssuer WHERE keyID=:issuerKey")
    List<QRCodeIssuer> findissuerKey(String issuerKey);

    @Query("SELECT * FROM QRCodeIssuer")
    List<QRCodeIssuer> getissuerKey();


    @Transaction
    @Query("SELECT * FROM RegisteredMembers WHERE uniqueid=:uniqueID")
    List<RegisteredMembers> findMemberByUniqueId(String uniqueID);

    @Transaction
    @Query("SELECT * FROM RegisteredMembers WHERE primaryid=:primaryId")
    List<RegisteredMembers> findMemberByPrimaryId(long primaryId);

    @Transaction
    @Query("SELECT * FROM RegisteredMembers WHERE memberid=:memberId")
    List<RegisteredMembers> findMemberByMemberId(String memberId);

    @Transaction
    @Query("SELECT * FROM RegisteredMembers WHERE accessid=:accessId")
    List<RegisteredMembers> findMemberByAccessId(String accessId);

    @Transaction
    @Query("SELECT * FROM RegisteredMembers WHERE ltrim(accessid, 0)=:accessId")
    List<RegisteredMembers> findMemberByLTrimAccessId(String accessId);

    @Transaction
    @Query("SELECT * FROM RegisteredMembers WHERE certifyUniversalGuid=:guId")
    List<RegisteredMembers> findMemberByGuid(String guId);

    @Query("DELETE FROM RegisteredMembers")
    void deleteAll();

    @Query("DELETE FROM RegisteredMembers WHERE primaryid =:primaryId")
    int deleteMember(long primaryId);

    @Query("DELETE FROM RegisteredMembers WHERE uniqueid =:certifyId")
    int deleteMemberByCertifyId(String certifyId);

    @Query("SELECT * FROM RegisteredMembers")
    List<RegisteredMembers> findAllRegisterMembersList();

    @Query("SELECT * FROM RegisteredMembers ORDER BY dateTime DESC LIMIT 1")
    RegisteredMembers getLastMemberSyncDateTime();

    @Query("SELECT * FROM OfflineRecordTemperatureMembers ORDER BY primaryid DESC LIMIT 1")
    OfflineRecordTemperatureMembers OfflineRecordTemperatureMembers();

    @Query("SELECT * FROM OfflineRecordTemperatureMembers ORDER BY primaryid ASC LIMIT 1")
    OfflineRecordTemperatureMembers OfflineRecordTempFirstMember();

    @Query("SELECT * FROM OfflineRecordTemperatureMembers ORDER BY primaryid ASC LIMIT 10")
    List<OfflineRecordTemperatureMembers> LastTenOfflineTempRecord();

    @Query("SELECT * FROM QrCodeStore ORDER BY primaryId ASC LIMIT 1")
    QrCodeStore getFirstQrCodeData();

    @Query("SELECT * FROM QrCodeStore ORDER BY primaryid DESC LIMIT 1")
    QrCodeStore qrCodeGetLastPrimaryId();

    @Query("DELETE FROM OfflineRecordTemperatureMembers WHERE primaryid =:primaryId")
    void deleteOfflineRecord(long primaryId);

    @Query("SELECT COUNT (*) FROM OfflineRecordTemperatureMembers")
    int countOfflineRecord();

    @Query("SELECT * FROM RegisteredMembers ORDER BY primaryid DESC LIMIT 1")
    RegisteredMembers getLastMember();

    @Update
    void updateMember(RegisteredMembers registeredMember);

    @Query("SELECT * FROM OfflineRecordTemperatureMembers")
    List<OfflineRecordTemperatureMembers> findAllOfflineRecordTempMember();

    @Query("DELETE FROM OfflineRecordTemperatureMembers")
    void deleteAllOfflineRecord();

    @Insert
    void insertAccessLogOfflineRecord(AccessLogOfflineRecord... accessLogOfflineRecords);

    @Query("SELECT * FROM AccessLogOfflineRecord ORDER BY primaryId ASC LIMIT 1")
    AccessLogOfflineRecord firstAccessLogOfflineRecord();

    @Query("DELETE FROM AccessLogOfflineRecord WHERE primaryid =:primaryId")
    void deleteOfflineAccessLogRecord(long primaryId);

    @Query("DELETE FROM QrCodeStore WHERE primaryId =:primaryId")
    void deleteOfflineQrCodeRecord(long primaryId);

    @Query("SELECT * FROM AccessLogOfflineRecord ORDER BY primaryId DESC LIMIT 1")
    AccessLogOfflineRecord OfflineAccessLogRecord();

    @Query("SELECT * FROM AccessLogOfflineRecord")
    List<AccessLogOfflineRecord> findAllOfflineAccessLogRecord();

    @Query("SELECT * FROM AccessLogOfflineRecord ORDER BY primaryId ASC LIMIT 10")
    List<AccessLogOfflineRecord> LastTenOfflineAccessLog();

    @Query("SELECT * FROM RegisteredMembers ORDER BY primaryid ASC LIMIT 10")
    List<RegisteredMembers> lastTenMembers();

    @Query("DELETE FROM AccessLogOfflineRecord")
    void deleteAllOfflineAccessLogRecords();

    @Query("SELECT * FROM DeviceKeySettings")
    List<DeviceKeySettings> findAllDeviceSettings();

    @Transaction
    @Query("SELECT * FROM DeviceKeySettings WHERE settingName=:settingName")
    DeviceKeySettings findDeviceSettingByName(String settingName);

    @Query("SELECT * FROM QrCodeStore")
    List<QrCodeStore> findAllOfflineQrCodeData();

    @Update
    void updateSetting(DeviceKeySettings deviceKeySettings);

    /*@Query("SELECT * FROM QuestionDataDb")
    List<QuestionDataDb> findAllQuestionsData();

    @Query("DELETE FROM QuestionDataDb")
    void deleteAllQuestions();*/

    @Query("SELECT * FROM LanguageData")
    List<LanguageData> findAllLanguages();

    @Query("SELECT * FROM LanguageData WHERE languageId =:languageID")
    LanguageData getLanguageOnId(long languageID);

    @Query("SELECT * FROM LanguageData WHERE languageCode =:languagecode")
    LanguageData getLanguageOnCode(String languagecode);

    @Update
    void updateLanguageData(LanguageData languageData);

    @Query("DELETE FROM LanguageData")
    void deleteAllLanguages();

    @Query("SELECT * FROM DeviceSettingsData WHERE primaryId =:languageID")
    DeviceSettingsData getDeviceSettingsDataOnId(long languageID);

    @Query("SELECT * FROM AccessControlSettings WHERE primaryId =:languageID")
    AccessControlSettings getAccessControlSettingOnId(long languageID);

    @Query("SELECT * FROM AudioVisualSettings WHERE primaryId =:languageID")
    AudioVisualSettings getAudioVisualSettingOnId(long languageID);

    @Query("SELECT * FROM ConfirmationViewSettings WHERE primaryId =:languageID")
    ConfirmationViewSettings getConfirmationSettingsOnId(long languageID);

    @Query("SELECT * FROM GuideSettings WHERE primaryId =:languageID")
    GuideSettings getGuideSettingOnId(long languageID);

    @Query("SELECT * FROM IdentificationSettings WHERE primaryId =:languageID")
    IdentificationSettings getIdentificationSettingOnId(long languageID);

    @Query("SELECT * FROM PrinterSettings WHERE primaryId =:languageID")
    PrinterSettings getPrinterSettingOnId(long languageID);

    @Query("SELECT * FROM HomePageSettings WHERE primaryId =:languageID")
    HomePageSettings getHomePageSettingOnId(long languageID);

    @Query("SELECT * FROM ScanViewSettings WHERE primaryId =:languageID")
    ScanViewSettings getScanViewSettingOnId(long languageID);

    @Query("SELECT * FROM TouchlessSettings WHERE primaryId =:languageID")
    TouchlessSettings getTouchlessSettingOnId(long languageID);

    @Query("SELECT * FROM GestureQuestionsDb")
    List<GestureQuestionsDb> getGestureQuestionListDb();

    @Query("SELECT * FROM waveSkipDb WHERE parentQuestionId =:id AND expectedOutcomeName =:value")
    WaveSkipDb getLogicSkipListDb(String id,String value);

    @Update
    void updateDeviceSettingsData(DeviceSettingsData deviceSettingsData);

    @Update
    void updateAccessControlSettings(AccessControlSettings accessControlSettings);

    @Update
    void updateAudioVisualSettings(AudioVisualSettings audioVisualSettings);

    @Update
    void updateConfirmationViewSettings(ConfirmationViewSettings confirmationViewSettings);

    @Update
    void updateGuideSettings(GuideSettings guideSettings);

    @Update
    void updateHomePageSettings(HomePageSettings homePageSettings);

    @Update
    void updateIdentificationSettings(IdentificationSettings identificationSettings);

    @Update
    void updatePrinterSettings(PrinterSettings printerSettings);

    @Update
    void updateScanViewSettings(ScanViewSettings scanViewSettings);

    @Update
    void updateTouchlessSettings(TouchlessSettings touchlessSettings);

    @Query("DELETE FROM DeviceSettingsData")
    void deleteDeviceSettingsData();

    @Query("DELETE FROM AccessControlSettings")
    void deleteAccessControlSettings();

    @Query("DELETE FROM AudioVisualSettings")
    void deleteAudioVisualSettings();

    @Query("DELETE FROM ConfirmationViewSettings")
    void deleteConfirmationSettings();

    @Query("DELETE FROM GuideSettings")
    void deleteGuideSettings();

    @Query("DELETE FROM HomePageSettings")
    void deleteHomePageSettings();

    @Query("DELETE FROM IdentificationSettings")
    void deleteIdentificationSettings();

    @Query("DELETE FROM PrinterSettings")
    void deletePrinterSettings();

    @Query("DELETE FROM ScanViewSettings")
    void deleteScanSettings();

    @Query("DELETE FROM TouchlessSettings")
    void deleteTouchlessSettings();

    @Query("DELETE FROM GestureQuestionsDb")
    void deleteAllGestureQuestionList();

    @Query("DELETE FROM WaveSkipDb")
    void deleteAllLogicWaveSkipList();

}
