package com.certify.snap.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class RegisteredMembers extends LitePalSupport {

    String firstname;
    String lastname;
    String expiretime;
    String status;
    String mobile;
    String image;
    String features;
    @Column(unique = true)
    String memberid;
    String email;
    @Column(unique = true)
    String accessid;
    String uniqueid;

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
