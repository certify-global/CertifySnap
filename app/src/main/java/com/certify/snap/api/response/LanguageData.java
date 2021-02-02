package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(indices={@Index(value="languageId", unique=true)})
public class LanguageData {

    @SerializedName("name")
    public String name;

    @SerializedName("languageCode")
    public String languageCode;

    @SerializedName("sourceFile")
    public String sourceFile;

    @PrimaryKey
    @NonNull
    @SerializedName("languageId")
    public int languageId;

    @SerializedName("fileCode")
    public String fileCode;

    public boolean offline;
}
