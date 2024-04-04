package com.so.sorpc.demo.provider;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.so.sorpc.core.annotation.SoRpcProvider;
import com.so.sorpc.core.api.RpcContext;
import com.so.sorpc.demo.api.User;
import com.so.sorpc.demo.api.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
@Component
@Slf4j
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
    public User[] findUsers(User[] users) {
        return users;
    }

    @Override
    public Map<String, User> getByUserMap(Map<String, User> userMap) {
        return userMap;
    }

    @Override
    public List<User> getByMap(Map<String, Integer> map) {
        return List.of(new User(1, "sososss"));
    }

    @Override
    public User ex(boolean flag) {
        if(flag) throw new RuntimeException("just throw an exception");
        return new User(100, "SO100");
    }

    String timeoutPorts = "8099,8094";

    @Override
    public User timeout(int timeout) {
        String port = environment.getProperty("server.port");
        if(Arrays.asList(timeoutPorts.split(",")).contains(port)) {
            log.debug("receive timeout");
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return new User(1001, "SO1001-" + port);
    }

    @Override
    public void setTimeoutPorts(String timeoutPorts) {
        log.debug("these port is set timeout:" + timeoutPorts);
        this.timeoutPorts = timeoutPorts;
    }

    @Override
    public String echoParameter(String key) {
        System.out.println(" ====>> RpcContext.ContextParameters: ");
        RpcContext.contextParameters.get().forEach((k,v)-> System.out.println(k+" -> " +v));
        return RpcContext.getContextParameters(key);
    }

}
