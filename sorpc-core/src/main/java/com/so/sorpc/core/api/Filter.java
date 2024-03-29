package com.so.sorpc.core.api;

/**
 * 过滤器
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-17
 */
public interface Filter {

    Object preFilter(RpcRequest rpcRequest);

    Object postFilter(RpcRequest request, RpcResponse response, Object result);

    Filter Default = new Filter() {
        @Override
        public Object preFilter(RpcRequest rpcRequest) {
            return null;
        }

        @Override
        public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
            return null;
        }
    };

//    Filter next() {}
}
