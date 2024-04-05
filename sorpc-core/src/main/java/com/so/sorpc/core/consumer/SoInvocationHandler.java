package com.so.sorpc.core.consumer;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.so.sorpc.core.api.Filter;
import com.so.sorpc.core.api.RpcContext;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;
import com.so.sorpc.core.consumer.http.HttpInvoker;
import com.so.sorpc.core.consumer.http.OkHttpInvoker;
import com.so.sorpc.core.exception.RpcException;
import com.so.sorpc.core.governance.SlidingTimeWindow;
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
    final List<InstanceMeta> providers;

    final Set<InstanceMeta> isolatedProviders = new HashSet<>();

    final Set<InstanceMeta> halfOpenProviders = new HashSet<>();

    final Map<String, SlidingTimeWindow> windows = new HashMap<>();
    HttpInvoker httpInvoker;

    ScheduledThreadPoolExecutor executor;

    public SoInvocationHandler(Class<?> service, RpcContext rpcContext,
            List<InstanceMeta> providers) {
        this.service = service;
        this.rpcContext = rpcContext;
        this.providers = providers;
        int timeout = Integer.parseInt(rpcContext.getParameters()
                .getOrDefault("consumer.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);
        int halfOpenInitialDelay = Integer.parseInt(rpcContext.getParameters()
                .getOrDefault("consumer.halfOpenInitialDelay", "10000"));
        int halfOpenDelay = Integer.parseInt(rpcContext.getParameters()
                .getOrDefault("consumer.halfOpenDelay", "60000"));
        executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleWithFixedDelay(this::halfOpen, halfOpenInitialDelay, halfOpenDelay, TimeUnit.MILLISECONDS);
    }

    private void halfOpen() {
        log.debug("halfOpen schedule start");
        halfOpenProviders.clear();
        halfOpenProviders.addAll(isolatedProviders);
        log.debug("halfOpen schedule end");
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
        //retries handler
        int retries = Integer.parseInt(rpcContext.getParameters().getOrDefault("consumer.retries", "1"));
        int faultLimit = Integer.parseInt(rpcContext.getParameters().getOrDefault("consumer.faultLimit", "10"));

        //if SocketTimeoutException occur, retry, else return
        while (retries-- > 0) {
            log.debug(" ===> reties: " + retries);
        try {
                for (Filter filter : this.rpcContext.getFilters()) {
                    Object objectResult = filter.preFilter(rpcRequest);
                    if (objectResult != null) {
                        log.debug(filter.getClass().getName() + "==> prefilter" + objectResult);
                        return objectResult;
                    }
                }

                InstanceMeta instance;
                RpcResponse<?> rpcResponse;
                Object objectResult;
                synchronized (halfOpenProviders) {
                    if(halfOpenProviders.isEmpty()) {
                        List<InstanceMeta> instanceMetas = rpcContext.getRouter().choose(providers);
                        instance = rpcContext.getLoadBalancer().choose(instanceMetas);
                        log.debug(" loadBalancer.choose(instances) ==> {}", instance);
                    } else {
                        instance = halfOpenProviders.stream().findFirst().orElse(null);
                        log.debug(" halfOpen.choose(instances) ==> {}", instance);
                    }
                }
                String instanceSign = instance.toUrl();
                try {
                    rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());
                    objectResult = castMethodResponse(method, rpcResponse);
                } catch (Exception e) {
                    log.debug(" exception occur ==> {}", instance);
                    //use sliding time window handle exception statistics
                    //when 10 exception occur in 30s, isolate
                    synchronized(windows) {
                        SlidingTimeWindow window = windows.computeIfAbsent(instanceSign, k -> new SlidingTimeWindow());
                        window.record(System.currentTimeMillis());
                        log.debug("instance {} in window with {}", instanceSign, window.getSum());
                        if (window.getSum() > faultLimit) {
                            isolate(instance);
                        }
                    }
                    throw e;
                }

                synchronized (providers) {
                    if (!providers.contains(instance)) {
                        isolatedProviders.remove(instance);
                        providers.add(instance);
                        log.debug("instance {} is recovered, isolatedProviders={}, providers={}", instance, isolatedProviders, providers);
                    }
                }

                for (Filter filter : this.rpcContext.getFilters()) {
                    Object filterResult = filter.postFilter(rpcRequest, rpcResponse, objectResult);
                    if (filterResult != null) {
                        return filterResult;
                    }
                }
                return objectResult;
            } catch (Exception ex) {
                if (!(ex.getCause() instanceof SocketTimeoutException)) {
                    throw ex;
                }
        }
    }
        return null;
    }

    private void isolate(InstanceMeta instance) {
        log.debug(" ==> isolate instance: " + instance);
        isolatedProviders.add(instance);
        log.debug(" ==> providers before = {}", providers);
        providers.remove(instance);
        log.debug(" ==> isolatedProviders after = {}", isolatedProviders);
    }

    @Nullable
    private Object castMethodResponse(Method method, RpcResponse<?> rpcResponse) {
        //判断状态
        if (rpcResponse.isStatus()) {
            //反序列化为object
            Object data = rpcResponse.getData();
            //这里的类型转换很复杂
//            return TypeUtils.cast(data, method.getReturnType());
            return TypeUtils.castByMethod(data, method);
        } else {
                RpcException ex = rpcResponse.getEx();
                if (ex != null) {
                    log.error("response error,", ex);
                    throw ex;
                }
                return null;

        }
    }
}
