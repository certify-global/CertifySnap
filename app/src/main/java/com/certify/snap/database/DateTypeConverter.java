package com.certify.snap.database;

import androidx.room.TypeConverter;

import com.certify.snap.model.QuestionDataDb;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateTypeConverter {
    @TypeConverter
    public Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public Long dateToTimestamp(Date date) {
        if (date == null) {
            return null;
        } else {
            return date.getTime();
        }
    }

    @TypeConverter
    public static List<QuestionDataDb> stringToQuestions(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<QuestionDataDb>>() {}.getType();
        List<QuestionDataDb> questions = gson.fromJson(json, type);
        return questions;
    }

    @TypeConverter
    public static String questionsToString(List<QuestionDataDb> list) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<QuestionDataDb>>() {}.getType();
        String json = gson.toJson(list, type);
        return json;
    }

}
