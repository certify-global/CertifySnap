package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class DeviceSettingsData {

    @PrimaryKey
    @NonNull
    public int primaryId;

    @Ignore
    @SerializedName("doNotSyncMembers")
    public String doNotSyncMembers = "";

    @Ignore
    @SerializedName("syncMemberGroup")
    public String syncMemberGroup = "0";

    @SerializedName("groupId")
    public String groupId = "0";

    @Ignore
    @SerializedName("navigationBar")
    public String navigationBar = "1";

    @Ignore
    @SerializedName("multipleScanMode")
    public String multipleScanMode = "1";

    @SerializedName("deviceMasterCode")
    public String deviceMasterCode = "";

    @SerializedName("languageId")
    public String languageId = "1";

}
