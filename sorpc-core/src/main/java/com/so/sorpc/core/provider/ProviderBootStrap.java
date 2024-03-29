package com.so.sorpc.core.provider;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.so.sorpc.core.annotation.SoRpcProvider;
import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.meta.InstanceMeta;
import com.so.sorpc.core.meta.ProviderMeta;
import com.so.sorpc.core.meta.ServiceMeta;
import com.so.sorpc.core.utils.MethodUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
@Data
@Slf4j
public class ProviderBootStrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    RegistryCenter rc;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();   //获取到的服务接口存根

    private InstanceMeta instance;

    @Value("${server.port}")
    private int port;

    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String env;


    @PostConstruct
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(SoRpcProvider.class);
        rc = applicationContext.getBean(RegistryCenter.class);
        log.info("输出通过注解获取到的providers信息");
        providers.forEach((x,y) -> log.info("获取到的服务实现为: " + x));
        //把获取到的服务实现,放到skeleton中
        providers.values().forEach(this::genInterface);
    }

    @SneakyThrows
    public void start() {
        rc.start();
        String ip = InetAddress.getLocalHost().getHostAddress();
        instance = InstanceMeta.http(ip, port);
        skeleton.keySet().forEach(this::registerService);
    }

    public void registerService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(app)
                .env(env)
                .namespace(namespace)
                .name(service)
                .build();
        rc.register(serviceMeta, instance);
    }

    @PreDestroy
    public void stop() {
        skeleton.keySet().forEach(this::unregisterService);
        rc.stop();
    }

    public void unregisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(app)
                .env(env)
                .namespace(namespace)
                .name(service)
                .build();
        rc.unregister(serviceMeta, instance);
    }

    /**
     * 将接口信息存入skeleton
     * @param impl
     */
    void genInterface(Object impl) {
        //类型信息, 一个类有多个接口
        Arrays.stream(impl.getClass().getInterfaces()).forEach(
                service -> {
                    log.info("获取到的接口信息为:" + service.getCanonicalName());
                    Method[] methods = service.getMethods();
                    for (Method method : methods) {
                        if (MethodUtils.checkIfObjectMethod(method)) {
                            continue;
                        }
                        ProviderMeta providerMeta = createProviderMeta(impl, method);
                        log.info(" create a provider: " + providerMeta);
                        skeleton.add(service.getCanonicalName(), providerMeta);
                    }
                }
        );
    }

    private  ProviderMeta createProviderMeta(Object impl, Method method) {
        return ProviderMeta.builder()
                .method(method)
                .serviceImpl(impl)
                .methodSign(MethodUtils.methodSign(method))
                .build();
    }

}
