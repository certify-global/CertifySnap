package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class ConfirmationViewSettings {

    @PrimaryKey
    @NonNull
    public int primaryId;

    @Ignore
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

    @Ignore
    @SerializedName("viewDelay")
    public String viewDelay = "1";

    @Ignore
    @SerializedName("enableConfirmationScreenAboveThreshold")
    public String enableConfirmationScreenAboveThreshold = "1";

    @Ignore
    @SerializedName("viewDelayAboveThreshold")
    public String viewDelayAboveThreshold = "1";

    @Ignore
    @SerializedName("showVaccinationIndicator")
    public String showVaccinationIndicator = "1";

    @Ignore
    @SerializedName("showNonVaccinationIndicator")
    public String showNonVaccinationIndicator = "1";

}
