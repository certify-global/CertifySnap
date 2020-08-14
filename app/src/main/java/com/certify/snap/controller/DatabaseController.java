package com.certify.snap.controller;

import android.content.Context;
import android.util.Log;

import com.certify.snap.database.Database;
import com.certify.snap.database.DatabaseStore;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.RegisteredFailedMembers;
import com.certify.snap.model.RegisteredMembers;

import java.util.ArrayList;
import java.util.List;

public class DatabaseController {
    private static final String TAG = DatabaseController.class.getSimpleName();
    private static DatabaseController mInstance = null;
    private static DatabaseStore databaseStore = null;

    public static DatabaseController getInstance() {
        if (mInstance == null) {
            mInstance = new DatabaseController();
        }
        return mInstance;
    }

    public void init(Context context, String passphrase){
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
        if (databaseStore != null) {
            List<RegisteredMembers> membersList = databaseStore.findMemberByMemberId(memberId);
            return membersList != null && membersList.size() > 0;
        }
        return false;
    }

    public boolean isAccessIdExist(String accessId) {
        if (databaseStore != null) {
            List<RegisteredMembers> membersList = databaseStore.findMemberByAccessId(accessId);
            return membersList != null && membersList.size() > 0;
        }
        return false;
    }

    public List<RegisteredMembers> findMember(long primaryId) {
        if (databaseStore != null) {
            return databaseStore.findMemberByPrimaryId(primaryId);
        }
        return new ArrayList<>();
    }

    public int deleteMember(long primaryId) {
        if (databaseStore != null) {
            return databaseStore.deleteMember(primaryId);
        }
        return -1;
    }

    public List<RegisteredMembers> isUniqueIdExit(String uniqueID) {
        if (databaseStore != null) {
            return databaseStore.findMemberByUniqueId(uniqueID);
        }
        return new ArrayList<>();
    }

    public void insertMemberToDB(RegisteredMembers member) {
        if (databaseStore != null) {
            databaseStore.insertMember(member);
        }
    }

    public void insertOfflineMemberIntoDB(OfflineRecordTemperatureMembers offlineRecordTemperatureMembers) {
        if (databaseStore != null) {
            databaseStore.insertOfflineRecordTemperatureMembers(offlineRecordTemperatureMembers);
        }
    }

    public void insertRegisterFailMember(RegisteredFailedMembers registeredFailedMembers) {
        if (databaseStore != null) {
            databaseStore.insertRegisteredFailedMember(registeredFailedMembers);
        }
    }

    public List<RegisteredMembers> findAll() {
        if (databaseStore != null) {
            return databaseStore.findAllRegisterMembersList();
        }
        return new ArrayList<>();
    }

    public OfflineRecordTemperatureMembers getLastPrimaryId() {
        if (databaseStore != null) {
            return databaseStore.OfflineRecordTemperatureMembers();
        }
        return null;
    }

    private RegisteredMembers getLastPrimaryIdOnMember() {
        if (databaseStore != null) {
            return databaseStore.getLastMember();
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
        if (databaseStore != null){
            databaseStore.deleteAll();
        }
    }

    public void updateMember(RegisteredMembers registermember){
        if (databaseStore != null){
            databaseStore.updateMember(registermember);
        }
    }

    public List<OfflineRecordTemperatureMembers> findAllOfflineRecord() {
        if (databaseStore != null) {
            return databaseStore.findAllOfflineRecordTempMember();
        }
        return new ArrayList<>();
    }

    public OfflineRecordTemperatureMembers getFirstOfflineRecord() {
        if (databaseStore != null) {
            return databaseStore.OfflineRecordTempFirstMember();
        }
        return null;
    }

    public void deleteOfflineRecord(long primaryId){
        if (databaseStore != null){
            databaseStore.deleteOfflineRecord(primaryId);
        }
    }

    public int countOfflineTempRecord(){
        if (databaseStore != null){
             return databaseStore.countOfflineRecord();
        }
        return 0;
    }

    public int deleteMemberByCertifyId(String certifyId){
        if (databaseStore != null){
            return databaseStore.deleteMemberByCertifyId(certifyId);
        }
        return 0;
    }

    public void deleteAllOfflineRecord(){
        if (databaseStore != null){
            databaseStore.deleteAllOfflineRecord();
        }
    }
}
