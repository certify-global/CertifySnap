package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class AccessControlSettings {

    @PrimaryKey
    @NonNull
    public int primaryId;

    @SerializedName("enableAutomaticDoors")
    public String enableAutomaticDoors = "0";

    @SerializedName("allowAnonymous")
    public String allowAnonymous = "0";

    @SerializedName("blockAccessOnHighTemperature")
    public String blockAccessOnHighTemperature = "0";

    @SerializedName("doorControlTimeWired")
    public int doorControlTimeWired = 5;

    @SerializedName("enableAccessControl")
    public String enableAccessControl = "0";

    @SerializedName("accessControllerCardFormat")
    public int accessControllerCardFormat = 26;

    @SerializedName("loggingMode")
    public int loggingMode = 0;

    @SerializedName("validAccessOption")
    public int validAccessOption = 4;

    @SerializedName("relayMode")
    public String relayMode = "1";

    @SerializedName("enableWeigandPassThrough")
    public String enableWeigandPassThrough = "0";

    @SerializedName("attendanceMode")
    public int attendanceMode = 1;

    @SerializedName("truncateZeroes")
    public String truncateZeroes;

}
