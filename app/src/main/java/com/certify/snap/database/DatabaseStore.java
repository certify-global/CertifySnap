package com.certify.snap.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.certify.snap.api.response.LanguageData;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.DeviceSettings;
import com.certify.snap.model.GuestMembers;
import com.certify.snap.model.OfflineGuestMembers;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.OfflineVerifyMembers;
import com.certify.snap.model.QuestionDataDb;
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
    void insertDeviceSetting(DeviceSettings deviceSettings);

    @Insert
    void insertGestureQuestions(QuestionDataDb questionData);

    @Insert
    void insertLanguages(LanguageData questionData);

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

    @Query("DELETE FROM RegisteredMembers")
    void deleteAll();

    @Query("DELETE FROM RegisteredMembers WHERE primaryid =:primaryId")
    int deleteMember(long primaryId);

    @Query("DELETE FROM RegisteredMembers WHERE uniqueid =:certifyId")
    int deleteMemberByCertifyId(String certifyId);

    @Query("SELECT * FROM RegisteredMembers")
    List<RegisteredMembers> findAllRegisterMembersList();

    @Query("SELECT * FROM OfflineRecordTemperatureMembers ORDER BY primaryid DESC LIMIT 1")
    OfflineRecordTemperatureMembers OfflineRecordTemperatureMembers();

    @Query("SELECT * FROM OfflineRecordTemperatureMembers ORDER BY primaryid ASC LIMIT 1")
    OfflineRecordTemperatureMembers OfflineRecordTempFirstMember();

    @Query("SELECT * FROM OfflineRecordTemperatureMembers ORDER BY primaryid ASC LIMIT 10")
    List<OfflineRecordTemperatureMembers> LastTenOfflineTempRecord();

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

    @Query("SELECT * FROM DeviceSettings")
    List<DeviceSettings> findAllDeviceSettings();

    @Transaction
    @Query("SELECT * FROM DeviceSettings WHERE settingName=:settingName")
    DeviceSettings findDeviceSettingByName(String settingName);

    @Update
    void updateSetting(DeviceSettings deviceSettings);

    @Query("SELECT * FROM QuestionDataDb")
    List<QuestionDataDb> findAllQuestionsData();

    @Query("DELETE FROM QuestionDataDb")
    void deleteAllQuestions();

    @Query("SELECT * FROM LanguageData")
    List<LanguageData> findAllLanguages();

    @Query("SELECT * FROM LanguageData WHERE languageId =:languageID")
    LanguageData getLanguageOnId(long languageID);

    @Query("DELETE FROM LanguageData")
    void deleteAllLanguages();
}
