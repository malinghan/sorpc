package com.so.sorpc.core.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import com.so.sorpc.core.api.Filter;
import com.so.sorpc.core.api.LoadBalancer;
import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.api.Router;
import com.so.sorpc.core.api.RpcContext;
import com.so.sorpc.core.cluster.GrayRouter;
import com.so.sorpc.core.cluster.RoundRobinLoadBalancer;
import com.so.sorpc.core.consumer.ConsumerBootStrap;
import com.so.sorpc.core.filter.ContextParameterFilter;
import com.so.sorpc.core.meta.InstanceMeta;
import com.so.sorpc.core.registry.so.SoRegistryCenter;
import com.so.sorpc.core.registry.zk.ZkRegistryCenter;

import lombok.extern.slf4j.Slf4j;

/**
 * config for consumer
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-11
 */
@Configuration
@Slf4j
@Import({AppConfigProperties.class,ConsumerConfigProperties.class})
public class ConsumerConfig {

    @Autowired
    AppConfigProperties appConfigProperties;

    @Autowired
    ConsumerConfigProperties consumerConfigProperties;

    @Autowired
    ApplicationContext applicationContext;



    @Bean
    ConsumerBootStrap getConsumerBootStrap() {
        return new ConsumerBootStrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE + 1)
    ApplicationRunner consumerBootStrapRunner(@Autowired ConsumerBootStrap consumerBootStrap) {
        return x -> {
            log.info("consumerBootStrap start...");
            consumerBootStrap.start();
            log.info("consumerBootStrap end...");
        };
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        return new RoundRobinLoadBalancer<>();
    }

    @Bean
    public Router<InstanceMeta> router() {
        return new GrayRouter(consumerConfigProperties.getGrayRatio());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    ApolloChangedListener consumer_apolloChangedListener() {
        return new ApolloChangedListener();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter consumerRegistryCenter() {
//        return new ZkRegistryCenter();
        return new SoRegistryCenter();
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

    /**
     * autowired rpc context
     * @param router
     * @param loadBalancer
     * @param filters
     * @return
     */
    @Bean
    public RpcContext createContext(@Autowired Router router,
            @Autowired LoadBalancer loadBalancer,
            @Autowired List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.getParameters().put("app.id", appConfigProperties.getId());
        context.getParameters().put("app.namespace", appConfigProperties.getNamespace());
        context.getParameters().put("app.env", appConfigProperties.getEnv());
        context.getParameters().put("consumer.retries", String.valueOf(consumerConfigProperties.getRetries()));
        context.getParameters().put("consumer.timeout", String.valueOf(consumerConfigProperties.getTimeout()));
        context.getParameters().put("consumer.halfOpenInitialDelay", String.valueOf(consumerConfigProperties.getHalfOpenInitialDelay()));
        context.getParameters().put("consumer.faultLimit", String.valueOf(consumerConfigProperties.getFaultLimit()));
        context.getParameters().put("consumer.halfOpenDelay", String.valueOf(consumerConfigProperties.getHalfOpenDelay()));
        return context;
    }


}
