package com.certify.snap.bean;

import java.util.List;

/**
 * 作者    ljf
 * 时间    2019/8/23 0023 14:49
 * 文件    Telpo_Face_system
 * 描述
 */
public class Failed {
    private int code;
    private String error;
    private List<Data> data ;

    public void setCode(int code){
        this.code = code;
    }
    public int getCode(){
        return this.code;
    }
    public void setError(String error){
        this.error = error;
    }
    public String getError(){
        return this.error;
    }
    public void setData(List<Data> data){
        this.data = data;
    }
    public List<Data> getData(){
        return this.data;
    }

    class Data{

    }

    @Override
    public String toString() {
        return "Failed{" +
                "code=" + code +
                ", error='" + error + '\'' +
                ", data=" + data +
                '}';
    }
}
