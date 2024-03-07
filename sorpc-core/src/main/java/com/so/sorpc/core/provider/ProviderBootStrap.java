package com.so.sorpc.core.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.so.sorpc.core.annotation.SoRpcProvider;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;

import jakarta.annotation.PostConstruct;
import lombok.Data;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
@Data
public class ProviderBootStrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> skeleton = new HashMap<>();   //获取到的服务接口存根


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
        skeleton.put(c.getCanonicalName(), o);
    }

    public RpcResponse invoke(RpcRequest rpcRequest) {
        Object bean = skeleton.get(rpcRequest.getService());
        if (bean == null) {
            System.out.println("未通过request中service定义找到匹配的bean, service:" + rpcRequest.getService());
        }
        //查看是否通过反射拿到了对象  java.lang.Class
        System.out.println("服务调用方获取到的接口信息为: "+ bean.getClass().getCanonicalName());

        Method method = findMethod(bean.getClass(), rpcRequest.getMethod());
        if (method == null) {
            System.out.println("通过request中的method信息寻找到的方法为: null");
        }
        System.out.println("通过request中的method信息寻找到的方法为:" + method.getName());
        Object result = null;
        try {
            result = method.invoke(bean, rpcRequest.getArgs());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return new RpcResponse(true, result);
    }

//    Method findMethod(Class<?> c, String methodName) {
//        Method method = null;
//        try {
//            method = c.getDeclaredMethod(methodName);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
//        return method;
//    }


    private Method findMethod(Class<?> aClass, String methodName) {
        for (Method method : aClass.getMethods()) {
            if(method.getName().equals(methodName)) {  // 有多个重名方法，
                return method;
            }
        }
        return null;
    }
}
