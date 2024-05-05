package com.so.sorpc.demo.consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.so.sorpc.core.annotation.SoRpcConsumer;
import com.so.sorpc.core.annotation.SoRpcScan;
import com.so.sorpc.core.api.Router;
import com.so.sorpc.core.api.RpcContext;
import com.so.sorpc.core.cluster.GrayRouter;
import com.so.sorpc.core.config.ConsumerConfig;
import com.so.sorpc.demo.api.User;
import com.so.sorpc.demo.api.UserService;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RestController
@Import({ConsumerConfig.class})
@Slf4j
@SoRpcScan(basePackage = {"com.so.sorpc"})
public class SorpcDemoConsumerApplication { 

    @SoRpcConsumer
    private UserService userService;

    @Autowired
    ApplicationContext context;

//    @RequestMapping("/")
//    public User findBy(Integer id) {
//        return userService.findById(id);
//    }

    @RequestMapping("/api/")
    public User findBy(@RequestParam("id") Integer id) {
        return userService.findById(id);
    }

    @RequestMapping("/timeout")
    public User timeout(@RequestParam("time") int time) {
        return userService.timeout(time);
    }

    @Autowired
    Router router;

    @RequestMapping("/grey")
    public String grey(@RequestParam("ratio") int ratio) {
        ((GrayRouter)router).setGrayRatio(ratio);
        return "new grayRatio is " + ratio;
    }



    public static void main(String[] args) {
        SpringApplication.run(SorpcDemoConsumerApplication.class, args);
    }

    @Bean
    public ApplicationRunner consumerRunner()  {
        return x -> {
            testAll();
        };
    }

    private void testAll() {
        log.info(">>>>>>> test case1=====[test int cast case]=======");
        User user = userService.findById(1);
        log.info(user.toString());

        log.info(">>>>>>> test case2=====[test String cast case]=======");
        User user1 = userService.findById("hello");
        log.info(user1.toString());

        log.info(">>>>>>> test case3=====[test int[] return cast case]=======");
        int[] ids = userService.getIds();
        for (long id : ids) {
            log.info(id + "");
        }

        log.info(">>>>>>> test case4=====[test long[] return cast case]=======");
        long[] longIds = userService.getLongIds();
        for (long id : longIds) {
            log.info(id + "");
        }
        log.info(">>>>>>> test case5=====[test  int[] request and return cast case]=======");
        int[] ids1 = userService.getIds(new int[]{1,2,3});
        for (long id : ids1) {
            log.info(id + "");
        }

        log.info(">>>>>>> test case6=====[test  Object request  cast case]=======");
        //getByUser
        User user2 = new User(1, "so");
        int res = userService.getByUser(user2);
        log.info(res + "");

        log.info(">>>>>>> test case7=====[test  ArrayList request and return  cast case]=======");
        //getByUsers
        List<User> user3 = new ArrayList<>();
        user3.add(new User(1, "so"));
        user3.add(new User(2, "so"));
        user3.add(new User(3, "so"));
        List<User> res1 = userService.getByUsers(user3);
        log.info("输出 getByUsers:");
        log.info(JSONObject.toJSON(res1) + "");

        //getByUsers
        log.info(">>>>>>> test case8=====[test  Map request and return  cast case]=======");
        Map<String, User> user4 = Map.of("sso", new User(1, "so"));
        Map<String, User> res2 = userService.getByUserMap(user4);
        log.info("输出 getByUserMap:");
        log.info(JSONObject.toJSON(res2) + "");

        log.info(">>>>>>> test case9=====[test service toString() method case]=======");
        //getByUser
        String str = userService.toString();
        log.info(str);

        log.info("Case 10. >>===[测试参数和返回值都是User[]类型]===");
        User[] users = new User[]{
                new User(100, "ss100"),
                new User(101, "ss101")};
        Arrays.stream(userService.findUsers(users)).forEach(System.out::println);

        log.info("Case 11. >>===[测试参数为boolean，返回值都是User类型]===");
        User user100 = userService.ex(false);
        log.info(user100.toString());

        log.info("Case 12. >>===[测试服务端抛出一个RuntimeException异常]===");
        try {
            User userEx = userService.ex(true);
            log.info(userEx.toString());
        } catch (RuntimeException e) {
            log.info(" ===> exception: " + e.getMessage());
        }

        log.info("Case 13. >>===[测试服务端抛出一个超时重试后成功的场景]===");
        // 超时设置的【漏斗原则】
        // A 2000 -> B 1500 -> C 1200 -> D 1000
        long start = System.currentTimeMillis();
        userService.timeout(1100);
        log.info("userService.find take "
                + (System.currentTimeMillis()-start) + " ms");


        System.out.println("Case 14. >>===[测试通过Context跨消费者和提供者进行传参]===");
        String Key_Version = "rpc.version";
        String Key_Message = "rpc.message";
        RpcContext.setContextParameters(Key_Version, "v8");
        RpcContext.setContextParameters(Key_Message, "this is a test message");
        String version = userService.echoParameter(Key_Version);
        RpcContext.setContextParameters(Key_Version, "v8");
        RpcContext.setContextParameters(Key_Message, "this is a test message");
        String message = userService.echoParameter(Key_Message);
        System.out.println(" ===> echo parameter from c->p->c: " + Key_Version + " -> " + version);
        System.out.println(" ===> echo parameter from c->p->c: " + Key_Message + " -> " + message);
        RpcContext.contextParameters.get().clear();
    }
}
