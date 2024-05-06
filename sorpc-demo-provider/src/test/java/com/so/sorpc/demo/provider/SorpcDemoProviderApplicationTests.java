package com.so.sorpc.demo.provider;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ctrip.framework.apollo.mockserver.ApolloTestingServer;
import com.so.sorpc.core.config.ProviderConfigProperties;
import com.so.sorpc.core.test.TestZKServer;

import lombok.SneakyThrows;

@SpringBootTest
class SorpcDemoProviderApplicationTests {
    static TestZKServer zkServer = new TestZKServer();

//    static ApolloTestingServer apollo = new ApolloTestingServer();

    @Autowired
    ProviderConfigProperties providerProperties;

    @BeforeAll
    @SneakyThrows
    static void init() {
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============     ZK2182    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        zkServer.start();
//        System.out.println(" ====================================== ");
//        System.out.println(" ====================================== ");
//        System.out.println(" ===========     mock apollo    ======= ");
//        System.out.println(" ====================================== ");
//        System.out.println(" ====================================== ");
//        apollo.start();
    }

    @Test
    void contextLoads() {
        System.out.println(" ===> KkrpcDemoProviderApplicationTests  .... ");
    }

    @AfterAll
    static void destory() {
        zkServer.stop();
//        System.out.println(" ===========     stop apollo mockserver   ======= ");
//        apollo.close();
//        System.out.println(" ===========     destroy in after all     ======= ");
    }

}
