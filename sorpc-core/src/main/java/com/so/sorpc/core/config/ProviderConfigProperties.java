package com.so.sorpc.core.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * config properties for providers
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-04-05
 */
@Data
//@Configuration
@ConfigurationProperties(prefix = "sorpc.provider")
public class ProviderConfigProperties {
    Map<String, String> metas = new HashMap<>();

    //test config center
    String test;

    public void setTest(String test) {
        this.test = test;
    }
}
