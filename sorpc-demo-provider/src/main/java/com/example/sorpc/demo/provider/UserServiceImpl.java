package com.example.sorpc.demo.provider;


import org.springframework.stereotype.Component;

import com.so.sorpc.core.annotation.SoRpcProvider;
import com.so.sorpc.demo.api.User;
import com.so.sorpc.demo.api.UserService;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
@Component
@SoRpcProvider
public class UserServiceImpl implements UserService {
    @Override
    public User findById(Integer id) {
        return new User(id, "so" + System.currentTimeMillis());
    }



}
