package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class GetTokenResponse {

    @SerializedName("responseCode")
    public String responseCode;

    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("token_type")
    public String tokenType;

    @SerializedName("InstitutionID")
    public String institutionId;

    @SerializedName(".expires")
    public String expiryTime;

    @SerializedName("command")
    public String command;

    @SerializedName("expires_in")
    public long expiresIn;

    @SerializedName(".issued")
    public String issued;

}
