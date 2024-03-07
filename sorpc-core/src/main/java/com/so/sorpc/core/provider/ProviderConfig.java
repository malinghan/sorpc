package com.so.sorpc.core.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
