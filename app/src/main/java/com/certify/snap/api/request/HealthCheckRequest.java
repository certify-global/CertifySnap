package com.certify.snap.api.request;

import com.google.gson.annotations.SerializedName;

public class HealthCheckRequest {

    @SerializedName("lastUpdateDateTime")
    public String lastUpdateDateTime;

    @SerializedName("deviceSN")
    public String deviceSN;

    @SerializedName("deviceInfo")
    public String deviceInfo;

    @SerializedName("institutionId")
    public String institutionId;

    @SerializedName("appState")
    public String appState;

    @SerializedName("appUpTime")
    public String appUpTime;

    @SerializedName("deviceUpTime")
    public String deviceUpTime;

}
