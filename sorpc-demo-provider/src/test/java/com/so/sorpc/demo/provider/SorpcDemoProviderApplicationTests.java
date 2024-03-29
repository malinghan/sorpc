package com.so.sorpc.demo.provider;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.so.sorpc.core.test.TestZKServer;

@SpringBootTest
class SorpcDemoProviderApplicationTests {
    static TestZKServer zkServer = new TestZKServer();

    @BeforeAll
    static void init() {
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============     ZK2182    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        zkServer.start();
    }

    @Test
    void contextLoads() {
        System.out.println(" ===> KkrpcDemoProviderApplicationTests  .... ");
    }

    @AfterAll
    static void destory() {
        zkServer.stop();
    }

}
