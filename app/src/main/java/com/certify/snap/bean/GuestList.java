package com.certify.snap.bean;

import java.util.List;

public class GuestList {
    private int code;
    private long servertime;
    private List<Guest> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getServertime() {
        return servertime;
    }

    public void setServertime(long servertime) {
        this.servertime = servertime;
    }

    public List<Guest> getData() {
        return data;
    }

    public void setData(List<Guest> data) {
        this.data = data;
    }
}
