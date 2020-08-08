package com.certify.snap.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.litepal.crud.LitePalSupport;

@Entity(tableName = "OfflineGuestMembers")
public class OfflineGuestMembers {
    @PrimaryKey(autoGenerate = false)
    private int id;
    String userId;
    String verify_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVerify_time() {
        return verify_time;
    }

    public void setVerify_time(String verify_time) {
        this.verify_time = verify_time;
    }
}
