package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class SettingsResponse {

    @SerializedName("responseCode")
    public String responseCode;

    @SerializedName("responseSubCode")
    public String responseSubCode;

    @SerializedName("responseMessage")
    public String responseMessage;

    @SerializedName("responseData")
    public SettingsData settingsData;

}
