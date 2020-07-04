package com.certify.snap.bluetooth.data;

public class DeviceData {

    private String deviceName;
    private String deviceAddress;

    public DeviceData(String dvcName, String dvcAddress){
        this.deviceName = dvcName;
        this.deviceAddress = dvcAddress;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }


    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
