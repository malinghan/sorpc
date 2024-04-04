package com.so.sorpc.demo.consumer;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.so.sorpc.core.annotation.SoRpcConsumer;
import com.so.sorpc.core.test.TestZKServer;
import com.so.sorpc.demo.api.User;
import com.so.sorpc.demo.api.UserService;
import com.so.sorpc.demo.provider.SorpcDemoProviderApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {SorpcDemoConsumerApplication.class})
@Slf4j
class SorpcDemoConsumerApplicationTests {
    static ApplicationContext context1;
    static ApplicationContext context2;

    @SoRpcConsumer
    private UserService userService;

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
                "--logging.level.com.so.sorpc=info", "--app.metas={dc:'bj',gray:'false',unit:'B001'}");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============      P8095    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        context2 = SpringApplication.run(SorpcDemoProviderApplication.class,
                "--server.port=8095", "--sorpc.zkServer=localhost:2182",
                "--logging.level.com.so.sorpc=info", "--app.metas={dc:'bj',gray:'false',unit:'B001'}");
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

    private void testAll() {
        log.info(">>>>>>> test case1");
        User user = userService.findById(1);
        log.info(user.toString());

        User user1 = userService.findById("hello");
        log.info(user1.toString());

        //            String userToString = userService.toString();
        //            log.info(userToString);
        log.info(">>>>>>> test case2");
        int[] ids = userService.getIds();

        log.info(">>>>>>> test case3");
        long[] longIds = userService.getLongIds();
        for (long id : longIds) {
            log.info(id + "");
        }
        log.info(">>>>>>> test case4");
        //            int[] getIds(int[] ids)
        int[] ids1 = userService.getIds(new int[]{1,2,3});
        for (long id : ids1) {
            log.info(id + "");
        }

        log.info(">>>>>>> test case5");
        //getByUser
        User user2 = new User(1, "so");
        int res = userService.getByUser(user2);
        log.info(res + "");

        log.info(">>>>>>> test case6");
        //getByUsers
        List<User> user3 = new ArrayList<>();
        user3.add(new User(1, "so"));
        user3.add(new User(2, "so"));
        user3.add(new User(3, "so"));
        List<User> res1 = userService.getByUsers(user3);
        log.info("输出 getByUsers:");
        log.info(JSONObject.toJSON(res1) + "");
    }

}
