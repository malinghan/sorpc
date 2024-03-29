package com.so.sorpc.demo.provider;

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
import com.so.sorpc.core.provider.ProviderInvoker;
import com.so.sorpc.core.provider.ProviderConfig;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Import({ProviderConfig.class})
@RestController
@Slf4j
public class SorpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SorpcDemoProviderApplication.class, args);
    }

    @Autowired
    ProviderInvoker providerInvoker;


    @RequestMapping("/")
    RpcResponse<Object> invoke(@RequestBody  RpcRequest request) {
       return providerInvoker.invoke(request);
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
//           log.info("测试UserService.findById(Integer)");
//           rpcRequest.setService("com.so.sorpc.demo.api.UserService");
//           rpcRequest.setMethodSign("findById@1_java.lang.Integer");
//           rpcRequest.setArgs(new Object[]{100});
//
//           RpcResponse rpcResponse = invoke(rpcRequest);
//
//           log.info("return: " + rpcResponse.getData());
//
//           //com.so.sorpc.demo.api.UserService
//           log.info("测试UserService.findById(int)");
//           rpcRequest.setService("com.so.sorpc.demo.api.UserService");
//           rpcRequest.setMethodSign("findById@1_int");
//           rpcRequest.setArgs(new Object[]{100});
//
//           RpcResponse rpcResponse01 = invoke(rpcRequest);
//
//           log.info("return: " + rpcResponse01.getData());
//
//           //com.so.sorpc.demo.api.UserService
//           log.info("测试UserService.findById(String)");
//           rpcRequest.setService("com.so.sorpc.demo.api.UserService");
//           rpcRequest.setMethodSign("findById@1_java.lang.String");
//           rpcRequest.setArgs(new Object[]{"hello"});
//
//           RpcResponse rpcResponse1 = invoke(rpcRequest);
//
//           log.info("return: " + rpcResponse1.getData());
//
//           //com.so.sorpc.demo.api.UserService
//           log.info("测试UserService.findById(long)");
//           rpcRequest.setService("com.so.sorpc.demo.api.UserService");
//           rpcRequest.setMethodSign("findById@1_long");
//           rpcRequest.setArgs(new Object[]{1L});
//
//           RpcResponse rpcResponse2 = invoke(rpcRequest);
//           log.info("return: " + rpcResponse2.getData());
//
//           //com.so.sorpc.demo.api.UserService
//           log.info("测试UserService.findById(Double)");
//           rpcRequest.setService("com.so.sorpc.demo.api.UserService");
//           rpcRequest.setMethodSign("findById@1_java.lang.Double");
//           rpcRequest.setArgs(new Object[]{1D});
//
//           RpcResponse rpcResponse3 = invoke(rpcRequest);
//           log.info("return: " + rpcResponse3.getData());

           //返回值测试
           log.info("测试UserService.getLongIds(Double)");
           rpcRequest.setService("com.so.sorpc.demo.api.UserService");
           rpcRequest.setMethodSign("getLongIds@0");
           rpcRequest.setArgs(new Object[]{});

           RpcResponse<Object> rpcResponse4 = invoke(rpcRequest);
           log.info("return: " + rpcResponse4.toString());
       };
    }

}
