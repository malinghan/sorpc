package com.someecho.sorpc.demo.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.so.sorpc.core.annotation.SoRpcConsumer;
import com.so.sorpc.core.consumer.ConsumerConfig;
import com.so.sorpc.demo.api.User;
import com.so.sorpc.demo.api.UserService;

@SpringBootApplication
@RestController
@Import({ConsumerConfig.class})
public class SorpcDemoConsumerApplication {

    @SoRpcConsumer
    private UserService userService;

    @Autowired
    ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(SorpcDemoConsumerApplication.class, args);
    }

    @Bean
    public ApplicationRunner consumerRunner()  {
        return x -> {
            User user = userService.findById(1);
            System.out.println(user.toString());

            User user1 = userService.findById("hello");
            System.out.println(user1.toString());

//            String userToString = userService.toString();
//            System.out.println(userToString);

            int[] ids = userService.getIds();
            for (int id : ids) {
                System.out.println(id);
            }

            long[] longIds = userService.getLongIds();
            for (long id : longIds) {
                System.out.println(id);
            }

//            int[] getIds(int[] ids)
            int[] ids1 = userService.getIds(new int[]{1,2,3});
            for (long id : ids1) {
                System.out.println(id);
            }

            //getByUser
            User user2 = new User(1, "so");
            int res = userService.getByUser(user2);
            System.out.println(res);

            //getByUsers
            List<User> user3 = new ArrayList<>();
            user3.add(new User(1, "so"));
            user3.add(new User(2, "so"));
            user3.add(new User(3, "so"));
            List<User> res1 = userService.getByUsers(user3);
            System.out.println("输出 getByUsers:");
            System.out.println(JSONObject.toJSON(res1));

            //getByUsers
            Map<String, List<User>> user4 = Map.of("sso",
                    List.of(new User(1, "so"),
                            new User(2, "so"),
                            new User(3, "so")));
            Map<String, List<User>> res2 = userService.getByUserMap(user4);
            System.out.println("输出 getByUserMap:");
            System.out.println(JSONObject.toJSON(res2));
        };
    }
}
