package com.so.sorpc.core.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.registry.ZkRegistryCenter;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
@Configuration
public class ProviderConfig {

    @Bean
    ProviderBootStrap getProviderBootStrap() {
        return new ProviderBootStrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    ApplicationRunner providerBootStrapRunner(@Autowired ProviderBootStrap providerBootStrap) {
        return x -> {
            System.out.println("providerBootStrap start...");
            providerBootStrap.start();
            System.out.println("providerBootStrap end...");
        };
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter providerRegistryCenter() {
        return new ZkRegistryCenter();
    }
}
