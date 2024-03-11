package com.so.sorpc.core.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-11
 */
@Configuration
public class ConsumerConfig {
    @Bean
    ConsumerBootStrap getConsumerBootStrap() {
        return new ConsumerBootStrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    ApplicationRunner consumerBootStrapRunner(@Autowired ConsumerBootStrap consumerBootStrap) {
        return x -> {
            System.out.println("consumerBootStrap start...");
            consumerBootStrap.start();
            System.out.println("consumerBootStrap end...");
        };
    }
}
