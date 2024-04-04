package com.so.sorpc.core.provider;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.registry.zk.ZkRegistryCenter;
import com.so.sorpc.core.transport.SpringBootTransport;

import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
@Configuration
@Slf4j
@Import({SpringBootTransport.class})
public class ProviderConfig {

    @Value("${server.port:8081}")
    private String port;

    @Value("${app.id:app1}")
    private String app;

    @Value("${app.namespace:public}")
    private String namespace;

    @Value("${app.env:dev}")
    private String env;

    @Value("#{${app.metas:{dc:'bj',gray:'false',unit:'B001'}}}")  //Spel
    Map<String, String> metas;

    @Bean
    ProviderBootStrap providerBootstrap() {
        return new ProviderBootStrap(port, app, namespace, env, metas);
    }

    @Bean
    public ProviderInvoker providerInvoker(@Autowired ProviderBootStrap providerBootStrap) {
        return new ProviderInvoker(providerBootStrap);
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    ApplicationRunner providerBootStrapRunner(@Autowired ProviderBootStrap providerBootStrap) {
        return x -> {
            log.info("providerBootStrap start...");
            providerBootStrap.start();
            log.info("providerBootStrap started...");
        };
    }

    @Bean //(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean //default zk
    public RegistryCenter providerRegistryCenter() {
        return new ZkRegistryCenter();
    }
}
