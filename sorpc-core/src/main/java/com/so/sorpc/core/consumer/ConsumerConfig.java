package com.so.sorpc.core.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.so.sorpc.core.api.LoadBalancer;
import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.api.Router;
import com.so.sorpc.core.cluster.RoundRobinLoadBalancer;
import com.so.sorpc.core.meta.InstanceMeta;
import com.so.sorpc.core.registry.zk.ZkRegistryCenter;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-11
 */
@Configuration
public class ConsumerConfig {

    @Value("${sorpc.providers}")
    String servers;

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

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        //return LoadBalancer.Default;
        return new RoundRobinLoadBalancer<>();
    }

    @Bean
    public Router<InstanceMeta> router() {
        return Router.Default;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumerRegistryCenter() {
        return new ZkRegistryCenter();
       // return new RegistryCenter.StaticRegistryCenter(List.of(servers.split(",")));
    }
}
