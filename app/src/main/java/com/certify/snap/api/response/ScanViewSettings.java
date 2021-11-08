package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class ScanViewSettings {

    @PrimaryKey
    @NonNull
    public int primaryId;

    @Ignore
    @SerializedName("displayTemperatureDetail")
    public String displayTemperatureDetail = "1";

    @Ignore
    @SerializedName("captureUserImageAboveThreshold")
    public String captureUserImageAboveThreshold = "1";

    @Ignore
    @SerializedName("captureAllUsersImage")
    public String captureAllUsersImage = "0";

    @Ignore
    @SerializedName("enableSoundOnNormalTemperature")
    public String enableSoundOnNormalTemperature = "0";

    @Ignore
    @SerializedName("enableSoundOnHighTemperature")
    public String enableSoundOnHighTemperature = "0";

    @Ignore
    @SerializedName("viewDelay")
    public String viewDelay = "3";

    @Ignore
    @SerializedName("temperatureThreshold")
    public String temperatureThreshold = "100.4";

    @Ignore
    @SerializedName("temperatureFormat")
    public String temperatureFormat = "F";

    @Ignore
    @SerializedName("allowLowTemperatureScanning")
    public String allowLowTemperatureScanning = "0";

    @Ignore
    @SerializedName("closeProximityScan")
    public String closeProximityScan = "0";

    @Ignore
    @SerializedName("enableLiveness")
    public String enableLiveness = "0";

    @Ignore
    @SerializedName("lowTemperatureThreshold")
    public String lowTemperatureThreshold = "93.2";

    @Ignore
    @SerializedName("enableMaskDetection")
    public String enableMaskDetection = "0";

    @Ignore
    @SerializedName("temperatureCompensation")
    public String temperatureCompensation = "0.0";

    @SerializedName("temperatureNormal")
    public String temperatureNormal = "";

    @SerializedName("temperatureHigh")
    public String temperatureHigh = "";

    @Ignore
    @SerializedName("displayResultBar")
    public String displayResultBar = "1";

    @Ignore
    @SerializedName("scanType")
    public String scanType = "1";

    @Ignore
    @SerializedName("enableTemperatureScan")
    public String enableTemperatureScan = "1";

    @Ignore
    @SerializedName("audioForNormalTemperature")
    public String audioForNormalTemperature = "";

    @Ignore
    @SerializedName("audioForHighTemperature")
    public String audioForHighTemperature = "";

    @Ignore
    @SerializedName("retryOptionFaceScan")
    public String retryOptionFaceScan = "";
}
