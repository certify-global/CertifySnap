package com.certify.callback;

import org.json.JSONObject;

public interface GetLanguagesCallback {
    void onJSONObjectListenerGetLanguages(JSONObject report, String status, JSONObject req);
}
