package com.auto.utils;

import com.alibaba.fastjson.JSON;
import okhttp3.Headers;

public class ResultMap {
    private int code;
    private String data;
    private String url;
    private Headers headers;

    public ResultMap(int code, String data, String url, Headers headers){
        this.setCode(code);
        this.setData(data);
        this.setUrl(url);
        this.setHeaders(headers);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
