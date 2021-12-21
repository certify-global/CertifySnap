package com.certify.snap.bluetooth.data;

public class WritePacket {
    private String guid;
    private String mp;

    public String getModel() {
        return mp;
    }

    public void setModel(String model) {
        this.mp = model;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
}
