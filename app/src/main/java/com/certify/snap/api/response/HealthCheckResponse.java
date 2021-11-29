package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class HealthCheckResponse {

    @SerializedName("responseCode")
    public int responseCode;

    @SerializedName("responseSubCode")
    public String responseSubCode;

    @SerializedName("responseMessage")
    public String responseMessage;

    @SerializedName("responseData")
    public String responseData;

    @SerializedName("Message")
    public String message = "";
}
