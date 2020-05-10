package com.certify.callback;

import org.json.JSONObject;

public interface RecordTemperatureCallback {
    void onJSONObjectListenerTemperature(String report, String status, JSONObject req);
}
