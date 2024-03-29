package com.so.sorpc.core.api;

import java.util.List;

import com.so.sorpc.core.meta.InstanceMeta;
import com.so.sorpc.core.meta.ServiceMeta;
import com.so.sorpc.core.registry.ChangedListener;

/**
 * 注册中心
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-17
 */
public interface RegistryCenter {

    void start();
    void stop();

    //provider
    void register(ServiceMeta service, InstanceMeta instance);

    void unregister(ServiceMeta service, InstanceMeta instance);

    void subscribe(ServiceMeta service, ChangedListener listener);

    void unsubscribe(ServiceMeta service, ChangedListener listener);

    List<InstanceMeta> fetchAll(ServiceMeta service);

    class StaticRegistryCenter implements RegistryCenter {

        List<InstanceMeta> providers;

        public StaticRegistryCenter(List<InstanceMeta> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void register(ServiceMeta service, InstanceMeta instance) {

        }

        @Override
        public void unregister(ServiceMeta service, InstanceMeta instance) {

        }

        @Override
        public void subscribe(ServiceMeta service, ChangedListener listener) {

        }

        @Override
        public void unsubscribe(ServiceMeta service, ChangedListener listener) {

        }

        @Override
        public List<InstanceMeta> fetchAll(ServiceMeta service) {
            return providers;
        }
    }
}
