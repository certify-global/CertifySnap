package com.certify.snap.bean;

import java.util.List;

/**
 * 作者    ljf
 * 时间    2019/7/30 0030 11:52
 * 文件    TestCoding
 * 描述
 */
public class MemberUpdate {
    private int code;
    private long servertime;
    private List<Members> data;

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

    public List<Members> getData() {
        return data;
    }

    public void setData(List<Members> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MemberUpdate{" +
                "code=" + code +
                ", servertime='" + servertime + '\'' +
                ", data=" + data +
                '}';
    }
}
