package com.so.sorpc.core.registry;

import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import com.so.sorpc.core.api.RegistryCenter;

import lombok.SneakyThrows;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-18
 */
public class ZkRegistryCenter implements RegistryCenter {
    private CuratorFramework client = null;

    @Override
    public void start() {

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3); //重试3次后，sleep 1s
        //通过zkClient来连接zk
        client = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")  //zk地址 TODO 改成可配置
                .namespace("sorpc") //根路径
                .retryPolicy(retryPolicy) //重试策略
                .build();
        System.out.println(" ===> zk client starting.");
        client.start();
    }

    @Override
    public void stop() {
        System.out.println(" ===> zk client stopped.");
        client.close();
    }

    @Override
    public void register(String service, String instance) {
        //1. 获取服务路径
        String servicePath = "/" + service;
        //2. 注册实例
        try {
            if (client.checkExists().forPath(servicePath) == null) {
                //持久化服务路径
                //TODO 这里写死为service
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }
            // 3. 创建实例的临时性节点
            String instancePath = servicePath + "/" + instance;
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "providers".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unregister(String service, String instance) {
        //1. 获取服务路径
        String servicePath = "/" + service;
        //2. 取消注册实例
        try {
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }
            // 3. 创建实例的临时性节点
            String instancePath = servicePath + "/" + instance;
            System.out.println(" ===> unregister to zk: " + instancePath);
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
//            System.out.println("zk subscribe event: " + event);
//            List<String> nodes = fetchAll(service);
//            listener.fire(new Event(nodes));
//        });
//        cache.start();
//    }

    @SneakyThrows
    @Override
    public void subscribe(String service, ChangedListener listener) {
        final CuratorCache cache = CuratorCache.builder(client, "/"+service
                        ).build();
        CuratorCacheListener cacheListener = CuratorCacheListener.builder()
                        .forAll((type, oldNode, newNode) -> {
                    // 有任何节点变动这里会执行
                    System.out.println("zk subscribe event: " + type);
                    List<String> nodes = fetchAll(service);
                    listener.fire(new Event(nodes));
                }).build();
        cache.listenable().addListener(cacheListener);
        cache.start();
    }

    @Override
    public List<String> fetchAll(String service) {
        //1. 获取服务路径
        String servicePath = "/" + service;
        try {
           List<String> nodes = client.getChildren().forPath(servicePath);
            System.out.println(" ===> fetchAll from zk: " + servicePath);
           nodes.forEach(System.out::println);
           return nodes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
