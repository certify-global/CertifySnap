package com.certify.snap.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.snap.common.Application;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.database.Database;
import com.certify.snap.database.DatabaseStore;
import com.certify.snap.database.secureDB.SQLCipherUtils;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
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
    public static final int DB_VERSION = 2;
    public static Context mContext;
    SharedPreferences sharedPreferences;

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
        try {
            if (databaseStore != null) {
                return databaseStore.findMemberByPrimaryId(primaryId);
            }
        } catch (SQLiteException e){
            handleDBException(e);
            if (!Util.isServiceRunning(MemberSyncService.class, mContext)) {
                if (sharedPreferences != null && (sharedPreferences.getBoolean(GlobalParameters.FACIAL_DETECT, true)
                        || sharedPreferences.getBoolean(GlobalParameters.RFID_ENABLE, false))) {
                    if (sharedPreferences.getBoolean(GlobalParameters.SYNC_ONLINE_MEMBERS, false)) {
                        mContext.startService(new Intent(mContext, MemberSyncService.class));
                        Application.StartService(mContext);
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

    public List<RegisteredMembers> lastTenMembers() {
        if (databaseStore != null) {
            return databaseStore.lastTenMembers();
        }
        return new ArrayList<>();
    }

    public void clearAll() {
        if (databaseStore != null) {
            databaseStore.deleteAll();
            databaseStore.deleteAllOfflineRecord();
            databaseStore.deleteAllOfflineAccessLogRecords();
        }
    }

    public void handleDBException(SQLiteException e) {
        if (e.getMessage().contains("file is not a database")) {
            SQLCipherUtils.State state = SQLCipherUtils.getDatabaseState(mContext.getApplicationContext(), Database.DB_NAME);
            if (state == SQLCipherUtils.State.ENCRYPTED){
                validateDB();
                init(mContext, Application.getInstance().getPragmaKey(mContext));
            } else if (state == SQLCipherUtils.State.DOES_NOT_EXIST){
                init(mContext, Application.getInstance().getPragmaKey(mContext));
            }
        }
    }

    public void validateDB() {
            File databasesDir = new File(mContext.getApplicationInfo().dataDir + "/databases");
            File file = new File(databasesDir, Database.DB_NAME);
            if (file.exists()) {
                file.delete();
                new File(databasesDir, "snap_face.db-shm").delete();
                new File(databasesDir, "snap_face.db-wal").delete();
            }
    }
}
