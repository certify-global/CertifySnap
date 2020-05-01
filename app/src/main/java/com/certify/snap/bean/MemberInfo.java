package com.certify.snap.bean;

/**
 * 作者    ljf
 * 时间    2019/7/30 0030 11:52
 * 文件    TestCoding
 * 描述
 */
public class MemberInfo {
    private int code;
    private String error;
    private Info data;

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

    public Info getData() {
        return data;
    }

    public void setData(Info data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MemberInfo{" +
                "code=" + code +
                ", error='" + error + '\'' +
                ", data=" + data +
                '}';
    }

    private class Info{
        private String userId;
        private String name;
        private String head_url;
        private String status;
        private String expire_time;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHead_url() {
            return head_url;
        }

        public void setHead_url(String head_url) {
            this.head_url = head_url;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getExpire_time() {
            return expire_time;
        }

        public void setExpire_time(String expire_time) {
            this.expire_time = expire_time;
        }

        @Override
        public String toString() {
            return "Info{" +
                    "userId='" + userId + '\'' +
                    ", name='" + name + '\'' +
                    ", head_url='" + head_url + '\'' +
                    ", status='" + status + '\'' +
                    ", expire_time='" + expire_time + '\'' +
                    '}';
        }
    }

}
