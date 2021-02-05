package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.certify.snap.database.DateTypeConverter;
import com.certify.snap.model.QuestionDataDb;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

@Entity(indices={@Index(value="primaryId", unique=true)})
@TypeConverters({DateTypeConverter.class})
public class GestureQuestionsDb {
    @PrimaryKey
    @NonNull
    public int primaryId;
    public List<QuestionDataDb> questionsDbList=null;

}
