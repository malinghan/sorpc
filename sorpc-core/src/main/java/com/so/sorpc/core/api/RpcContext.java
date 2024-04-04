package com.so.sorpc.core.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<String, String> parameters = new HashMap<>();

    public static ThreadLocal<Map<String, String>> contextParameters = new ThreadLocal<>() {
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<>();
        }
    };

    public static void setContextParameters(String key, String value) {
        contextParameters.get().put(key, value);
    }

    public static String getContextParameters(String key) {
        return contextParameters.get().get(key);
    }

    public static void removeContextParameters(String key) {
         contextParameters.get().remove(key);
    }
}
