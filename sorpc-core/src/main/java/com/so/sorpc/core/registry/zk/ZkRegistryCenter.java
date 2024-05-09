package com.so.sorpc.core.registry.zk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSON;
import com.so.sorpc.core.api.RegistryCenter;
import com.so.sorpc.core.exception.RpcException;
import com.so.sorpc.core.meta.InstanceMeta;
import com.so.sorpc.core.meta.ServiceMeta;
import com.so.sorpc.core.registry.ChangedListener;
import com.so.sorpc.core.registry.Event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-18
 */
@Slf4j
public class ZkRegistryCenter implements RegistryCenter {
    private CuratorFramework client = null;

    @Value("${sorpc.zk.server:localhost:2181}")
    String servers;

    @Value("${sorpc.zk.root:sorpc}")
    String root;

    private final List<CuratorCache> caches = new ArrayList<>();
//    private List<TreeCache> caches = new ArrayList<>();

    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3); //重试3次后，sleep 1s
        //通过zkClient来连接zk
        client = CuratorFrameworkFactory.builder()
                .connectString(servers)  //zk地址
                .namespace(root) //根路径
                .retryPolicy(retryPolicy) //重试策略
                .build();
        log.info(" ===> zk client starting.");
        client.start();
    }

    @Override
    public void stop() {
        log.info(" ===> zk tree cache closed.");
        caches.forEach(CuratorCache::close);
//        caches.forEach(TreeCache::close);
        log.info(" ===> zk client stopped.");
        client.close();
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        //1. 获取服务路径
        String servicePath = "/" + service.toPath();
        //2. 注册实例
        try {
            if (client.checkExists().forPath(servicePath) == null) {
                //3. 创建持久化服务节点，将服务版本等信息也注册上去
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, service.toMetas().getBytes());
            }
            // 3. 创建实例的临时性节点, 将实例等信息也注册上去
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" ===> register to zk: " + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, instance.toMetas().getBytes());
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        //1. 获取服务路径
        String servicePath = "/" + service.toPath();
        //2. 取消注册实例
        try {
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }
            // 3. 创建实例的临时性节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" ===> unregister to zk: {}", instancePath);
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        CuratorCache cache = CuratorCache.builder(client, "/"+service.toPath()
                        ).build();
        cache.listenable().addListener((type, oldNode, newNode) -> {
            // 有任何节点变动这里会执行
            log.info("zk subscribe event: {}", type);
            List<InstanceMeta> nodes = fetchAll(service);
            listener.fire(new Event(nodes));
        });
        cache.start();
        caches.add(cache);
    }

//    @SneakyThrows
//    @Override
//    public void subscribe(ServiceMeta service, ChangedListener listener) {
//        final TreeCache cache = TreeCache.newBuilder(client, "/"+service.toPath())
//                .setCacheData(true).setMaxDepth(2).build();
//        cache.getListenable().addListener(
//                (curator, event) -> {
//                    // 有任何节点变动这里会执行
//                    log.info("zk subscribe event: " + event);
//                    List<InstanceMeta> nodes = fetchAll(service);
//                    listener.fire(new Event(nodes));
//                }
//        );
//        cache.start();
//        caches.add(cache);
//    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        //1. 获取服务路径
        String servicePath = "/" + service.toPath();
        try {
           List<String> nodes = client.getChildren().forPath(servicePath);
            log.info(" ===> fetchAll from zk: {}", servicePath);
           nodes.forEach(log::info);
           return mapInstances(servicePath, nodes);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    private List<InstanceMeta> mapInstances(String servicePath, List<String> nodes)  {
        if (nodes == null || nodes.isEmpty()) {
            return new ArrayList<>();
        }
        return nodes.stream().map(node -> {
            String[] strs = node.split("_");
            //get instance meta
            InstanceMeta instance = InstanceMeta.http(strs[0], Integer.valueOf(strs[1]));
            String nodePath = servicePath + "/" + node;
            byte[] bytes;
            try {
                //get instance parameters
                bytes = client.getData().forPath(nodePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Map<String, Object> params = JSON.parseObject(new String(bytes));
            params.forEach((k,v) -> {
                log.debug("k:{}-> v:{}", k , v);
                instance.getParameters().put(k,v==null  ?   null    :   v.toString());
            });
            return instance;
        }).collect(Collectors.toList());
    }
}
