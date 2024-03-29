package com.so.sorpc.core.consumer;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.so.sorpc.core.api.Filter;
import com.so.sorpc.core.api.RpcContext;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;
import com.so.sorpc.core.consumer.http.HttpInvoker;
import com.so.sorpc.core.consumer.http.OkHttpInvoker;
import com.so.sorpc.core.meta.InstanceMeta;
import com.so.sorpc.core.utils.MethodUtils;
import com.so.sorpc.core.utils.TypeUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-11
 */
@Slf4j
public class SoInvocationHandler implements InvocationHandler {

    Class<?> service;
    RpcContext rpcContext;
    List<InstanceMeta> providers;
    HttpInvoker httpInvoker = new OkHttpInvoker();

    public SoInvocationHandler(Class<?> service, RpcContext rpcContext,
            List<InstanceMeta> providers) {
        this.service = service;
        this.rpcContext = rpcContext;
        this.providers = providers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)  {
        if (MethodUtils.checkLocalMethod(method)) {
            return null;
        }
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);

        for (Filter filter : this.rpcContext.getFilters()) {
            Object objectResult = filter.preFilter(rpcRequest);
            if (objectResult != null) {
                log.info(filter.getClass().getName() + "==> prefilter" + objectResult);
                return objectResult;
            }
        }

        List<InstanceMeta> instanceMetas = rpcContext.getRouter().choose(providers);
        InstanceMeta instanceMeta = rpcContext.getLoadBalancer().choose(instanceMetas);

        log.info("post request url:" + instanceMeta.toUrl());
        RpcResponse<?> rpcResponse =  httpInvoker.post(rpcRequest, instanceMeta.toUrl());

        Object objectResult = castMethodResponse(method, rpcResponse);

        for (Filter filter : this.rpcContext.getFilters()) {
            objectResult = filter.postFilter(rpcRequest, rpcResponse, objectResult);
        }
        return objectResult;
    }

    @Nullable
    private Object castMethodResponse(Method method, RpcResponse<?> rpcResponse) {
        //判断状态
        if (rpcResponse.isStatus()) {
            //反序列化为object
            Object data = rpcResponse.getData();
            //这里的类型转换很复杂
            return TypeUtils.cast(data, method.getReturnType());
//            return TypeUtils.castByMethod(data, method);
        } else {
                Exception ex = rpcResponse.getEx();
                throw new RuntimeException(ex);
        }
    }
}
