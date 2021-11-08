package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetWaveQuestionList {

    @SerializedName("surveyQuestionary")
    public List<QuestionData> questionList;
    @SerializedName("logicJson")
    public GetAdvancedWaveQuestions logicJsonAdvan;
}
