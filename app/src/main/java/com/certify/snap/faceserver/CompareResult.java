package com.certify.snap.faceserver;


import java.io.Serializable;

public class CompareResult implements Serializable {
    private String userName;
    private float similar;
    private int trackId;
    private String memberId;
    private String temperature;
    private String lastName;

    public String getFacialScore() {
        return facialScore;
    }

    public void setFacialScore(String facialScore) {
        this.facialScore = facialScore;
    }

    private String facialScore;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

    public CompareResult(String userName, float similar) {
        this.userName = userName;
        this.similar = similar;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public float getSimilar() {
        return similar;
    }

    public void setSimilar(float similar) {
        this.similar = similar;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "CompareResult{" +
                "userName='" + userName + '\'' +
                ", similar=" + similar +
                ", trackId=" + trackId +
                ", memberId='" + memberId + '\'' +
                ", facialScore='" + facialScore + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
