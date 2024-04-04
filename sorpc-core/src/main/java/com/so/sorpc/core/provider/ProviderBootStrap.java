package com.so.sorpc.core.provider;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

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
    private String port;
    private String app;
    private String namespace;
    private String env;
    private Map<String, String> metas;
    private InstanceMeta instance;
    public ProviderBootStrap(String port, String app, String namespace,
            String env, Map<String, String> metas) {
        this.port = port;
        this.app = app;
        this.namespace = namespace;
        this.env = env;
        this.metas = metas;
    }

    @PostConstruct
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(SoRpcProvider.class);
        rc = applicationContext.getBean(RegistryCenter.class);
        log.info("output all providers");
        providers.forEach((x,y) -> log.info("provider is putting : " + x));
        //把获取到的服务实现,放到skeleton中
        providers.values().forEach(this::genInterface);
    }

    @SneakyThrows
    public void start() {
        rc.start();
        String ip = InetAddress.getLocalHost().getHostAddress();
        log.info("metas:{}", metas);
        instance = InstanceMeta.http(ip, Integer.valueOf(port)).addParams(this.metas);
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
                    log.info("receive service:" + service.getCanonicalName());
                    Method[] methods = service.getMethods();
                    for (Method method : methods) {
                        if (MethodUtils.checkLocalMethod(method)) {
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
