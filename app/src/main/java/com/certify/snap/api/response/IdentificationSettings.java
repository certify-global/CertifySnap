package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class IdentificationSettings {

    @SerializedName("enableQRCodeScanner")
    public String enableQRCodeScanner = "0";

    @SerializedName("enableAnonymousQRCode")
    public String enableAnonymousQRCode = "0";

    @SerializedName("enableRFIDScanner")
    public String enableRFIDScanner = "0";

    @SerializedName("identificationTimeout")
    public String identificationTimeout = "5";

    @SerializedName("enableAcknowledgementScreen")
    public String enableAcknowledgementScreen = "0";

    @SerializedName("acknowledgementText")
    public String acknowledgementText = "";

    @SerializedName("enableFacialRecognition")
    public String enableFacialRecognition = "0";

    @SerializedName("facialThreshold")
    public String facialThreshold = "80";

    @SerializedName("enableConfirmationNameAndImage")
    public String enableConfirmationNameAndImage = "0";

    @SerializedName("cameraScanMode")
    public String cameraScanMode = "1";

}
