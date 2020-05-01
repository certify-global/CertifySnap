package com.certify.snap.bean;

/**
 * 作者    ljf
 * 时间    2019/7/30 0030 11:52
 * 文件    TestCoding
 * 描述
 */
public class Secret {
    private int code;
    private String error;
    private Data data;

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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Secret{" +
                "code=" + code +
                ", error="+ error +
                ", data=" + data +
                '}';
    }

    public class Data{
        private String client_id;
        private String client_secret;
        private String access_limit;
        private String device_password;
        private String wallpaper;

        public String getClient_id() {
            return client_id;
        }

        public void setClient_id(String client_id) {
            this.client_id = client_id;
        }

        public String getClient_secret() {
            return client_secret;
        }

        public void setClient_secret(String client_secret) {
            this.client_secret = client_secret;
        }

        public String getAccess_limit() {
            return access_limit;
        }

        public void setAccess_limit(String access_limit) {
            this.access_limit = access_limit;
        }

        public String getDevice_password() {
            return device_password;
        }

        public void setDevice_password(String device_password) {
            this.device_password = device_password;
        }

        public String getWallpaper() {
            return wallpaper;
        }

        public void setWallpaper(String wallpaper) {
            this.wallpaper = wallpaper;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "client_id='" + client_id + '\'' +
                    ", client_secret='" + client_secret + '\'' +
                    ", access_limit='" + access_limit + '\'' +
                    ", device_password='" + device_password + '\'' +
                    ", wallpaper='" + wallpaper + '\'' +
                    '}';
        }
    }
}
