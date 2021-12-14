package com.certify.snap.api.request;

import com.google.gson.annotations.SerializedName;

public class GetTokenRequest {

    @SerializedName("DeviceSN")
    public String deviceSN;
}
