package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class HomePageSettings {

    @PrimaryKey
    @NonNull
    public int primaryId;

    @Ignore
    @SerializedName("logo")
    public String logo = "";

    @Ignore
    @SerializedName("enableThermalCheck")
    public String enableThermalCheck = "";

    @SerializedName("line1")
    public String line1 = "";

    @SerializedName("line2")
    public String line2 = "";

    @Ignore
    @SerializedName("enableHomeScreen")
    public String enableHomeScreen = "1";

    @Ignore
    @SerializedName("viewIntervalDelay")
    public String viewIntervalDelay = "2";

    @Ignore
    @SerializedName("enableTextOnly")
    public String enableTextOnly = "";

    @SerializedName("homeText")
    public String homeText = "";

}
