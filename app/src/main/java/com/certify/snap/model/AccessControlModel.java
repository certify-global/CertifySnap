package com.certify.snap.model;


import com.certify.snap.controller.DatabaseController;

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
        return DatabaseController.getInstance().isAccessIdExist(cardId);
    }

    public RegisteredMembers getRfidScanMatchedMember() {
        return currentScannedMember;
    }

    public void clearData() {
        currentScannedMember = null;
    }
}
