package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class SettingsData {

    @SerializedName("deviceName")
    public String deviceName = "";

    @SerializedName("settingName")
    public String settingName = "Local";

    @SerializedName("settingVersion")
    public String settingVersion = "";

    @SerializedName("deviceMasterCode")
    public String deviceMasterCode = "";

    @SerializedName("jsonValue")
    public DeviceSettingsApi deviceSettingsApi;
}
