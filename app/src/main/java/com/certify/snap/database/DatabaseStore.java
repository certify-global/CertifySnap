package com.certify.snap.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.certify.snap.model.GuestMembers;
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
}
