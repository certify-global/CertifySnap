package com.certify.callback;

import org.json.JSONObject;

public interface AddDeviceCallback {
    void onJSONObjectListenerAddDevice(JSONObject report, String status, JSONObject req);
}
