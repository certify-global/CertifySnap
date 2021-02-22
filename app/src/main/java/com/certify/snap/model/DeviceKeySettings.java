package com.certify.snap.model;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices={@Index(value="id", unique=true)})
public class DeviceKeySettings {

    @PrimaryKey
    @NonNull
    public long id;
    public String settingName;
    public String settingValue;
}
