package com.certify.callback;

import org.json.JSONObject;

public interface GestureAnswerCallback {
    void onJSONObjectListenerGestureAnswer(JSONObject report, String status, JSONObject req);
}
