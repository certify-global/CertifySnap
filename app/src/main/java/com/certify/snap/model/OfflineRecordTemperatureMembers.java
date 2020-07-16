package com.certify.snap.model;

import org.litepal.crud.LitePalSupport;

public class OfflineRecordTemperatureMembers extends LitePalSupport {

    String firstName;
    String lastName;
    String memberId;
    String temperature;
    String deviceTime;
    String imagepath;
    String jsonObj;

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

    @Override
    public String toString() {
        return "TemperatureMembers{" +
                " firstname='" + firstName + '\'' +
                " lastname='" + lastName + '\'' +
                ", memberId='" + memberId + '\'' +
                ", temperature='" + temperature + '\'' +
                ", deviceTime='"+ deviceTime +'\'' +
                ", jsonObj='"+ jsonObj +'\'' +
                ", imagepath='" + imagepath + '\'' +
                '}';
    }

}
