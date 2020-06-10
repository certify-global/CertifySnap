package com.certify.callback;

import org.json.JSONObject;

public interface ManageMemberCallback {
    void onJSONObjectListenerManageMember(String report, String status, JSONObject req);
}
