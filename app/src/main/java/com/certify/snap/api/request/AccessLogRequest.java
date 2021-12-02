package com.certify.snap.api.request;

import com.google.gson.annotations.SerializedName;

public class AccessLogRequest {
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

    @SerializedName("networkId")
    public String networkId;

    @SerializedName("temperature")
    public float temperature;

    @SerializedName("qrCodeId")
    public String qrCodeId;

    @SerializedName("deviceId")
    public String deviceId;

    @SerializedName("deviceName")
    public String deviceName;

    @SerializedName("institutionId")
    public String institutionId;

    @SerializedName("facilityId")
    public int facilityId;

    @SerializedName("locationId")
    public int locationId;

    @SerializedName("facilityName")
    public String facilityName;

    @SerializedName("locationName")
    public String locationName;

    @SerializedName("deviceTime")
    public String deviceTime;

    @SerializedName("timezone")
    public String timezone;

    @SerializedName("sourceIP")
    public String sourceIP;

    @SerializedName("deviceData")
    public DeviceInfo deviceData;

    @SerializedName("guid")
    public String guid;

    @SerializedName("faceParameters")
    public String faceParameters;

    @SerializedName("eventType")
    public String eventType;

    @SerializedName("evenStatus")
    public String evenStatus;

    @SerializedName("utcRecordDate")
    public String utcRecordDate;

    @SerializedName("loggingMode")
    public int loggingMode;

    @SerializedName("accessOption")
    public int accessOption;

    @SerializedName("attendanceMode")
    public int attendanceMode;

    @SerializedName("allowAccess")
    public boolean allowAccess;

    @SerializedName("offlineSync")
    public int offlineSync;

    @SerializedName("utcOfflineDateTime")
    public String utcOfflineDateTime;

}
