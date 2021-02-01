package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class DeviceSettingsData {

    @SerializedName("doNotSyncMembers")
    public String doNotSyncMembers = "";

    @SerializedName("syncMemberGroup")
    public String syncMemberGroup = "0";

    @SerializedName("groupId")
    public String groupId = "0";

    @SerializedName("navigationBar")
    public String navigationBar = "1";

    @SerializedName("multipleScanMode")
    public String multipleScanMode = "1";

    @SerializedName("deviceMasterCode")
    public String deviceMasterCode = "";

    @SerializedName("languageCode")
    public String languageCode = "1";

}
