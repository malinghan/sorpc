package com.so.sorpc.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.fastjson.JSON;
import com.so.sorpc.core.api.RpcResponse;

@SpringBootTest
class SorpcCoreApplicationTests {

    @Test
    void contextLoads() {
        String respJson = "{\"status\":true,\"data\":{\"id\":1,\"name\":\"so1710162994361\"},\"ex\":null}";
        RpcResponse rpcResponse = JSON.parseObject(respJson, RpcResponse.class);
    }

}
