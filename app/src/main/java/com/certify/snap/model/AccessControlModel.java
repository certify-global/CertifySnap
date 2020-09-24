package com.certify.snap.model;


import com.certify.snap.controller.DatabaseController;

import java.util.List;

public class AccessControlModel {

    private static final String TAG = AccessControlModel.class.getSimpleName();
    private static AccessControlModel mInstance = null;
    private RegisteredMembers currentScannedMember = null;

    public static AccessControlModel getInstance() {
        if (mInstance == null) {
            mInstance = new AccessControlModel();
        }
        return mInstance;
    }

    public boolean isMemberMatch(String cardId) {
        List<RegisteredMembers> membersList = DatabaseController.getInstance().findMemberByAccessId(cardId);
        if (membersList != null && membersList.size() > 0) {
            currentScannedMember = membersList.get(0);
            return true;
        }
        return false;
    }

    public RegisteredMembers getRfidScanMatchedMember() {
        return currentScannedMember;
    }

    public void clearData() {
        currentScannedMember = null;
    }
}
