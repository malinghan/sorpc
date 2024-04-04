package com.so.sorpc.core.consumer;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.so.sorpc.core.annotation.SoRpcConsumer;
import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.api.RpcContext;
import com.so.sorpc.core.meta.InstanceMeta;
import com.so.sorpc.core.meta.ServiceMeta;
import com.so.sorpc.core.utils.MethodUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-11
 */
@Data
@Slf4j
public class ConsumerBootStrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;

    Environment environment;

    private String app;
    private String namespace;
    private String env;
    private int retries;
    private int timeout;

    private Map<String, Object> stub = new HashMap<>();   //获取到客户端接口调用存根

    public void start() {
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        RpcContext context = applicationContext.getBean(RpcContext.class);

        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);

            List<Field> fields = MethodUtils.getAnnotatedFields(bean.getClass(), SoRpcConsumer.class);

            for (Field field : fields) {
                log.debug(" ===> " + field.getName());
                Class<?> service = field.getType();
                String serviceName = service.getCanonicalName();
                try {
                    //这就是stub.put操作
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
                        consumer = creatConsumerFromRc(service, context, rc);
                        stub.put(serviceName, consumer);
                    }
                    field.setAccessible(true);
                    field.set(bean, consumer);
                } catch (Exception e) {
                    log.warn(" ==> Field[{}.{}] create consumer failed.", serviceName, field.getName());
                    log.error("Ignore and print it as: ", e);
                }
            }
        }
    }

    private Object creatConsumerFromRc(Class<?> service,RpcContext rpcContext, RegistryCenter registryCenter) {
             String serviceName = service.getCanonicalName();
            ServiceMeta serviceMeta = ServiceMeta.builder()
                    .app(rpcContext.param("app.id"))
                    .env(rpcContext.param("app.env"))
                    .namespace(rpcContext.param("app.namespace"))
                    .name(serviceName)
                    .build();
            List<InstanceMeta> providers = registryCenter.fetchAll(serviceMeta);
            if (providers == null || providers.isEmpty()) {
                log.debug("no providers");
                return null;
            }
            log.debug(" ===> map to providers: ");
            providers.forEach(System.out::println);
            registryCenter.subscribe(serviceMeta, event -> {
                    providers.clear();
                    providers.addAll(event.getData());
             });
           return Proxy.newProxyInstance(service.getClassLoader(),
           new Class[]{service}, new SoInvocationHandler(service, rpcContext, providers));
    }
}
