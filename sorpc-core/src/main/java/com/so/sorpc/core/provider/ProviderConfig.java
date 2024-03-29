package com.so.sorpc.core.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.registry.zk.ZkRegistryCenter;

import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
@Configuration
@Slf4j
public class ProviderConfig {

    @Bean
    ProviderBootStrap providerBootstrap() {
        return new ProviderBootStrap();
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
    public RegistryCenter providerRegistryCenter() {
        return new ZkRegistryCenter();
    }
}
