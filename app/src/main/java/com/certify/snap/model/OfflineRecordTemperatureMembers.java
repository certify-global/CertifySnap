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

    public static Long lastPrimaryId() {
        //OfflineRecordTemperatureMembers offlineRecord = LitePal.findLast(OfflineRecordTemperatureMembers.class);
        OfflineRecordTemperatureMembers offlineRecord = DatabaseController.getInstance().getLastPrimaryId();
        if (offlineRecord != null) {
            Log.d("OfflineRecordTemp", "primaryId offlineRecord " + (offlineRecord.getPrimaryid() + 1));
            return offlineRecord.getPrimaryid() + 1;
        }
        Log.d("OfflineRecordTemp", "primaryId lastPrimaryId " + 0L);

        return 1L;
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
