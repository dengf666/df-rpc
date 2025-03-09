package com.rpc.rpcspringbootprovider.impl;

import com.df.rpc.common.model.User;
import com.df.rpc.common.service.UserService;
import com.rpc.rpcspringbootstart.annotation.RpcService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("收到消息" + user);
        return user;
    }

    @Override
    public short getNumber() {
        return UserService.super.getNumber();
    }
}
