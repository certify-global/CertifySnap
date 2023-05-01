package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class AccessLogResponse {

    @SerializedName("responseCode")
    public int responseCode;

    @SerializedName("responseSubCode")
    public int responseSubCode = 0;

    @SerializedName("responseMessage")
    public String responseMessage;

    @SerializedName("responseData")
    public String responseData;

}
