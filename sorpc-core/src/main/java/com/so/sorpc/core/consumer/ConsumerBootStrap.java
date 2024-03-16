package com.so.sorpc.core.consumer;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.so.sorpc.core.annotation.SoRpcConsumer;
import com.so.sorpc.core.api.LoadBalancer;
import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.api.Router;
import com.so.sorpc.core.api.RpcContext;

import lombok.Data;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-11
 */
@Data
public class ConsumerBootStrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;

    Environment environment;

    private Map<String, Object> stub = new HashMap<>();   //获取到客户端接口调用存根

    public void start() {
        Router router = applicationContext.getBean(Router.class);
        LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);

        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);


        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);

            List<Field> fields = getAnnotatedFields(bean.getClass());


            for (Field field : fields) {
                System.out.println(" ===> " + field.getName());
                try {
                    Class<?> service = field.getType();
                    String serviceName = service.getCanonicalName();
                    //这就是stub.put操作
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
//                        consumer = creatConsumer(service, router, loadBalancer, providers);
                        consumer = creatConsumerFromRc(service, context, rc);
                    }
                    field.setAccessible(true);
                    field.set(bean, consumer);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private List<Field> getAnnotatedFields(Class<?> beanClass) {
        List<Field> annotatedFields = new ArrayList<>();
        //TODO
        while (beanClass != null) {
//            System.out.println("获取到的beanClass:" + beanClass.getCanonicalName());
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                if(field.isAnnotationPresent(SoRpcConsumer.class)) {
                    annotatedFields.add(field);
                }
            }
            beanClass = beanClass.getSuperclass();
        }
        return annotatedFields;
    }

    /**
     * 代理调用请求
     * @param service
     */
//   private Object  creatConsumer(Class<?> service,Router router,LoadBalancer loadBalancer,List<String> providers) {
//       return Proxy.newProxyInstance(service.getClassLoader(),
//               new Class[]{service}, new SoInvocationHandler(service, router,loadBalancer, providers));
//    }

    private Object creatConsumerFromRc(Class<?> service,RpcContext rpcContext, RegistryCenter registryCenter) {
            List<String> providers = registryCenter.fetchAll(service.getCanonicalName());
           return Proxy.newProxyInstance(service.getClassLoader(),
           new Class[]{service}, new SoInvocationHandler(service, rpcContext, providers));
    }
}
