package com.certify.callback;

import org.json.JSONObject;

public interface QRCodeCallback {
    void onJSONObjectListenerQRCode(JSONObject report, String status, JSONObject req);
}
