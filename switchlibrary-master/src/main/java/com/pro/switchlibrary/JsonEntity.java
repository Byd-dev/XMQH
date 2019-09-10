package com.pro.switchlibrary;

import java.util.List;

public class JsonEntity {




    /**
     * status : 0
     * url : https://ab.76bao.hk
     */

    private String status;
    private String url;
    private List<String> dPool;
    private List<String> fPool;
    private List<String> dBlog;
    private List<String> fBlog;

    @Override
    public String toString() {
        return "JsonEntity{" +
                "status='" + status + '\'' +
                ", url='" + url + '\'' +
                ", dPool=" + dPool +
                ", fPool=" + fPool +
                ", dBlog=" + dBlog +
                ", fBlog=" + fBlog +
                '}';
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public List<String> getDPool() {
        return dPool;
    }

    public void setDPool(List<String> dPool) {
        this.dPool = dPool;
    }

    public List<String> getFPool() {
        return fPool;
    }

    public void setFPool(List<String> fPool) {
        this.fPool = fPool;
    }

    public List<String> getDBlog() {
        return dBlog;
    }

    public void setDBlog(List<String> dBlog) {
        this.dBlog = dBlog;
    }

    public List<String> getFBlog() {
        return fBlog;
    }

    public void setFBlog(List<String> fBlog) {
        this.fBlog = fBlog;
    }
}
