package com.certify.snap.bluetooth.model;

public class DeviceItem {
    private String deivceName;
    private String deviceAddress;

    public String getName(){return deivceName;}
    public String getAddress(){return deviceAddress;}

    public DeviceItem(String date, String content){
        this.deivceName=date;
        this.deviceAddress=content;
    }
}
