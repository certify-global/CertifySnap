package com.certify.callback;

import org.json.JSONObject;

public interface JSONObjectCallbackLogin {
    void onJSONObjectListenerLogin(JSONObject report, String status, String req);
}
