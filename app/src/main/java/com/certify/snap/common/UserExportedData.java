package com.certify.snap.common;

import android.graphics.Bitmap;

import com.certify.snap.faceserver.CompareResult;
import com.certify.snap.model.QrCodeData;
import com.certify.snap.model.RegisteredMembers;

public class UserExportedData {
    public Bitmap rgb;
    public Bitmap ir;
    public Bitmap thermal;
    public RegisteredMembers member;
    public int faceScore;
    public String temperature;
    public boolean sendImages;
    public boolean exceedsThreshold;
    public String maskStatus;
    public CompareResult compareResult;
    private QrCodeData qrCodeData;  //TODO1: Optimize
    public String triggerType = "";

    public UserExportedData() {
        this.member = new RegisteredMembers();
    }

    public UserExportedData(Bitmap rgb, Bitmap ir, RegisteredMembers member, int faceScore) {
        this.rgb = rgb;
        this.ir = ir;
        this.member = member;
        this.faceScore = faceScore;
    }

    public QrCodeData getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(QrCodeData qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    @Override
    public String toString() {
        return "UserExportedData{" +
                "member=" + member +
                ", faceScore=" + faceScore +
                ", temperature='" + temperature + '\'' +
                ", sendImages=" + sendImages +
                ", exceedsThreshold=" + exceedsThreshold +
                ", maskStatus='" + maskStatus + '\'' +
                '}';
    }
}