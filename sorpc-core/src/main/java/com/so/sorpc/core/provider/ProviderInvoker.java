package com.so.sorpc.core.provider;

import static com.so.sorpc.core.exception.RpcException.ExceedLimitEx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;
import com.alibaba.fastjson.JSONObject;
import com.so.sorpc.core.api.RpcContext;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;
import com.so.sorpc.core.config.ProviderConfigProperties;
import com.so.sorpc.core.exception.RpcException;
import com.so.sorpc.core.governance.SlidingTimeWindow;
import com.so.sorpc.core.meta.ProviderMeta;
import com.so.sorpc.core.utils.TypeUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-21
 */
@Data
@Slf4j
public class ProviderInvoker {

    private MultiValueMap<String, ProviderMeta> skeleton;

    //服务端流控滑动窗口
    final Map<String, SlidingTimeWindow> windows = new HashMap<>();
    final ProviderConfigProperties providerConfigProperties;

    public ProviderInvoker(ProviderBootStrap providerBootStrap) {
        this.skeleton = providerBootStrap.getSkeleton();
        this.providerConfigProperties = providerBootStrap.getProviderProperties();
    }

    public RpcResponse<Object> invoke(RpcRequest rpcRequest) {
        log.debug(" ===> ProviderInvoker.invoke(request:{})", rpcRequest);
        //put contextParameters into request
        if (!rpcRequest.getParams().isEmpty()) {
            rpcRequest.getParams().forEach(RpcContext::setContextParameters);
        }
        RpcResponse<Object> response = new RpcResponse<>();
        String service = rpcRequest.getService();
        //define traffic control for every service
        int trafficControl = Integer.parseInt(providerConfigProperties.getMetas().getOrDefault("tc", "20"));
        log.debug(" ===> trafficControl: {} for {}", trafficControl, service);

        synchronized (windows) {
            SlidingTimeWindow window = windows.computeIfAbsent(service, k -> new SlidingTimeWindow());
            if (window.calcSum() > trafficControl) {
                log.debug("windows:{}", windows);
                throw new RpcException(
                        "service" + service + " invoke in 30s/[" + window.getSum() + " ] larger than tpsLimit = "
                                + trafficControl, ExceedLimitEx);
            }
            //traffic control record
            window.record(System.currentTimeMillis());
            log.debug("service {} in window with {}", service, window.getSum());
        }


        List<ProviderMeta> providerMetas = skeleton.get(rpcRequest.getService());
        Object result = null;
        ProviderMeta meta =  providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(rpcRequest.getMethodSign()))
                .findFirst().orElse(null);
        Object bean =  meta.getServiceImpl();
        //查看是否通过反射拿到了对象  java.lang.Class
        log.debug("provider get bean class: "+ bean.getClass().getCanonicalName());
        Method method = meta.getMethod();

        try {
            //provider侧反序列化处理
            Object[] args = processArgs(rpcRequest.getArgs(), method.getParameterTypes());
            result = method.invoke(bean, args);
            log.debug("provider process result: "+ JSONObject.toJSONString(result));
            response.setData(result);
            response.setStatus(true);
        } catch (IllegalAccessException e) {
            response.setEx(new RpcException(e.getMessage()));
        } catch (InvocationTargetException e) {
            response.setEx(new RpcException(e.getTargetException().getMessage()));
        } finally {
            RpcContext.contextParameters.get().clear();
        }
        log.debug(" ===> ProviderInvoker.invoke(response:{})", response);
        return response;
    }

    /**
     * 处理返回
     * @param args
     * @param parameterTypes
     * @return
     */
    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
        if (args == null || parameterTypes == null || args.length == 0 || parameterTypes.length == 0) { return args;}
        int length = args.length;
        Object[] res = new Object[length];
        for (int i = 0; i < length; i++) {
            res[i] = TypeUtils.cast(args[i], parameterTypes[i]);
        }
        return res;
    }
}
