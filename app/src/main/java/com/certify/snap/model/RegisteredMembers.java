package com.certify.snap.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.certify.snap.controller.DatabaseController;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;


@Entity(indices={@Index(value="primaryid", unique=true)})
public class RegisteredMembers {

    @PrimaryKey
    @NonNull
    public long primaryid;
    public String firstname;
    public String lastname;
    public String expiretime;
    public String status;
    public String mobile;
    public String image;
    public String features;
    public String memberid;
    public String email;
    public String accessid;
    public String uniqueid;
    public String memberType;
    public String dateTime;

    public long getPrimaryId() {
        return primaryid;
    }

    public void setPrimaryId(long primaryId) {
        this.primaryid = primaryId;
    }
    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getExpiretime() {
        return expiretime;
    }

    public void setExpiretime(String expiretime) {
        this.expiretime = expiretime;
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

    public String getMemberid() {
        return memberid;
    }

    public void setMemberid(String memberid) {
        this.memberid = memberid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessid() {
        return accessid;
    }

    public void setAccessid(String accessid) {
        this.accessid = accessid;
    }

    public String getUniqueid() {
        return uniqueid;
    }

    public void setUniqueid(String uniqueid) {
        this.uniqueid = uniqueid;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    @Override
    public String toString() {
        return "Members{" +
                " firstname='" + firstname + '\'' +
                " lastname='" + lastname + '\'' +
                ", memberid='" + memberid + '\'' +
                ", status='" + status + '\'' +
                ", mobile='" + mobile + '\'' +
                ", email='" + email + '\'' +
                ", accessid='"+ accessid +'\'' +
                ", uniqueid='"+ uniqueid +'\'' +
                ", image='" + image + '\'' +
                ", feature='"+ features +'\'' +
                '}';
    }
}
