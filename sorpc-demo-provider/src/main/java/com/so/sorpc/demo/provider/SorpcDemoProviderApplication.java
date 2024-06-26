package com.so.sorpc.demo.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.so.sorpc.core.annotation.SoRpcScan;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;
import com.so.sorpc.core.config.ApolloChangedListener;
import com.so.sorpc.core.config.ProviderConfig;
import com.so.sorpc.core.config.ProviderConfigProperties;
import com.so.sorpc.core.exception.RpcException;
import com.so.sorpc.core.transport.SpringBootTransport;
import com.so.sorpc.demo.api.UserService;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Import({ProviderConfig.class})
@RestController
@Slf4j
@SoRpcScan(basePackage = {"com.so.sorpc"})
public class SorpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SorpcDemoProviderApplication.class, args);
    }

    //is it usefull?
    @Bean
    ApolloChangedListener apolloChangedListener() {
        return new ApolloChangedListener();
    }

    @Autowired
    SpringBootTransport transport;

    @Autowired
    UserService userService;

    @Autowired
    ProviderConfigProperties providerProperties;

    @RequestMapping("/metas")
    public String meta() {
        //observe metas object hashcode
        System.out.println(System.identityHashCode(providerProperties.getMetas()));
        return providerProperties.getMetas().toString();
    }

    @RequestMapping("/ports")
    public RpcResponse<String> ports(@RequestParam("ports") String ports) {
        userService.setTimeoutPorts(ports);
        RpcResponse<String> response = new RpcResponse<>();
        response.setStatus(true);
        response.setData("OK:" + ports);
        return response;
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
           log.info("case 1: 测试UserService.findById(Integer)==[基本测试：1个参数 Integer]==");
           rpcRequest.setService("com.so.sorpc.demo.api.UserService");
           rpcRequest.setMethodSign("findById@1_java.lang.Integer");
           rpcRequest.setArgs(new Object[]{100});

           RpcResponse rpcResponse = transport.invoke(rpcRequest);

           log.info("return: " + rpcResponse.getData());

           //com.so.sorpc.demo.api.UserService
           log.info("case 2: 测试UserService.findById(int)==[基本测试：1个参数 int]==");
           rpcRequest.setService("com.so.sorpc.demo.api.UserService");
           rpcRequest.setMethodSign("findById@1_int");
           rpcRequest.setArgs(new Object[]{100});

           RpcResponse rpcResponse01 = transport.invoke(rpcRequest);

           log.info("return: " + rpcResponse01.getData());

           //com.so.sorpc.demo.api.UserService
           log.info("case 3: 测试UserService.findById(String)==[基本测试：1个参数 String]==");
           rpcRequest.setService("com.so.sorpc.demo.api.UserService");
           rpcRequest.setMethodSign("findById@1_java.lang.String");
           rpcRequest.setArgs(new Object[]{"hello"});

           RpcResponse rpcResponse1 = transport.invoke(rpcRequest);

           log.info("return: " + rpcResponse1.getData());

           //com.so.sorpc.demo.api.UserService
           log.info("case 4: 测试UserService.findById(long)==[基本测试：1个参数 long]==");
           rpcRequest.setService("com.so.sorpc.demo.api.UserService");
           rpcRequest.setMethodSign("findById@1_long");
           rpcRequest.setArgs(new Object[]{1L});

           RpcResponse rpcResponse2 = transport.invoke(rpcRequest);
           log.info("return: " + rpcResponse2.getData());

           //com.so.sorpc.demo.api.UserService
           log.info("case 5: 测试UserService.findById(Double)==[基本测试：1个参数 Double]==");
           rpcRequest.setService("com.so.sorpc.demo.api.UserService");
           rpcRequest.setMethodSign("findById@1_java.lang.Double");
           rpcRequest.setArgs(new Object[]{1D});
           RpcResponse rpcResponse3 = transport.invoke(rpcRequest);
           log.info("return: " + rpcResponse3.getData());

//           log.info("case 6: 测试流量并发控制==");
//           for (int i = 0; i < 100; i++) {
//               try {
//                   Thread.sleep(1000);
//                   RpcResponse<Object> r = transport.invoke(rpcRequest);
//                   System.out.println(i + " ***>>> " +r.getData());
//               } catch (RpcException e) {
//                   // ignore
//                   log.info(i + " ***>>> " +e.getMessage() + " -> " + e.getErrorcode());
//               } catch (InterruptedException e) {
//                   throw new RuntimeException(e);
//               }
//           }
//           log.info("return: " + rpcResponse3.getData());
       };
    }

}
