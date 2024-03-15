package com.so.sorpc.demo.provider;

import org.springframework.stereotype.Component;

import com.so.sorpc.core.annotation.SoRpcProvider;
import com.so.sorpc.demo.api.Order;
import com.so.sorpc.demo.api.OrderService;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
@Component
@SoRpcProvider
public class OrderServiceImpl implements OrderService {
    @Override
    public Order findById(Long id) {
        return new Order(id, 0.01f);
    }
}
