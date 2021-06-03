package com.certify.callback;

import org.json.JSONObject;

public interface GetLastCheckinTimeCallback {
    void onJSONObjectListenerGetCheckInTime(JSONObject report, String status, JSONObject req);
}
