package com.so.sorpc.core.api;

import java.util.List;

import lombok.Data;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-17
 */
@Data
public class RpcContext {
    List<Filter> filters; // todo

    Router router;

    LoadBalancer loadBalancer;
}
