package com.certify.snap.model;

import org.litepal.crud.LitePalSupport;

import java.util.Date;

public class OfflineVerifyMembers extends LitePalSupport {
    String name;
    String mobile;
    String imagepath;
    String temperature;
    Date verify_time;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public Date getVerify_time() {
        return verify_time;
    }

    public void setVerify_time(Date verify_time) {
        this.verify_time = verify_time;
    }

    @Override
    public String toString() {
        return "OfflineVerifyMembers{" +
                "name='" + name + '\'' +
                ", mobile='" + mobile + '\'' +
                ", imagepath='" + imagepath + '\'' +
                ", temperature='" + temperature + '\'' +
                ", verify_time=" + verify_time +
                '}';
    }
}
