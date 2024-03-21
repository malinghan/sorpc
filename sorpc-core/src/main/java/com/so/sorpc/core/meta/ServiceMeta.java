package com.so.sorpc.core.meta;

import lombok.Builder;
import lombok.Data;

/**
 * 描述服务元数据.
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-21
 */
@Data
@Builder
public class ServiceMeta {
    private String app; //组名
    private String namespace; //域名
    private String env;  //环境
    private String name; //服务名称
    private String version; //版本号

    public String toPath() {
        return String.format("%s_%s_%s_%s", app, namespace, env, name);
    }
}
