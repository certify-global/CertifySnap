package com.certify.snap.controller;

import android.content.Context;

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

    public void init(Context context){
        databaseStore = Database.create(context).databaseStore();
    }

    public boolean isMemberExist(String memberId) {
        if (databaseStore != null) {
            List<RegisteredMembers> membersList = databaseStore.findMemberOnMemberId(memberId);
            return membersList != null && membersList.size() > 0;
        }
        return false;
    }

    public boolean isAccessIdExist(String accessId) {
        if (databaseStore != null) {
            List<RegisteredMembers> membersList = databaseStore.findMemberOnAccessId(accessId);
            return membersList != null && membersList.size() > 0;
        }
        return false;
    }

    public List<RegisteredMembers> findMember(String memberId) {
        if (databaseStore != null) {
            return databaseStore.findMemberOnMemberId(memberId);
        }
        return new ArrayList<>();
    }

    public int deleteMember(String memberId) {
        if (databaseStore != null) {
            return databaseStore.deleteMember(memberId);
        }
        return -1;
    }

    public List<RegisteredMembers> isUniqueIdExit(String uniqueID) {
        if (databaseStore != null) {
            return databaseStore.findMemberOnUniqueId(uniqueID);
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
}
