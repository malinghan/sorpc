package com.so.sorpc.core.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.alibaba.fastjson.JSONObject;
import com.so.sorpc.core.annotation.SoRpcProvider;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;
import com.so.sorpc.core.meta.ProviderMeta;
import com.so.sorpc.core.utils.MethodUtils;
import com.so.sorpc.core.utils.TypeUtils;

import jakarta.annotation.PostConstruct;
import lombok.Data;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
@Data
public class ProviderBootStrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();   //获取到的服务接口存根


    @PostConstruct
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(SoRpcProvider.class);

        System.out.println("输出通过注解获取到的providers信息");
        providers.forEach((x,y) -> System.out.println("获取到的服务实现为: " + x));
        //把获取到的服务实现,放到skeleton中
        providers.values().forEach(x -> genInterface(x));
    }

    /**
     * 将接口信息存入skeleton
     * @param o
     */
    void genInterface(Object o) {
        //类型信息
        Class<?> c = o.getClass().getInterfaces()[0];
        System.out.println("获取到的接口信息为:" + c.getCanonicalName());
        Method[] methods = c.getMethods();
        List<ProviderMeta> providerMetas = new ArrayList<>();
        for (Method method : methods) {
//            if (MethodUtils.checkLocalMethod(method.getName())) {
//                continue;
//            }
            if (MethodUtils.checkIfObjectMethod(method)) {
                continue;
            }
            ProviderMeta providerMeta = createProviderMeta(c, o, method);
            providerMetas.add(providerMeta);
        }
        skeleton.put(c.getCanonicalName(), providerMetas);
    }

    private  ProviderMeta createProviderMeta(Class<?> c, Object x, Method method) {
        ProviderMeta meta = new ProviderMeta();
        meta.setMethod(method);
        meta.setServiceImpl(x);
        meta.setMethodSign(MethodUtils.methodSign(method));
        return meta;
    }



    public RpcResponse invoke(RpcRequest rpcRequest) {
        List<ProviderMeta> providerMetas = skeleton.get(rpcRequest.getService());
        Object result = null;
        ProviderMeta meta = null;
        for (ProviderMeta providerMeta : providerMetas) {
              if(providerMeta.getMethodSign().equals(rpcRequest.getMethodSign())) {
                    meta = providerMeta;
                    break;
              }
        }
        //TODO 空指针处理
        if (meta == null) {
            System.out.println("未通过request中service定义找到匹配的meta, service:" + rpcRequest.getService());
//            return  null;
        }
        Object bean =  meta.getServiceImpl();
        //查看是否通过反射拿到了对象  java.lang.Class
        System.out.println("服务调用方获取到的接口信息为: "+ bean.getClass().getCanonicalName());
        Method method = meta.getMethod();
        RpcResponse response = new RpcResponse();
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
