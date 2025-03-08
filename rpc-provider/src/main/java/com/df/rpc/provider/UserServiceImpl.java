package com.df.rpc.provider;


import com.df.rpc.common.model.User;
import com.df.rpc.common.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
public class UserServiceImpl implements UserService {

    public User getUser(User user) {
        List<String> collect = new ArrayList<String>().stream()
                .map((s) -> s + "1").collect(Collectors.toList());
        System.out.println("用户名：" + user.getName());
        return user;
    }

}
