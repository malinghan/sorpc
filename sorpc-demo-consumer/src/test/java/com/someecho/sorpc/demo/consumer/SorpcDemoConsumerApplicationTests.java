package com.someecho.sorpc.demo.consumer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.so.sorpc.core.test.TestZKServer;
import com.someecho.sorpc.demo.provider.SorpcDemoProviderApplication;

@SpringBootTest(classes = {SorpcDemoConsumerApplication.class})
class SorpcDemoConsumerApplicationTests {
    static ApplicationContext context1;
    static ApplicationContext context2;

    static TestZKServer zkServer = new TestZKServer();

    @BeforeAll
    static void init() {
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============     ZK2182    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        zkServer.start();
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============      P8094    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        context1 = SpringApplication.run(SorpcDemoProviderApplication.class,
                "--server.port=8094", "--sorpc.zkServer=localhost:2182",
                "--logging.level.com.so.sorpc=info");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============      P8095    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        context2 = SpringApplication.run(SorpcDemoProviderApplication.class,
                "--server.port=8095", "--sorpc.zkServer=localhost:2182",
                "--logging.level.com.so.sorpc=info");
    }

    @Test
    void contextLoads() {
        System.out.println(" ===> aaaa  .... ");
    }

    @AfterAll
    static void destroy() {
        SpringApplication.exit(context1, () -> 1);
        SpringApplication.exit(context2, () -> 1);
        zkServer.stop();
    }

}
