package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class HomePageSettings {

    @PrimaryKey
    @NonNull
    public int primaryId;

    @SerializedName("logo")
    public String logo = "";

    @SerializedName("enableThermalCheck")
    public String enableThermalCheck = "";

    @SerializedName("line1")
    public String line1 = "";

    @SerializedName("line2")
    public String line2 = "";

    @SerializedName("enableHomeScreen")
    public String enableHomeScreen = "1";

    @SerializedName("viewIntervalDelay")
    public String viewIntervalDelay = "2";

    @SerializedName("enableTextOnly")
    public String enableTextOnly = "";

    @SerializedName("homeText")
    public String homeText = "";

}
