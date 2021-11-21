package com.certify.snap.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.certify.snap.controller.DatabaseController;

@Entity(indices={@Index(value="primaryid", unique=true)})
public class OfflineRecordTemperatureMembers {

    public int id;
    public String firstName;
    public String lastName;
    public String memberId;
    public String temperature;
    public String deviceTime;
    public String utcTime;
    public String imagepath;
    public String jsonObj;
    @PrimaryKey
    @NonNull
    public Long primaryid;
    public int offlineSync;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getOfflineSync() {
        return offlineSync;
    }

    public void setOfflineSync(int offlineSync) {
        this.offlineSync = offlineSync;
    }

    public Long getPrimaryid() {
        return primaryid;
    }

    public void setPrimaryid(Long primaryid) {
        this.primaryid = primaryid;
    }

    public String getJsonObj() {
        return jsonObj;
    }

    public void setJsonObj(String jsonObj) {
        this.jsonObj = jsonObj;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
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

    public static Long lastPrimaryId() {
        long primaryId = 1;
        OfflineRecordTemperatureMembers offlineRecord = DatabaseController.getInstance().getLastPrimaryId();
        if (offlineRecord != null) {
            primaryId = offlineRecord.getPrimaryid();
            ++primaryId;
            Log.d("OfflineAccessLogRecord", "primaryId offlineRecord " + primaryId);
            return primaryId;
        }
        Log.d("OfflineRecordTemp", "primaryId lastPrimaryId " + primaryId);

        return primaryId;
    }

    @Override
    public String toString() {
        return "TemperatureMembers{" +
                "primaryid='" + primaryid + '\'' +
                " firstname='" + firstName + '\'' +
                " lastname='" + lastName + '\'' +
                ", memberId='" + memberId + '\'' +
                ", temperature='" + temperature + '\'' +
                ", deviceTime='"+ deviceTime +'\'' +
                ", jsonObj='"+ jsonObj +'\'' +
                ", imagepath='" + imagepath + '\'' +
                ", syncoffline='" + offlineSync + '\'' +
                '}';
    }

}
