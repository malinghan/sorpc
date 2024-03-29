package com.so.sorpc.core.consumer;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.so.sorpc.core.annotation.SoRpcConsumer;
import com.so.sorpc.core.api.Filter;
import com.so.sorpc.core.api.LoadBalancer;
import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.api.Router;
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

    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String env;

    @Value("${app.retries}")
    private int retries;

    @Value("${app.timeout}")
    private int timeout;

    private Map<String, Object> stub = new HashMap<>();   //获取到客户端接口调用存根

    public void start() {
        Router<InstanceMeta> router = applicationContext.getBean(Router.class);
        LoadBalancer<InstanceMeta> loadBalancer = applicationContext.getBean(LoadBalancer.class);
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        List<Filter> filters = applicationContext.getBeansOfType(Filter.class).values().stream().toList();

        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.getParameters().put("app.retries", String.valueOf(retries)); //设置post invoke重试次数
        context.getParameters().put("app.timeout", String.valueOf(timeout)); //设置httpClient超时时间


        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);

            List<Field> fields = MethodUtils.getAnnotatedFields(bean.getClass(), SoRpcConsumer.class);

            for (Field field : fields) {
                log.debug(" ===> " + field.getName());
                try {
                    Class<?> service = field.getType();
                    String serviceName = service.getCanonicalName();
                    //这就是stub.put操作
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
                        consumer = creatConsumerFromRc(service, context, rc);
                        stub.put(serviceName, consumer);
                    }
                    field.setAccessible(true);
                    field.set(bean, consumer);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private Object creatConsumerFromRc(Class<?> service,RpcContext rpcContext, RegistryCenter registryCenter) {
             String serviceName = service.getCanonicalName();
            ServiceMeta serviceMeta = ServiceMeta.builder()
                    .app(app)
                    .env(env)
                    .namespace(namespace)
                    .name(serviceName)
                    .build();
            List<InstanceMeta> providers = registryCenter.fetchAll(serviceMeta);
            log.debug(" ===> map to providers: ");
            providers.forEach(System.out::println);
            //TODO 这里简化了subscribe处理, 将providers全部刷新了一下
            registryCenter.subscribe(serviceMeta, event -> {
                    providers.clear();
                    providers.addAll(event.getData());
             });
           return Proxy.newProxyInstance(service.getClassLoader(),
           new Class[]{service}, new SoInvocationHandler(service, rpcContext, providers));
    }
}
