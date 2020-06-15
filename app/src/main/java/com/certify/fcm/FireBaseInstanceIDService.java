package com.certify.fcm;

import com.google.firebase.iid.FirebaseInstanceId;

public class FireBaseInstanceIDService extends FireBaseMessagingService {
    private static final String TAG = "MyFirebaseIIDService";




    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
    }

}