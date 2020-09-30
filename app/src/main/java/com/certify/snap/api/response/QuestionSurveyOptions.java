package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class QuestionSurveyOptions {

    @SerializedName("questionId")
    public int questionId;

    @SerializedName("optionId")
    public int optionId;

    @SerializedName("name")
    public String name;

}
