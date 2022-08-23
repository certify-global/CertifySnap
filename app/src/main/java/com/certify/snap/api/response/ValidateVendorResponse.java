package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class ValidateVendorResponse {

    @SerializedName("responseCode")
    public int responseCode;

    @SerializedName("responseSubCode")
    public String responseSubCode;

    @SerializedName("responseMessage")
    public String responseMessage;

    @SerializedName("responseData")
    public VendorResponseData VendorResponseData;

}
