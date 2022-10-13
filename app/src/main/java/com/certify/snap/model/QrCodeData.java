package com.certify.snap.model;

public class QrCodeData {
    private String uniqueId;
    private String firstName;
    private String lastName;
    private String trqStatus;
    private String memberId;
    private String accessId;
    private String qrCodeId;
    private int memberTypeId;
    private String memberTypeName;
    private String faceTemplate;
    private String isVisitor = "0";

    public String getIsVisitor() {
        return isVisitor;
    }

    public void setIsVisitor(String isVisitor) {
        this.isVisitor = isVisitor;
    }

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

    public int getMemberTypeId() {
        return memberTypeId;
    }

    public void setMemberTypeId(int memberTypeId) {
        this.memberTypeId = memberTypeId;
    }

    public String getMemberTypeName() {
        return memberTypeName;
    }

    public void setMemberTypeName(String memberTypeName) {
        this.memberTypeName = memberTypeName;
    }

    public String getFaceTemplate() {
        return faceTemplate;
    }

    public void setFaceTemplate(String faceTemplate) {
        this.faceTemplate = faceTemplate;
    }

}
