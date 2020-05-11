package com.certify.callback;

import org.json.JSONObject;

public interface SettingCallback {
    void onJSONObjectListenerSetting(JSONObject report, String status, JSONObject req);
}
