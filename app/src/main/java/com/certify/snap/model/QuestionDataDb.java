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
    public Integer institutionId = 0;
    public Integer userId = 0;
    public Integer id = 0;
    public String questionName;
    public String title;
    public String surveyQuestionaryDetails;
    public String surveyOptions;
    public Integer settingId = 0;
    public String expectedOutcome;
    public String languageCode;

}
