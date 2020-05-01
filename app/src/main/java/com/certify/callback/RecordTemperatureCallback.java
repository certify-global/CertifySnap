package com.certify.callback;

import org.json.JSONObject;

public interface RecordTemperatureCallback {
    void onJSONObjectListenertemperature(String report, String status, JSONObject req);
}
