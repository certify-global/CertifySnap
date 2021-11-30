package com.certify.snap.api.request;

import com.google.gson.annotations.SerializedName;

public class TemperatureRecordRequest {

    @SerializedName("deviceId")
    public String deviceId;

    @SerializedName("temperature")
    public String temperature;

    @SerializedName("institutionId")
    public String institutionId;

    @SerializedName("facilityId")
    public int facilityId;

    @SerializedName("locationId")
    public int locationId;

    @SerializedName("deviceTime")
    public String deviceTime;

    @SerializedName("trigger")
    public String trigger;

    @SerializedName("machineTemperature")
    public String machineTemperature;

    @SerializedName("ambientTemperature")
    public String ambientTemperature;

    @SerializedName("irTemplate")
    public String irTemplate;

    @SerializedName("rgbTemplate")
    public String rgbTemplate;

    @SerializedName("thermalTemplate")
    public String thermalTemplate;

    @SerializedName("deviceData")
    public DeviceInfo deviceData;

    @SerializedName("deviceParameters")
    public String deviceParameters;

    @SerializedName("temperatureFormat")
    public String temperatureFormat;

    @SerializedName("exceedThreshold")
    public boolean exceedThreshold;

    @SerializedName("id")
    public String id;

    @SerializedName("accessId")
    public String accessId;

    @SerializedName("firstName")
    public String firstName;

    @SerializedName("lastName")
    public String lastName;

    @SerializedName("memberId")
    public String memberId;

    @SerializedName("memberTypeId")
    public String memberTypeId;

    @SerializedName("memberTypeName")
    public String memberTypeName;

    @SerializedName("trqStatus")
    public String trqStatus;

    @SerializedName("networkId")
    public String networkId;

    @SerializedName("qrCodeId")
    public String qrCodeId;

    @SerializedName("maskStatus")
    public String maskStatus;

    @SerializedName("faceScore")
    public int faceScore;

    @SerializedName("faceParameters")
    public String faceParameters;

    @SerializedName("utcTime")
    public String utcTime;

    @SerializedName("utcOfflineDateTime")
    public String utcOfflineDateTime;

    @SerializedName("offlineSync")
    public boolean offlineSync;
}
