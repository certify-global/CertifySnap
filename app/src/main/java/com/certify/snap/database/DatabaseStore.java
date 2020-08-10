package com.certify.snap.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.certify.snap.model.GuestMembers;
import com.certify.snap.model.OfflineGuestMembers;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.OfflineVerifyMembers;
import com.certify.snap.model.RegisteredFailedMembers;
import com.certify.snap.model.RegisteredMembers;

import java.util.List;

@Dao
public interface DatabaseStore {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMember(RegisteredMembers registeredMembers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOfflineVerifyMember(OfflineVerifyMembers offlineVerifyMembers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOfflineGuestMember(OfflineGuestMembers... offlineGuestMembers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOfflineRecordTemperatureMembers(OfflineRecordTemperatureMembers... offlineRecordTemperatureMembers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRegisteredFailedMember(RegisteredFailedMembers... registeredFailedMembers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGuestMembers(GuestMembers guestMembers);

    @Transaction
    @Query("SELECT * FROM registeredmembers WHERE uniqueid=:uniqueID")
    List<RegisteredMembers> findMemberOnUniqueId(String uniqueID);

    @Transaction
    @Query("SELECT * FROM registeredmembers WHERE memberid=:memberId")
    List<RegisteredMembers> findMemberOnMemberId(String memberId);

    @Transaction
    @Query("SELECT * FROM registeredmembers WHERE accessid=:accessId")
    List<RegisteredMembers> findMemberOnAccessId(String accessId);

    @Query("DELETE FROM registeredmembers")
    void deleteAll();

    @Query("DELETE FROM registeredmembers WHERE memberid =:memberId")
    int deleteMember(String memberId);

    /*@Query("SELECT * FROM registeredmembers")
    void findAllRegisterMemberAsync();*/
}
