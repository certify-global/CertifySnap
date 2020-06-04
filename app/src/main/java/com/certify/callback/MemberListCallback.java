package com.certify.callback;

import org.json.JSONObject;

public interface MemberListCallback {
    void onJSONObjectListenerMemberList(JSONObject report, String status, JSONObject req);
}
