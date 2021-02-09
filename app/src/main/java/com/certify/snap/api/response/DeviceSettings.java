package com.certify.snap.api.response;

import com.google.gson.annotations.SerializedName;

public class DeviceSettings {

    @SerializedName("DeviceSettings")
    public DeviceSettingsData deviceSettingsData;

    @SerializedName("HomePageView")
    public HomePageSettings homePageView;

    @SerializedName("ScanView")
    public ScanViewSettings scanView;

    @SerializedName("ConfirmationView")
    public ConfirmationViewSettings confirmationView;

    @SerializedName("GuideMessages")
    public GuideSettings guideMessages;

    @SerializedName("IdentificationSettings")
    public IdentificationSettings identificationSettings;

    @SerializedName("AccessControl")
    public AccessControlSettings accessControl;

    @SerializedName("AudioVisualAlerts")
    public AudioVisualSettings audioVisualAlerts;

    @SerializedName("TouchlessInteraction")
    public TouchlessSettings touchlessInteraction;

    @SerializedName("PrinterSettings")
    public PrinterSettings printerSettings;

    @SerializedName("configuredLanguageCode")
    public String languageCode = "en";

    @SerializedName("configuredLanguageId")
    public int languageId = 0;
}
