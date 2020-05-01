package com.certify.snap.model;

import org.litepal.crud.LitePalSupport;

public class OfflineGuestMembers extends LitePalSupport {
    String userId;
    String verify_time;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVerify_time() {
        return verify_time;
    }

    public void setVerify_time(String verify_time) {
        this.verify_time = verify_time;
    }
}
