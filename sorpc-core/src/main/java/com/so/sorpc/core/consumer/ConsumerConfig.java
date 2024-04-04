package com.so.sorpc.core.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.so.sorpc.core.api.Filter;
import com.so.sorpc.core.api.LoadBalancer;
import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.api.Router;
import com.so.sorpc.core.cluster.GrayRouter;
import com.so.sorpc.core.cluster.RoundRobinLoadBalancer;
import com.so.sorpc.core.filter.ContextParameterFilter;
import com.so.sorpc.core.filter.MockFilter;
import com.so.sorpc.core.meta.InstanceMeta;
import com.so.sorpc.core.registry.zk.ZkRegistryCenter;

import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-11
 */
@Configuration
@Slf4j
public class ConsumerConfig {

    @Value("${sorpc.providers}")
    String servers;


    @Value("${app.grayRatio:0}")
    private int grayRatio;

    @Bean
    ConsumerBootStrap getConsumerBootStrap() {
        return new ConsumerBootStrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    ApplicationRunner consumerBootStrapRunner(@Autowired ConsumerBootStrap consumerBootStrap) {
        return x -> {
            log.info("consumerBootStrap start...");
            consumerBootStrap.start();
            log.info("consumerBootStrap end...");
        };
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        //return LoadBalancer.Default;
        return new RoundRobinLoadBalancer<>();
    }

    @Bean
    public Router<InstanceMeta> router() {
        return new GrayRouter(grayRatio);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumerRegistryCenter() {
        return new ZkRegistryCenter();
    }

//    @Bean
//    public Filter filter() {
//        return new CacheFilter();
//    }

//    @Bean
//    public Filter filter() {
//        return new MockFilter();
//    }

    @Bean
    public Filter filter() {
        return new ContextParameterFilter();
    }


}
