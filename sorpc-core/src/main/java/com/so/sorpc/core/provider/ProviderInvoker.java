package com.so.sorpc.core.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.springframework.util.MultiValueMap;
import com.alibaba.fastjson.JSONObject;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;
import com.so.sorpc.core.meta.ProviderMeta;
import com.so.sorpc.core.utils.TypeUtils;

import lombok.Data;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-21
 */
@Data
public class ProviderInvoker {

    private MultiValueMap<String, ProviderMeta> skeleton;

    public ProviderInvoker(ProviderBootStrap providerBootStrap) {
        this.skeleton = providerBootStrap.getSkeleton();
    }

    public RpcResponse<Object> invoke(RpcRequest rpcRequest) {
        List<ProviderMeta> providerMetas = skeleton.get(rpcRequest.getService());
        Object result = null;
        ProviderMeta meta = null;
        for (ProviderMeta providerMeta : providerMetas) {
            if(providerMeta.getMethodSign().equals(rpcRequest.getMethodSign())) {
                meta = providerMeta;
                break;
            }
        }
        if (meta == null) {
            System.out.println("未通过request中service定义找到匹配的meta, service:" + rpcRequest.getService());
        }
        Object bean =  meta.getServiceImpl();
        //查看是否通过反射拿到了对象  java.lang.Class
        System.out.println("服务调用方获取到的接口信息为: "+ bean.getClass().getCanonicalName());
        Method method = meta.getMethod();
        RpcResponse<Object> response = new RpcResponse<>();
        try {
            //provider侧反序列化处理
            Object[] args = processArgs(rpcRequest.getArgs(), method.getParameterTypes());
            result = method.invoke(bean, args);
            System.out.println("服务调用方获取到的输出: "+ JSONObject.toJSONString(result));
            response.setData(result);
            response.setStatus(true);
        } catch (IllegalAccessException e) {
            response.setEx(new RuntimeException(e.getMessage()));
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            response.setEx(new RuntimeException(e.getTargetException().getMessage()));
            e.printStackTrace();
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
