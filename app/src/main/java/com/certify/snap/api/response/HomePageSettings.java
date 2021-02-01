package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class HomePageSettings {

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
