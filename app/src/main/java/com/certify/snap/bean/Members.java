package com.certify.snap.bean;

/**
 * 作者    ljf
 * 时间    2019/6/13 0013 12:37
 * 文件    Telpo_Face_Demo_0610
 * 描述
 */
public class Members {
    String userId;
    String name;
    String expire_time;
    String status;
    String mobile;
    String image;

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

    @Override
    public String toString() {
        return "Members{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", expire_time='" + expire_time + '\'' +
                ", status='" + status + '\'' +
                ", mobile='" + mobile + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
