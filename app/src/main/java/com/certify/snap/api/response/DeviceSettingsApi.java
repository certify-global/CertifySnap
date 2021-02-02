package com.certify.snap.api.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DeviceSettingsApi {

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

    @Nullable
    @SerializedName("settings")
    public List<DeviceSettings> settings;

}
