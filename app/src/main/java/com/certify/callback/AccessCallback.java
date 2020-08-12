package com.certify.callback;

import org.json.JSONObject;

public interface AccessCallback {
    void onJSONObjectListenerAccess(JSONObject report, String status, JSONObject req);
}
