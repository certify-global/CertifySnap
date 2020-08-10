package com.certify.snap.controller;

import android.content.Context;

import com.certify.snap.database.Database;
import com.certify.snap.database.DatabaseStore;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.RegisteredFailedMembers;
import com.certify.snap.model.RegisteredMembers;

import java.util.List;

public class DatabaseController {
    private static final String TAG = DatabaseController.class.getSimpleName();
    private static DatabaseController mInstance = null;
    private static DatabaseStore databaseStore;

    public static DatabaseController getInstance() {
        if (mInstance == null) {
            mInstance = new DatabaseController();
        }
        return mInstance;
    }

    public void init(Context context){
        databaseStore = Database.create(context).databaseStore();
    }

    public boolean isMemberExist(String memberId) {
        List<RegisteredMembers> membersList = databaseStore.findMemberOnMemberId(memberId);
        return membersList != null && membersList.size() > 0;
    }

    public boolean isAccessIdExist(String accessId) {
        List<RegisteredMembers> membersList = databaseStore.findMemberOnAccessId(accessId);
        return membersList != null && membersList.size() > 0;
    }

    public List<RegisteredMembers> findMember(String memberId) {
        return databaseStore.findMemberOnMemberId(memberId);
    }

    public int deleteMember(String memberId) {
        return databaseStore.deleteMember(memberId);
    }

    public List<RegisteredMembers> isUniqueIdExit(String uniqueID) {
        return databaseStore.findMemberOnUniqueId(uniqueID);
    }

    public void insertMemberToDB(RegisteredMembers member){
        databaseStore.insertMember(member);
    }

    public void insertOfflineMemberIntoDB(OfflineRecordTemperatureMembers offlineRecordTemperatureMembers){
        databaseStore.insertOfflineRecordTemperatureMembers(offlineRecordTemperatureMembers);
    }

    public void insertRegisterFailMember(RegisteredFailedMembers registeredFailedMembers){
        databaseStore.insertRegisteredFailedMember(registeredFailedMembers);
    }

    public List<RegisteredMembers> findAll(){
        return databaseStore.findAllRegisterMembersList();
    }

    public OfflineRecordTemperatureMembers getLastPrimaryId(){
        return databaseStore.OfflineRecordTemperatureMembers();
    }
}
