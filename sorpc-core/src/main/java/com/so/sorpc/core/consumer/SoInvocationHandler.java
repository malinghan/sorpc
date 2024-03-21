package com.so.sorpc.core.consumer;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import com.so.sorpc.core.api.RpcContext;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;
import com.so.sorpc.core.consumer.http.HttpInvoker;
import com.so.sorpc.core.consumer.http.OkHttpInvoker;
import com.so.sorpc.core.meta.InstanceMeta;
import com.so.sorpc.core.utils.MethodUtils;
import com.so.sorpc.core.utils.TypeUtils;
/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-11
 */
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
        if (MethodUtils.checkIfObjectMethod(method)) {
            return null;
        }
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);

        List<InstanceMeta> instanceMetas = rpcContext.getRouter().choose(providers);
        InstanceMeta instanceMeta = rpcContext.getLoadBalancer().choose(instanceMetas);

        System.out.println("post request url:" + instanceMeta.toUrl());
        RpcResponse<?> rpcResponse =  httpInvoker.post(rpcRequest, instanceMeta.toUrl());

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
