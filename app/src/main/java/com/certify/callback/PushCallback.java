package com.certify.callback;

import org.json.JSONObject;

public interface PushCallback {
    void onJSONObjectListenerPush(JSONObject report, String status, JSONObject req);
}
