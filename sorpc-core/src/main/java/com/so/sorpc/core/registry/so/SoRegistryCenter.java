package com.so.sorpc.core.registry.so;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.consumer.http.HttpInvoker;
import com.so.sorpc.core.meta.InstanceMeta;
import com.so.sorpc.core.meta.ServiceMeta;
import com.so.sorpc.core.registry.ChangedListener;
import com.so.sorpc.core.registry.Event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-05-09
 */
@Slf4j
public class SoRegistryCenter  implements RegistryCenter {

    public static final String REG_PATH = "/reg";
    public static final String UNREG_PATH = "/unreg";
    public static final String FINDALL_PATH = "/findAll";
    public static final String VERSION_PATH = "/version";
    public static final String RENEWS_PATH = "/renews";

    @Value("${soregistry.servers}")
    String servers;

    // rc services version
    Map<String , Long> VERSIONS = new HashMap<String, Long>();
    //store (instance -> services) map for renew providers
    LinkedMultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();
    SoHealthChecker healthChecker = new SoHealthChecker();

    @Override
    public void start() {
        log.info(" ====> [so registry ]: start with server {}", servers);
        healthChecker.start();
        providerCheck();
    }

    @Override
    public void stop() {
        log.info(" ====> [so registry ]: stop with server {}", servers);
        healthChecker.stop();
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====> [so registry ]: register instance {} for {}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), regPath(service), InstanceMeta.class);
        log.info(" ====> [so registry ]: registered instance: {}", instance);
        //将其列入刷新维护列表
        RENEWS.add(instance, service);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====> [so registry ]: unregister instance {} for {}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), unregPath(service), InstanceMeta.class);
        log.info(" ====> [so registry ]: unregistered instance: {}", instance);
        //将其移除刷新维护列表??
        RENEWS.remove(instance, service);
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        healthChecker.consumerCheck( () -> {
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            Long newVersion = HttpInvoker.httpGet(versionPath(service), Long.class);
            log.info(" ====>>>> [so registry] : version = {}, newVersion = {}", version, newVersion);
            if(newVersion > version) {
                List<InstanceMeta> instances = fetchAll(service);
                listener.fire(new Event(instances));
                VERSIONS.put(service.toPath(), newVersion);
            }
        });
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ====> [so registry ]: find all for service:{} ", service);
        List<InstanceMeta> instances =  HttpInvoker.httpGet(findAllPath(service), new TypeReference<List<InstanceMeta>>() {});
        log.info(" ====> [so registry ]: find all instances: {}", instances);
        return instances;
    }


    public void providerCheck() {
        healthChecker.providerCheck(() -> {
            RENEWS.keySet().stream().forEach(
                    instance -> {
                        Long timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance),
                                renewsPath(RENEWS.get(instance)), Long.class);
                        log.info(" ====>>>> [so registry] : renew instance {} at {}", instance, timestamp);
                    }
            );
        });
    }

    private String path(String context, List<ServiceMeta> serviceList) {
        if (CollectionUtils.isEmpty(serviceList)) {
            return null;
        }
        String services = serviceList.stream().map(ServiceMeta::toPath).collect(Collectors.joining(","));
        log.info(" ====>>>> [so registry] : renew instance for {}", services);
        return servers + context + "?services=" + services;
    }

    private String path(String context, ServiceMeta service) {
        return servers + context + "?service=" + service.toPath();
    }

    private String regPath(ServiceMeta service) {
        return path(REG_PATH, service);
    }
    private String unregPath(ServiceMeta service) {
        return path(UNREG_PATH, service);
    }
    private String findAllPath(ServiceMeta service) {
        return path(FINDALL_PATH, service);
    }
    private String versionPath(ServiceMeta service) {
        return path(VERSION_PATH, service);
    }

    private String renewsPath(List<ServiceMeta> serviceList) {
        return path(RENEWS_PATH, serviceList);
    }

}
