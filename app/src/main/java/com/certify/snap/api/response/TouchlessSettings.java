package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class TouchlessSettings {

    @SerializedName("enableWave")
    public String enableWave = "0";

    @SerializedName("enableQuestionAndAnswer")
    public String enableQuestionAndAnswer = "0";

    @SerializedName("settingId")
    public String settingId = "0";

    @SerializedName("exitOnNegativeOutcome")
    public String exitOnNegativeOutcome = "0";

    @SerializedName("messageForNegativeOutcome")
    public String messageForNegativeOutcome = "Please contact Admin";

    @SerializedName("enableMaskEnforcement")
    public String enableMaskEnforcement = "0";

    @SerializedName("maskEnforceText")
    public String maskEnforceText = "Please wear a mask and Wave Left Hand to start over or else Wave Right Hand to continue.";

    @SerializedName("enableVoice")
    public String enableVoice = "0";

    @SerializedName("showWaveImage")
    public String showWaveImage = "0";

    @SerializedName("showWaveProgress")
    public String showWaveProgress = "0";

    @SerializedName("waveIndicatorInstructions")
    public String waveIndicatorInstructions = "Hold your palm on side of the screen where indicate to answer. If you have to start over, hold both hands.";

}
