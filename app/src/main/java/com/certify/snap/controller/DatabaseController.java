package com.certify.snap.controller;

import com.certify.snap.model.RegisteredMembers;

import org.litepal.LitePal;

import java.util.List;

public class DatabaseController {
    private static final String TAG = DatabaseController.class.getSimpleName();
    private static DatabaseController mInstance = null;

    public static DatabaseController getInstance() {
        if (mInstance == null) {
            mInstance = new DatabaseController();
        }
        return mInstance;
    }

    public boolean isMemberExist(String memberId) {
        List<RegisteredMembers> membersList = LitePal.where("memberid = ?", memberId).find(RegisteredMembers.class);
        return membersList != null && membersList.size() > 0;
    }

    public boolean isAccessIdExist(String accessId) {
        List<RegisteredMembers> membersList = LitePal.where("accessid = ?", accessId).find(RegisteredMembers.class);
        return membersList == null && membersList.size() > 0;
    }

    public List<RegisteredMembers> findMember(String memberId) {
        return LitePal.where("memberid = ?", memberId).find(RegisteredMembers.class);
    }

    public int deleteMember(String memberId) {
        return LitePal.deleteAll(RegisteredMembers.class, "memberid = ?", memberId);
    }
}
