package com.so.sorpc.core.consumer.http;

import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-21
 */
public interface HttpInvoker {
    RpcResponse<?> post(RpcRequest rpcRequest, String url);
}
