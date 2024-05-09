package com.so.sorpc.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.provider.ProviderBootStrap;
import com.so.sorpc.core.provider.ProviderInvoker;
import com.so.sorpc.core.registry.so.SoRegistryCenter;
import com.so.sorpc.core.registry.zk.ZkRegistryCenter;

import lombok.extern.slf4j.Slf4j;

/**
 * default config for provider
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
@Configuration
@Slf4j
//@Import({AppConfigProperties.class, ProviderConfigProperties.class, SpringBootTransport.class})
@Import({AppConfigProperties.class, ProviderConfigProperties.class})
public class ProviderConfig {

    @Value("${server.port:8081}")
    private String port;

    @Autowired
    AppConfigProperties appConfigProperties;

    @Autowired
    ProviderConfigProperties providerConfigProperties;

    @Bean
    ProviderBootStrap providerBootstrap() {
        return new ProviderBootStrap(port, appConfigProperties, providerConfigProperties);
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
//        return new ZkRegistryCenter();
        return new SoRegistryCenter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    ApolloChangedListener provider_apolloChangedListener() {
        return new ApolloChangedListener();
    }
}
