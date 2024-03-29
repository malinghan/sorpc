package com.someecho.sorpc.demo.provider;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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

    @Autowired
    Environment environment;

    @Override
    public User findById(Integer id) {
        return new User(id, "so-"
                + environment.getProperty("server.port")
                + "-"+
                + System.currentTimeMillis());
    }

    @Override
    public User findById(int id) {
        return new User(id, "so" + System.currentTimeMillis());
    }

    @Override
    public User findById(long id) {
        return  new User(10, "so" + System.currentTimeMillis());
    }

    @Override
    public User findById(String idString) {
        return new User(10, "so-" + idString);
    }

    @Override
    public User findById(Double d) {
        return new User(10, "so-" + d);
    }

    @Override
    public int[] getIds() {
        return new int[0];
    }

    @Override
    public long[] getLongIds() {
        return new long[0];
    }

    @Override
    public int[] getIds(int[] ids) {
        return new int[]{4,5,6};
    }

    @Override
    public int getByUser(User user) {
        return user.getId();
    }

    @Override
    public List<User> getByUsers(List<User> users) {
        return users;
    }

    @Override
    public Map<String, List<User>> getByUserMap(Map<String, List<User>> users) {
        return users;
    }

    @Override
    public List<User> getByMap(Map<String, Integer> map) {
        return List.of(new User(1, "sososss"));
    }
}
