package com.certify.callback;

import org.json.JSONObject;

public interface FlowListCallback {
    void onJSONObjectListenerFlowList(JSONObject report, String status, JSONObject req);
}
