package com.certify.snap.model;


import com.certify.snap.common.AppSettings;
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
        boolean result = false;
        List<RegisteredMembers> membersList = DatabaseController.getInstance().findMemberByAccessId(cardId);
        if (membersList != null && membersList.size() > 0) {
            currentScannedMember = membersList.get(0);
            result = true;
        }
        if (AppSettings.isMobileAccessCard()) {
            List<RegisteredMembers> members = DatabaseController.getInstance().isUniqueIdExist(cardId);
            if (members != null && members.size() > 0) {
                currentScannedMember = membersList.get(0);
                result = true;
            }
        }
        return result;
    }

    public RegisteredMembers getRfidScanMatchedMember() {
        return currentScannedMember;
    }

    public void clearData() {
        currentScannedMember = null;
    }
}
