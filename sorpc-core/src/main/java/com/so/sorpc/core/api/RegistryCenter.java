package com.so.sorpc.core.api;

import java.util.List;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-17
 */
public interface RegistryCenter {

    void start();
    void stop();

    //provider
    void register(String service, String instance);
    void unregister(String service, String instance);

    List<String> fetchAll(String service);

    class StaticRegistryCenter implements RegistryCenter {

        List<String> providers;

        public StaticRegistryCenter(List<String> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void register(String service, String instance) {

        }

        @Override
        public void unregister(String service, String instance) {

        }

        @Override
        public List<String> fetchAll(String service) {
            return providers;
        }
    }
}
