package com.so.sorpc.demo.api;

import java.util.List;
import java.util.Map;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
public interface UserService {

    User findById(Integer id);

    User findById(int id);

    User findById(long id);

    User findById(String idString);

    User findById(Double d);

    int[] getIds();

    long[] getLongIds();

    int[] getIds(int[] ids);

    int getByUser(User user);

    List<User> getByUsers(List<User> users);

    User[] findUsers(User[] users);

    Map<String, User> getByUserMap(Map<String, User> users);

    List<User> getByMap(Map<String, Integer> map);

    User ex(boolean flag);

    User timeout(int timeout);

    void setTimeoutPorts(String timeoutPorts);

}
