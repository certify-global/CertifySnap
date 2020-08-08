package com.certify.snap.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.litepal.crud.LitePalSupport;

@Entity(tableName = "GuestMembers")
public class GuestMembers {
    @PrimaryKey(autoGenerate = false)
    private int id;
    private String userId;
    private String name;
    private String expire_time;
    private String mobile;
    private String qrcode;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpire_time() {
        return expire_time;
    }

    public void setExpire_time(String expire_time) {
        this.expire_time = expire_time;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }
}
