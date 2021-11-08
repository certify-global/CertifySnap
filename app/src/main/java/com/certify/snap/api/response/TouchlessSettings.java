package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class TouchlessSettings {

    @PrimaryKey
    @NonNull
    public int primaryId;

    @Ignore
    @SerializedName("enableWave")
    public String enableWave = "0";

    @Ignore
    @SerializedName("enableQuestionAndAnswer")
    public String enableQuestionAndAnswer = "0";

    @Ignore
    @SerializedName("settingId")
    public String settingId = "0";

    @Ignore
    @SerializedName("exitOnNegativeOutcome")
    public String exitOnNegativeOutcome = "0";

    @SerializedName("messageForNegativeOutcome")
    public String messageForNegativeOutcome = "Please contact Admin";

    @Ignore
    @SerializedName("enableMaskEnforcement")
    public String enableMaskEnforcement = "0";

    @SerializedName("maskEnforceText")
    public String maskEnforceText = "Please wear a mask and Wave Left Hand to start over or else Wave Right Hand to continue.";

    @Ignore
    @SerializedName("enableVoice")
    public String enableVoice = "0";

    @Ignore
    @SerializedName("showWaveImage")
    public String showWaveImage = "0";

    @Ignore
    @SerializedName("showWaveProgress")
    public String showWaveProgress = "0";

    @SerializedName("waveIndicatorInstructions")
    public String waveIndicatorInstructions = "Hold your palm on side of the screen where indicate to answer. If you have to start over, hold both hands.";

    @Ignore
    @SerializedName("enableTouchMode")
    public String enableTouchMode = "0";
}
