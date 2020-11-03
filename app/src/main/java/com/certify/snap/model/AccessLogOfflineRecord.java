package com.certify.snap.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.certify.snap.controller.DatabaseController;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class AccessLogOfflineRecord {

    @PrimaryKey
    @NonNull
    public long primaryId;
    public String jsonObj;
    public int offlineSync;


    public int getOfflineSync() {
        return offlineSync;
    }

    public void setOfflineSync(int offlineSync) {
        this.offlineSync = offlineSync;
    }

    public long getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(long primaryId) {
        this.primaryId = primaryId;
    }

    public String getJsonObj() {
        return jsonObj;
    }

    public void setJsonObj(String jsonObj) {
        this.jsonObj = jsonObj;
    }

    public static long lastPrimaryId() {
        AccessLogOfflineRecord offlineRecord = DatabaseController.getInstance().getLastAccessLogPrimaryId();
        if (offlineRecord != null) {
            Log.d("OfflineAccessLogRecord", "primaryId offlineRecord " + (offlineRecord.getPrimaryId() + 1));
            return offlineRecord.getPrimaryId() + 1;
        }
        Log.d("OfflineAccessLogRecord", "primaryId lastPrimaryId " + 0L);

        return 1L;
    }

    @Override
    public String toString() {
        return "AccessOfflineLog{" +
                "id='" + primaryId + '\'' +
                " jsonObj='"+ jsonObj +'\'' +
                " offlineSync='"+ offlineSync +'\'' +
                '}';
    }
}
