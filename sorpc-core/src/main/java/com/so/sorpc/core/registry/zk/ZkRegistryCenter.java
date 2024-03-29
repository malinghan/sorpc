package com.so.sorpc.core.registry.zk;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import com.so.sorpc.core.api.RegistryCenter;
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

    @Value("${sorpc.zkServer}")
    String servers;

    @Value("${sorpc.zkRoot}")
    String root;

    CuratorCache cache;

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
        log.info(" ===> zk client stopped.");
//        cache.close();
        client.close();
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        //1. 获取服务路径
        String servicePath = "/" + service.toPath();
        //2. 注册实例
        try {
            if (client.checkExists().forPath(servicePath) == null) {
                //持久化服务路径
                //TODO 这里写死为service
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }
            // 3. 创建实例的临时性节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" ===> register to zk: " + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "providers".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
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
            log.info(" ===> unregister to zk: " + instancePath);
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @SneakyThrows
//    @Override
//    public void subscribe(String service, ChangedListener listener) {
//        final TreeCache cache =TreeCache.newBuilder(client, "/"+service)
//                .setCacheData(true) //Sets whether or not to cache byte data per node; default true.
//                .setMaxDepth(2) //获取depth
//                .build();
//        cache.getListenable().addListener((curator, event) -> {
//            // 有任何节点变动这里会执行
//            log.info("zk subscribe event: " + event);
//            List<String> nodes = fetchAll(service);
//            listener.fire(new Event(nodes));
//        });
//        cache.start();
//    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        CuratorCache cache = CuratorCache.builder(client, "/"+service.toPath()
                        ).build();
        CuratorCacheListener cacheListener = CuratorCacheListener.builder()
                        .forAll((type, oldNode, newNode) -> {
                    // 有任何节点变动这里会执行
                    log.info("zk subscribe event: " + type);
                    List<InstanceMeta> nodes = fetchAll(service);
                    listener.fire(new Event(nodes));
                }).build();
        cache.listenable().addListener(cacheListener);
        cache.start();
    }

    @SneakyThrows
    @Override
    public void unsubscribe(ServiceMeta service, ChangedListener listener) {
        //TODO
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        //1. 获取服务路径
        String servicePath = "/" + service.toPath();
        try {
           List<String> nodes = client.getChildren().forPath(servicePath);
            log.info(" ===> fetchAll from zk: " + servicePath);
           nodes.forEach(System.out::println);
           return mapInstance(nodes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<InstanceMeta> mapInstance(List<String> nodes)  {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        return nodes.stream().map(node -> {
                    String[] strs = node.split("_");
                    return InstanceMeta.http(strs[0], Integer.valueOf(strs[1]));
                }).collect(Collectors.toList());
    }
}
