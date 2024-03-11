package com.someecho.sorpc.demo.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;

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

            String userToString = userService.toString();
            System.out.println(userToString);
        };
    }
}
