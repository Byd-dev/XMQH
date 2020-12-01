package com.pro.switchlibrary;

public class SDEntity {
    String appid;
    String url;


    @Override
    public String toString() {
        return "SDEntity{" +
                "appid='" + appid + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    public SDEntity(String appid, String url) {
        this.appid = appid;
        this.url = url;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
