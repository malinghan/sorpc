package com.so.sorpc.core.filter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.so.sorpc.core.api.Filter;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;

/**
 * 优化策略：
 * 1. 定义cache的容量过期时间
 * 2. 指定CacheFilter的顺序
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-29
 */
public class CacheFilter implements Filter  {

    static Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object preFilter(RpcRequest rpcRequest) {
        return cache.get(rpcRequest.toString());
    }

    @Override
    public Object postFilter(RpcRequest rpcRequest, RpcResponse rpcResponse, Object result) {
        cache.putIfAbsent(rpcRequest.toString(), result);
        return result;
    }
}
