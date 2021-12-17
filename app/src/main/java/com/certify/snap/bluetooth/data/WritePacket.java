package com.certify.snap.bluetooth.data;

public class WritePacket {
    private String certifyId;
    private String accessId;
    private String mp;

    public String getCertifyId() {
        return certifyId;
    }

    public void setCertifyId(String certifyId) {
        this.certifyId = certifyId;
    }

    public String getAccessId() {
        return accessId;
    }

    public void setAccessId(String id) {
        this.accessId = id;
    }

    public String getModel() {
        return mp;
    }

    public void setModel(String model) {
        this.mp = model;
    }

}
