package com.certify.callback;

import org.json.JSONObject;

public interface RecordTemperatureCallback {
    void onJSONObjectListenerTemperature(JSONObject report, String status, JSONObject req);
}
