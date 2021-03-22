package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class GuideSettings {

    @PrimaryKey
    @NonNull
    public int primaryId;

    @Ignore
    @SerializedName("enableGuideMessages")
    public String enableGuideMessages = "1";

    @SerializedName("message1")
    public String message1;

    @SerializedName("message2")
    public String message2;

    @SerializedName("message3")
    public String message3;

    @SerializedName("message4")
    public String message4;

}
