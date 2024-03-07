package com.example.sorpc.demo.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;
import com.so.sorpc.core.provider.ProviderBootStrap;
import com.so.sorpc.core.provider.ProviderConfig;

@SpringBootApplication
@Import({ProviderConfig.class})
@RestController
public class SorpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SorpcDemoProviderApplication.class, args);
    }

    @Autowired
    ProviderBootStrap providerBootStrap;


    @RequestMapping("/")
    RpcResponse invoke(@RequestBody  RpcRequest request) {
       return providerBootStrap.invoke(request);
    }

    /**
     * 在服务启动之后会自动运行该方法
     * @return
     */
    @Bean
    ApplicationRunner providerRun() {
       return x ->  {
           RpcRequest rpcRequest = new RpcRequest();
           //com.so.sorpc.demo.api.UserService
           rpcRequest.setService("com.so.sorpc.demo.api.UserService");
           rpcRequest.setMethod("findById");
           rpcRequest.setArgs(new Object[]{100});

           RpcResponse rpcResponse = invoke(rpcRequest);

           System.out.println("return: " + rpcResponse.getData());
       };
    }

}
