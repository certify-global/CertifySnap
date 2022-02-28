package com.certify.snap.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class QrCodeStore {
    @PrimaryKey
    @NonNull
    public long primaryId;
    public String guid;
    public String dateTime;
}
