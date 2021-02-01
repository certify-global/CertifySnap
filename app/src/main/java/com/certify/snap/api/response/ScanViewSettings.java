package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class ScanViewSettings {

    @SerializedName("displayTemperatureDetail")
    public String displayTemperatureDetail = "1";

    @SerializedName("captureUserImageAboveThreshold")
    public String captureUserImageAboveThreshold = "1";

    @SerializedName("captureAllUsersImage")
    public String captureAllUsersImage = "0";

    @SerializedName("enableSoundOnNormalTemperature")
    public String enableSoundOnNormalTemperature = "0";

    @SerializedName("enableSoundOnHighTemperature")
    public String enableSoundOnHighTemperature = "0";

    @SerializedName("viewDelay")
    public String viewDelay = "3";

    @SerializedName("temperatureThreshold")
    public String temperatureThreshold = "100.4";

    @SerializedName("temperatureFormat")
    public String temperatureFormat = "F";

    @SerializedName("allowLowTemperatureScanning")
    public String allowLowTemperatureScanning = "0";

    @SerializedName("closeProximityScan")
    public String closeProximityScan = "0";

    @SerializedName("enableLiveness")
    public String enableLiveness = "0";

    @SerializedName("lowTemperatureThreshold")
    public String lowTemperatureThreshold = "93.2";

    @SerializedName("enableMaskDetection")
    public String enableMaskDetection = "0";

    @SerializedName("temperatureCompensation")
    public String temperatureCompensation = "0.0";

    @SerializedName("temperatureNormal")
    public String temperatureNormal = "";

    @SerializedName("temperatureHigh")
    public String temperatureHigh = "";

    @SerializedName("displayResultBar")
    public String displayResultBar = "1";

    @SerializedName("scanType")
    public String scanType = "1";

    @SerializedName("enableTemperatureScan")
    public String enableTemperatureScan = "1";

    @SerializedName("audioForNormalTemperature")
    public String audioForNormalTemperature = "";

    @SerializedName("audioForHighTemperature")
    public String audioForHighTemperature = "";

}
