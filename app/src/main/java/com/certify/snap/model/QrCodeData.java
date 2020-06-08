package com.certify.snap.model;

public class QrCodeData {
    private String uniqueId;
    private String firstName;
    private String lastName;
    private String trqStatus;
    private String memberId;
    private String accessId;
    private String qrCodeId;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String snapId) {
        this.uniqueId = snapId;
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

    public String getTrqStatus() {
        return trqStatus;
    }

    public void setTrqStatus(String trqStatus) {
        this.trqStatus = trqStatus;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getAccessId() {
        return accessId;
    }

    public void setAccessId(String accessId) {
        this.accessId = accessId;
    }

    public String getQrCodeId() {
        return qrCodeId;
    }

    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
    }
}
