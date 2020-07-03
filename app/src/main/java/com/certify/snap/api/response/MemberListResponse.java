package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MemberListResponse {

    @SerializedName("responseCode")
    public String responseCode;

    @SerializedName("responseSubCode")
    public String responseSubCode;

    @SerializedName("responseMessage")
    public String responseMessage;

    @SerializedName("responseData")
    public List<MemberListData> memberList;

}
