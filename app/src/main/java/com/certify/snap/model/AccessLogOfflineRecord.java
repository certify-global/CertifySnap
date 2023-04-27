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
    private int id;
    private String titleType;
    private String firstName;
    private String middleName;
    private String lastName;
    private String memberId;
    private String temperature;
    private String deviceTime;
    private String utcTime;
    private String imagePath;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitleType() {
        return titleType;
    }

    public void setTitleType(String titleType) {
        this.titleType = titleType;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getDeviceTime() {
        return deviceTime;
    }

    public void setDeviceTime(String deviceTime) {
        this.deviceTime = deviceTime;
    }

    public String getUtcTime() {
        return utcTime;
    }

    public void setUtcTime(String utcTime) {
        this.utcTime = utcTime;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public static long lastPrimaryId() {
        long primaryId = 1;
        AccessLogOfflineRecord offlineRecord = DatabaseController.getInstance().getLastAccessLogPrimaryId();
        if (offlineRecord != null) {
            primaryId = offlineRecord.primaryId;
            ++primaryId;
            Log.d("OfflineAccessLogRecord", "primaryId offlineRecord " + primaryId);
            return primaryId;
        }
        Log.d("OfflineAccessLogRecord", "primaryId lastPrimaryId " + primaryId);

        return primaryId;
    }

    /*@Override
    public String toString() {
        return "AccessOfflineLog{" +
                "id='" + primaryId + '\'' +
                " jsonObj='"+ jsonObj +'\'' +
                " offlineSync='"+ offlineSync +'\'' +
                '}';
    }*/
}
