package com.certify.snap.api.request;

import com.google.gson.annotations.SerializedName;

public class DeviceInfo {

    @SerializedName("osVersion")
    public String osVersion;

    @SerializedName("appVersion")
    public String appVersion;

    @SerializedName("mobileIp")
    public String mobileIp;

    @SerializedName("mobileNumber")
    public String mobileNumber;

    @SerializedName("uniqueDeviceId")
    public String uniqueDeviceId;

    @SerializedName("IMEINumber")
    public String imeiNumber;

    @SerializedName("deviceModel")
    public String deviceModel;

    @SerializedName("deviceSN")
    public String deviceSerialNumber;

    @SerializedName("batteryStatus")
    public int batteryStatus;

    @SerializedName("networkStatus")
    public boolean networkStatus;

    @SerializedName("appState")
    public String appState;
}
