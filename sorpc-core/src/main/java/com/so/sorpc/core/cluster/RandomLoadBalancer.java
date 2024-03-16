package com.so.sorpc.core.cluster;

import java.util.List;
import java.util.Random;

import com.so.sorpc.core.api.LoadBalancer;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-17
 */
public class RandomLoadBalancer<T> implements LoadBalancer<T> {

    Random random = new Random();
    @Override
    public T choose(List<T> providers) {
        if(providers == null || providers.isEmpty()) return null;
        if(providers.size() == 1) return providers.get(0);
        return providers.get(random.nextInt(providers.size()));
    }
}
