package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class ConfirmationViewSettings {

    @SerializedName("enableConfirmationScreen")
    public String enableConfirmationScreen = "1";

    @SerializedName("normalViewLine1")
    public String normalViewLine1 = "Have a nice day";

    @SerializedName("normalViewLine2")
    public String normalViewLine2 = "";

    @SerializedName("aboveThresholdViewLine1")
    public String aboveThresholdViewLine1 = "Please contact your supervisor before starting any work.";

    @SerializedName("temperatureAboveThreshold2")
    public String temperatureAboveThreshold2 = "";

    @SerializedName("viewDelay")
    public String viewDelay = "1";

    @SerializedName("enableConfirmationScreenAboveThreshold")
    public String enableConfirmationScreenAboveThreshold = "1";

    @SerializedName("viewDelayAboveThreshold")
    public String viewDelayAboveThreshold = "1";

}
