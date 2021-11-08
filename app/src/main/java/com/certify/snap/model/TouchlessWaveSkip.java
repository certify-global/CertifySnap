package com.certify.snap.model;

import com.google.gson.annotations.SerializedName;

public class TouchlessWaveSkip {
    @SerializedName("parentQuestion")
    public String parentQuestion = "";
    @SerializedName("expectedOutcomeName")
    public String expectedOutcomeName = "";
    @SerializedName("childQuestion")
    public String childQuestion = "";

}
