package com.so.sorpc.core.api;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
@Data
public class RpcRequest {
    private String service; //接口: com.so.sorpc.demo.api.UserService
    private String methodSign;  //方法: getById
    private Object[] args;  //参数: 100
    //隐式传参调用所需
    private Map<String, String> params = new HashMap<>();
}
