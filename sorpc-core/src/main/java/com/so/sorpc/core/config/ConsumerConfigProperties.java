package com.so.sorpc.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * config properties for consumers
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-04-05
 */
@Data
//@Configuration
@ConfigurationProperties(prefix = "sorpc.consumer")
public class ConsumerConfigProperties {

    // for ha and governance
    private int retries = 1;

    private int timeout = 1000;

    private int faultLimit = 10;

    private int halfOpenInitialDelay = 10_000;

    private int halfOpenDelay = 60_000;

    private int grayRatio = 0;
}
