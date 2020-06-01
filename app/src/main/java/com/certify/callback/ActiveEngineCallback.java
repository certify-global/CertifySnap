package com.certify.callback;

import org.json.JSONObject;

public interface ActiveEngineCallback {
    void onActiveEngineCallback(Boolean activeStatus, String status, JSONObject req);
}
