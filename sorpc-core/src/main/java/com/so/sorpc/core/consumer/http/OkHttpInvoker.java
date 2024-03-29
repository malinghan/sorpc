package com.so.sorpc.core.consumer.http;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-21
 */
@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    OkHttpClient client;

    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

    public OkHttpInvoker(int timeout) {
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }
    @Override
    public RpcResponse<?> post(RpcRequest rpcRequest, String url) {
        String reqJson = JSON.toJSONString(rpcRequest);
        log.debug(" ===> url = " + url);
        log.debug(" ===> reqJson = " + reqJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSONTYPE))
                .build();
        try {
            String respJson = Objects.requireNonNull(client.newCall(request).execute().body()).string();
            log.debug(" ===> respJson = " + respJson);
            return JSON.parseObject(respJson, RpcResponse.class);
        } catch (IOException e) {
            //e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
