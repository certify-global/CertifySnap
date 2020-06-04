package com.certify.callback;

import org.json.JSONObject;

public interface MemberIDCallback {
    void onJSONObjectListenerMemberID(JSONObject report, String status, JSONObject req);
}
