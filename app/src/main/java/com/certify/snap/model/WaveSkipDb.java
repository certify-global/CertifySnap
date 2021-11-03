package com.certify.snap.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class WaveSkipDb {

    @PrimaryKey
    @NonNull
    public int primaryId;
    public String parentQuestionId = "";
    public String expectedOutcomeName = "";
    public String childQuestionId = "";
    public String expectedOutcome ="";
}
