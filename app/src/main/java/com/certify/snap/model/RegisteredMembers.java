package com.certify.snap.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class RegisteredMembers extends LitePalSupport {

    String name;
    String expire_time;
    String status;
    @Column(unique = true)
    String mobile;
    String image;
    String features;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    @Override
    public String toString() {
        return "Members{" +
                " name='" + name + '\'' +
                ", expire_time='" + expire_time + '\'' +
                ", status='" + status + '\'' +
                ", mobile='" + mobile + '\'' +
                ", image='" + image + '\'' +
                ", feature='"+ features +'\'' +
                '}';
    }
}
