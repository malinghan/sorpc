package com.so.sorpc.core.test;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.utils.CloseableUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-29
 */
@Slf4j
public class TestZKServer {
    TestingCluster cluster;

    @SneakyThrows
    public void start() {
        InstanceSpec instanceSpec = new InstanceSpec(null, 2182,
                -1, -1, true,
                -1, -1, -1);
        cluster = new TestingCluster(instanceSpec);
        log.info("TestingZooKeeperServer starting ...");
        cluster.start();
        cluster.getServers().forEach(s -> log.info(s.getInstanceSpec().toString()));
        log.info("TestingZooKeeperServer started.");
    }

    @SneakyThrows
    public void stop() {
        log.info("TestingZooKeeperServer stopping ...");
        cluster.stop();
        CloseableUtils.closeQuietly(cluster);
        log.info("TestingZooKeeperServer stopped.");
    }
}
