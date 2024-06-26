package com.so.sorpc.core.consumer.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;

import lombok.SneakyThrows;

/**
 * interface for http invoker
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-21
 */
public interface HttpInvoker {

    Logger log  = LoggerFactory.getLogger(HttpInvoker.class);

    RpcResponse<?> post(RpcRequest rpcRequest, String url);

    HttpInvoker Default = new OkHttpInvoker(500);

    String post(String requestString, String url);

    String get(String url);

    @SneakyThrows
    static <T> T httpGet(String url, Class<T> clazz) {
        log.debug(" ===> httpGet: {}", url);
        String respJson = Default.get(url);
        log.debug(" ===> response: {}", respJson);
        return JSON.parseObject(respJson, clazz);
    }

    @SneakyThrows
    static <T> T httpGet(String url, TypeReference<T> typeReference) {
        log.debug(" ===> httpGet: {}", url);
        String respJson = Default.get(url);
        log.debug(" ===> response: {}", respJson);
        return JSON.parseObject(respJson, typeReference);
    }

    @SneakyThrows
    static <T> T httpPost(String requestString,String url, Class<T> clazz) {
        log.debug(" =====>>>>>> httpPost: " + url);
        String respJson = Default.post(requestString, url);
        log.debug(" =====>>>>>> response: " + respJson);
        return JSON.parseObject(respJson, clazz);
    }
}
