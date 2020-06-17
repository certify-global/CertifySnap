package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class MemberListData {

    @SerializedName("id")
    public String id;

    @SerializedName("memberId")
    public String memberId;

    @SerializedName("accessId")
    public String accessId;
}
