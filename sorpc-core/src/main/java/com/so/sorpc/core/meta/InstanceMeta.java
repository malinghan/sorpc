package com.so.sorpc.core.meta;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstanceMeta {
    private String scheme; // 协议类型
    private String host;
    private Integer port;
    private String context; //实例信息
    private Integer status; //实例状态
    private Map<String, String> parameters  = new HashMap<>();  // idc  A B C; //实例相关参数

    public InstanceMeta(String scheme, String host, Integer port, String context) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String toPath() {
        return String.format("%s_%d",host,port);
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s",scheme, host, port, context);
    }

    public String toMetas() {
        return JSON.toJSONString(this.parameters);
    }

    public InstanceMeta addParams(Map<String, String> params) {
        this.getParameters().putAll(params);
        return this;
    }

    public static InstanceMeta http(String host, Integer port) {
       return new InstanceMeta("http", host, port, "sorpc");
    }


}
