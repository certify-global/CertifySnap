package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LanguageListResponse {

    @SerializedName("responseCode")
    public String responseCode;

    @SerializedName("responseSubCode")
    public String responseSubCode;

    @SerializedName("responseMessage")
    public String responseMessage;

    @SerializedName("responseData")
    public List<LanguageData> languageList;

}
