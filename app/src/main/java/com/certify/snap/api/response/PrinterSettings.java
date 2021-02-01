package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class PrinterSettings {

    @SerializedName("enableWBPrint")
    public String enableWBPrint = "0";

    @SerializedName("enableUSBPrint")
    public String enableUSBPrint = "0";

    @SerializedName("printAllScan")
    public String printAllScan = "0";

    @SerializedName("printAccessCard")
    public String printAccessCard = "0";

    @SerializedName("printQRCode")
    public String printQRCode = "0";

    @SerializedName("printWaveUsers")
    public String printWaveUsers = "0";

    @SerializedName("printHighTempScans")
    public String printHighTempScans = "0";

    @SerializedName("printFace")
    public String printFace = "0";

    @SerializedName("printName")
    public String printName = "0";

    @SerializedName("unidentifiedPrintText")
    public String unidentifiedPrintText = "0";

    @SerializedName("unidentifiedPrintTextValue")
    public String unidentifiedPrintTextValue = "Anonymous";

    @SerializedName("printNormalTemperature")
    public String printNormalTemperature = "0";

    @SerializedName("printHighTemperature")
    public String printHighTemperature = "0";

    @SerializedName("printIndicatorForQR")
    public String printIndicatorForQR = "0";

    @SerializedName("defaultBottomBarText")
    public String defaultBottomBarText = "";

    @SerializedName("printWaveAnswers")
    public String printWaveAnswers = "0";

    @SerializedName("printWaveAnswerYes")
    public String printWaveAnswerYes = "1";

    @SerializedName("printWaveAnswerNo")
    public String printWaveAnswerNo = "0";

    @SerializedName("defaultResultPrint")
    public String defaultResultPrint = "";

}
