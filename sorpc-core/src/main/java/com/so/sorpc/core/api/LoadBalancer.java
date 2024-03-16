package com.so.sorpc.core.api;

import java.util.List;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-17
 */
public interface LoadBalancer<T> {

    T choose(List<T> providers);

    LoadBalancer Default = p -> (p == null || p.isEmpty()) ? null : p.get(0);
}
