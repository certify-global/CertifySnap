package com.certify.snap.api.response;

import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

public class MemberData {

    @PrimaryKey
    public long primaryId;

    @SerializedName("id")
    public String uniqueId;

    @SerializedName("faceTemplate")
    public String faceTemplate;

    @SerializedName("memberId")
    public String memberId;

    @SerializedName("titleType")
    public String titleType = "";

    @SerializedName("firstName")
    public String firstName;

    @SerializedName("middleName")
    public String middleName = "";

    @SerializedName("lastName")
    public String lastName;

    @SerializedName("accessId")
    public String accessId;

    @SerializedName("memberType")
    public String memberType;

    @SerializedName("memberTypeName")
    public String memberTypeName;

    @SerializedName("groupId")
    public String groupId;

    @SerializedName("groupTypeName")
    public String groupTypeName;

    @SerializedName("networkId")
    public String networkId;

    @SerializedName("fromDate")
    public String accessFromDate;

    @SerializedName("toDate")
    public String accessToDate;

    @SerializedName("email")
    public String email;

    @SerializedName("phoneNumber")
    public String phoneNumber;

    @SerializedName("status")
    public String status;

    @SerializedName("isdocument")
    public String isDocument;

    @SerializedName("certifyUniversalGuid")
    public String certifyUniversalGuid = "";

    public String imagePath = "";

    public boolean isMemberAccessed;

    public String dateTimeCheckInOut = "";
}
