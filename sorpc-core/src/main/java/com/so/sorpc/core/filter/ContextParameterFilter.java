package com.so.sorpc.core.filter;

import java.util.Map;

import com.so.sorpc.core.api.Filter;
import com.so.sorpc.core.api.RpcContext;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-04-05
 */
public class ContextParameterFilter implements Filter {
    @Override
    public Object preFilter(RpcRequest rpcRequest) {
        Map<String, String> params = RpcContext.contextParameters.get();
        if (!params.isEmpty()) {
            rpcRequest.getParams().putAll(params);
        }
        //if not null, processor will directly return
        return null;
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        RpcContext.contextParameters.get().clear();
        //if not null, processor will directly return
        return null;
    }
}
