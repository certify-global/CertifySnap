package com.certify.snap.api.response;

import com.certify.snap.model.TouchlessWaveSkip;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GetAdvancedWaveQuestions {
    @SerializedName("enableLogic")
    public int enableLogic;
    @SerializedName("logicJsonObject")
    public List<TouchlessWaveSkip> touchlessWaveSkips;
}
