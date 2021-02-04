package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.certify.snap.model.QuestionDataDb;

import java.util.List;


@Entity(indices={@Index(value="primaryId", unique=true)})
public class GestureQuestionsDb {

    @PrimaryKey
    @NonNull
    public int primaryId;
    public List<QuestionDataDb> questionsDbList;

}
