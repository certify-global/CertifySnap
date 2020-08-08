package com.certify.snap.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.litepal.crud.LitePalSupport;

import java.util.Date;

@Entity(tableName = "OfflineVerifyMembers")
public class OfflineVerifyMembers {
    @PrimaryKey(autoGenerate = false)
    private int id;
    String name;
    String mobile;
    String imagepath;
    String temperature;
    Date verify_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
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
