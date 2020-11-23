package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class QuestionData {

    @SerializedName("institutionId")
    public int institutionId;

    @SerializedName("userId")
    public int userId;

    @SerializedName("id")
    public int id;

    @SerializedName("questionName")
    public String questionName;

    @SerializedName("title")
    public String title;

    @SerializedName("surveyQuestionaryDetails")
    public String surveyQuestionaryDetails;

    @SerializedName("surveyOptions")
    public ArrayList<QuestionSurveyOptions> surveyOptions;

    @SerializedName("settingId")
    public int settingId;

    @SerializedName("expectedOutcome")
    public String expectedOutcome;
}
