package com.so.sorpc.core.consumer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.so.sorpc.core.api.Filter;
import com.so.sorpc.core.api.LoadBalancer;
import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.api.Router;
import com.so.sorpc.core.api.RpcContext;
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

    @Value("${app.id:soapp1}")
    private String app;

    @Value("${app.namespace:public}")
    private String namespace;

    @Value("${app.env:dev}")
    private String env;

    @Value("${app.retries:1}")
    private int retries;

    @Value("${app.timeout:1000}")
    private int timeout;

    @Value("${app.faultLimit:10}")
    private int faultLimit;

    @Value("${app.halfOpenInitialDelay:10000}")
    private int halfOpenInitialDelay;

    @Value("${app.halfOpenDelay:60000}")
    private int halfOpenDelay;

    @Autowired
    ApplicationContext applicationContext;



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
    @ConditionalOnMissingBean
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
        context.getParameters().put("app.id", app);
        context.getParameters().put("app.namespace", namespace);
        context.getParameters().put("app.env", env);
        context.getParameters().put("app.retries", String.valueOf(retries));
        context.getParameters().put("app.timeout", String.valueOf(timeout));
        context.getParameters().put("app.halfOpenInitialDelay", String.valueOf(halfOpenInitialDelay));
        context.getParameters().put("app.faultLimit", String.valueOf(faultLimit));
        context.getParameters().put("app.halfOpenDelay", String.valueOf(halfOpenDelay));
        return context;
    }


}
