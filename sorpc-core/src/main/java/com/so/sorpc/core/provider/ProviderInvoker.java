package com.so.sorpc.core.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.springframework.util.MultiValueMap;
import com.alibaba.fastjson.JSONObject;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;
import com.so.sorpc.core.exception.RpcException;
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

    public ProviderInvoker(ProviderBootStrap providerBootStrap) {
        this.skeleton = providerBootStrap.getSkeleton();
    }

    public RpcResponse<Object> invoke(RpcRequest rpcRequest) {
        List<ProviderMeta> providerMetas = skeleton.get(rpcRequest.getService());
        Object result = null;
        ProviderMeta meta =  providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(rpcRequest.getMethodSign()))
                .findFirst().orElse(null);
        Object bean =  meta.getServiceImpl();
        //查看是否通过反射拿到了对象  java.lang.Class
        log.debug("provider get bean class: "+ bean.getClass().getCanonicalName());
        Method method = meta.getMethod();
        RpcResponse<Object> response = new RpcResponse<>();
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
        }
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
