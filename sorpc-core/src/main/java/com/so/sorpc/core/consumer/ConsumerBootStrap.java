package com.so.sorpc.core.consumer;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.so.sorpc.core.annotation.SoRpcConsumer;

import lombok.Data;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-11
 */
@Data
public class ConsumerBootStrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> stub = new HashMap<>();   //获取到客户端接口调用存根

    public void start() {
        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);

            List<Field> fields = getAnnotatedFields(bean.getClass());

//            System.out.println("获取到的fields:{}" + fields.toString());

            for (Field field : fields) {
                System.out.println(" ===> " + field.getName());
                try {
                    Class<?> service = field.getType();
                    String serviceName = service.getCanonicalName();
                    //这就是stub.put操作
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
                        consumer = creatConsumer(service);
                        //stub.put(serviceName, consumer);
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
   private Object  creatConsumer(Class<?> service) {
       return Proxy.newProxyInstance(service.getClassLoader(),
               new Class[]{service}, new SoInvocationHandler(service));
    }
}
