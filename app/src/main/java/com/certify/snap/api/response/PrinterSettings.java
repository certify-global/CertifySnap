package com.certify.snap.api.response;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(indices={@Index(value="primaryId", unique=true)})
public class PrinterSettings {

    @PrimaryKey
    @NonNull
    public int primaryId;

    @Ignore
    @SerializedName("enableWBPrint")
    public String enableWBPrint = "0";

    @Ignore
    @SerializedName("enableUSBPrint")
    public String enableUSBPrint = "0";

    @Ignore
    @SerializedName("printAllScan")
    public String printAllScan = "0";

    @Ignore
    @SerializedName("printAccessCard")
    public String printAccessCard = "0";

    @Ignore
    @SerializedName("printQRCode")
    public String printQRCode = "0";

    @Ignore
    @SerializedName("printWaveUsers")
    public String printWaveUsers = "0";

    @Ignore
    @SerializedName("printHighTempScans")
    public String printHighTempScans = "0";

    @Ignore
    @SerializedName("printFace")
    public String printFace = "0";

    @Ignore
    @SerializedName("printName")
    public String printName = "0";

    @SerializedName("unidentifiedPrintText")
    public String unidentifiedPrintText = "0";

    @SerializedName("unidentifiedPrintTextValue")
    public String unidentifiedPrintTextValue = "Anonymous";

    @Ignore
    @SerializedName("printNormalTemperature")
    public String printNormalTemperature = "0";

    @Ignore
    @SerializedName("printHighTemperature")
    public String printHighTemperature = "0";

    @Ignore
    @SerializedName("printIndicatorForQR")
    public String printIndicatorForQR = "0";

    @Ignore
    @SerializedName("defaultBottomBarText")
    public String defaultBottomBarText = "";

    @Ignore
    @SerializedName("printWaveAnswers")
    public String printWaveAnswers = "0";

    @SerializedName("printWaveAnswerYes")
    public String printWaveAnswerYes = "1";

    @SerializedName("printWaveAnswerNo")
    public String printWaveAnswerNo = "0";

    @SerializedName("defaultResultPrint")
    public String defaultResultPrint = "";

}
