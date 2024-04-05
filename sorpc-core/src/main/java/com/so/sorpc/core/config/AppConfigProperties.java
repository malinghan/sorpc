package com.so.sorpc.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-04-05
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sorpc.app")
public class AppConfigProperties {
    // for app instance
    private String id = "soapp1";

    private String namespace = "public";

    private String env = "dev";
}
