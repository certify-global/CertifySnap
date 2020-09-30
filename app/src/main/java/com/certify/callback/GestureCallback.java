package com.certify.callback;

import org.json.JSONObject;

public interface GestureCallback {
    void onJSONObjectListenerGesture(JSONObject report, String status, JSONObject req);
}
