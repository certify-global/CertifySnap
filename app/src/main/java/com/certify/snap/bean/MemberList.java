package com.certify.snap.bean;

import java.util.List;

/**
 * 作者    ljf
 * 时间    2019/7/30 0030 11:52
 * 文件    TestCoding
 * 描述
 */
public class MemberList {
    private int code;
    private String error;
    private List<Members> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<Members> getData() {
        return data;
    }

    public void setData(List<Members> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MemberList{" +
                "code=" + code +
                ", error='" + error + '\'' +
                ", data=" + data +
                '}';
    }
}
