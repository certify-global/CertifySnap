package com.certify.callback;

import org.json.JSONObject;

public interface JSONObjectCallback {
    void onJSONObjectListener(String report, String status, JSONObject req);
}
