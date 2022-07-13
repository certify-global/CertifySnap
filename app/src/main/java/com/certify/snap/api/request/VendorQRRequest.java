package com.certify.snap.api.request;

import com.certify.snap.common.Util;
import com.google.gson.annotations.SerializedName;

public class VendorQRRequest {

    @SerializedName("vendorGuid")
    public String vendorGuid;

    @SerializedName("deviceSNo")
    public String deviceSNo;

}
