package com.certify.snap.bean;

public class Total {
    private int code;
    private long servertime;
    private String data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getTimeStamp() {
        return servertime;
    }

    public void setTimeStamp(long timeStamp) {
        this.servertime = timeStamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Normal{" +
                "code=" + code +
                ", timeStamp='" + servertime + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
