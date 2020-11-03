package com.certify.snap.model;

import java.util.ArrayList;

public class GestureQuestionData {
    private String questionName;
    private ArrayList<GestureQuestionOptionsData> gestureQuestionOptionsData=new ArrayList<>();


    public ArrayList<GestureQuestionOptionsData> getGestureQuestionOptionsData() {
        return gestureQuestionOptionsData;
    }

    public void setGestureQuestionOptionsData(ArrayList<GestureQuestionOptionsData> gestureQuestionOptionsData) {
        this.gestureQuestionOptionsData = gestureQuestionOptionsData;
    }


    public String getQuestionName() {
        return questionName;
    }
    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }




}
