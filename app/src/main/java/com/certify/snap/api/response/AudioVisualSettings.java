package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class AudioVisualSettings {

    @PrimaryKey
    @NonNull
    public int primaryId;

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
