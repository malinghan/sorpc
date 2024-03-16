package com.so.sorpc.core.cluster;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.so.sorpc.core.api.LoadBalancer;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-17
 */
public class RoundRobinLoadBalancer<T> implements LoadBalancer<T> {

    AtomicInteger index = new AtomicInteger(0);
    @Override
    public T choose(List<T> providers) {
        if(providers == null || providers.isEmpty()) return null;
        if(providers.size() == 1) return providers.get(0);
        return providers.get((index.getAndIncrement() & 0x7fffffff) % providers.size());
    }
}
