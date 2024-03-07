package com.so.sorpc.core.api;

import java.lang.reflect.Method;

import lombok.Data;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
@Data
public class RpcRequest {
    String service;
    String method;
    Object[] args;
}
