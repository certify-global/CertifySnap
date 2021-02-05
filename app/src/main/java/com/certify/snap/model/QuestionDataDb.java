package com.certify.snap.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class QuestionDataDb {

    @PrimaryKey
    @NonNull
    public int primaryId;
    public int institutionId;
    public int userId;
    public int id;
    public String questionName;
    public String title;
    public String surveyQuestionaryDetails;
    public String surveyOptions;
    public int settingId;
    public String expectedOutcome;

}
