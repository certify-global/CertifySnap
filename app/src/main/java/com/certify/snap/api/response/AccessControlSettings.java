package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class AccessControlSettings {

    @SerializedName("enableAutomaticDoors")
    public String enableAutomaticDoors = "0";

    @SerializedName("allowAnonymous")
    public String allowAnonymous = "0";

    @SerializedName("blockAccessHighTemperature")
    public String blockAccessHighTemperature = "0";

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
}
