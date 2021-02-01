package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class AudioVisualSettings {

    @SerializedName("enableSoundForValidQRCode")
    public String enableSoundForValidQRCode = "0";

    @SerializedName("enableSoundForInvalidQRCode")
    public String enableSoundForInvalidQRCode = "0";

    @SerializedName("enableLightOnNormalTemperature")
    public String enableLightOnNormalTemperature = "0";

    @SerializedName("enableLightOnHighTemperature")
    public String enableLightOnHighTemperature = "0";

    @SerializedName("audioForValidQRCode")
    public String audioForValidQRCode = "";

    @SerializedName("audioForInvalidQRCode")
    public String audioForInvalidQRCode = "";

}
