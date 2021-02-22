package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class IdentificationSettings {

    @PrimaryKey
    @NonNull
    public int primaryId;

    @Ignore
    @SerializedName("enableQRCodeScanner")
    public String enableQRCodeScanner = "0";

    @Ignore
    @SerializedName("enableAnonymousQRCode")
    public String enableAnonymousQRCode = "0";

    @Ignore
    @SerializedName("enableRFIDScanner")
    public String enableRFIDScanner = "0";

    @Ignore
    @SerializedName("identificationTimeout")
    public String identificationTimeout = "5";

    @Ignore
    @SerializedName("enableAcknowledgementScreen")
    public String enableAcknowledgementScreen = "0";

    @SerializedName("acknowledgementText")
    public String acknowledgementText = "";

    @Ignore
    @SerializedName("enableFacialRecognition")
    public String enableFacialRecognition = "0";

    @Ignore
    @SerializedName("facialThreshold")
    public String facialThreshold = "80";

    @Ignore
    @SerializedName("enableConfirmationNameAndImage")
    public String enableConfirmationNameAndImage = "0";

    @Ignore
    @SerializedName("cameraScanMode")
    public String cameraScanMode = "1";

}
