package com.auto.utils;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class DingTalk {
    private final static Logger log = LoggerFactory.getLogger(DingTalk.class);

    public static void sendMessageDingding(String message) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"msgtype\": \"text\",\"text\": {\"content\": \""+message+" \"}}");
        log.info("调用钉钉入参 : " + body.toString());
        Request request = new Request.Builder()
                .url("https://oapi.dingtalk.com/robot/send?access_token=5beff9ced692139bc81b0fea78d1ce78a655d3ce1b9625ecdff2cbdcf080fbd9")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        log.info("调用钉钉结果 : " + response.toString());
    }
}
