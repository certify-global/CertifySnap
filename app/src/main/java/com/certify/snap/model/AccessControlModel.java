package com.certify.snap.model;


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
        /* List<RegisteredMembers> membersList = LitePal.where("accessid = ?", cardId).find(RegisteredMembers.class);
       if (membersList != null && membersList.size() > 0) {
            currentScannedMember = membersList.get(0);
            return true;
        }*/
        return false;
    }

    public RegisteredMembers getRfidScanMatchedMember() {
        return currentScannedMember;
    }

    public void clearData() {
        currentScannedMember = null;
    }
}
