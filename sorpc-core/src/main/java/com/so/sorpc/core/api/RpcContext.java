package com.so.sorpc.core.api;

import java.util.List;

import com.so.sorpc.core.meta.InstanceMeta;

import lombok.Data;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-17
 */
@Data
public class RpcContext {
    List<Filter> filters;

    Router<InstanceMeta> router;

    LoadBalancer<InstanceMeta>  loadBalancer;
}
