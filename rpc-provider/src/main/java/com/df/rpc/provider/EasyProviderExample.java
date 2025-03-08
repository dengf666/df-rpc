package com.df.rpc.provider;


import com.df.rpc.RpcApplication;
import com.df.rpc.common.service.UserService;
import com.df.rpc.config.RegistryConfig;
import com.df.rpc.config.RpcConfig;
import com.df.rpc.model.ServiceMetaInfo;
import com.df.rpc.registry.LocalRegistry;
import com.df.rpc.registry.Registry;
import com.df.rpc.registry.RegistryFactory;
import com.df.rpc.server.HttpServer;
import com.df.rpc.server.VertxHttpServer;

import java.security.Provider;

/**
 * 简易服务提供者示例
 */
public class EasyProviderExample {

    public static void main(String[] args) {
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        RpcApplication.init();

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistry();
        Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());
        System.out.println("-----------------------" + registryConfig);
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(UserService.class.getName());
        serviceMetaInfo.setServiceVersion(rpcConfig.getVersion());
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(rpcConfig.getServerPort());
    }
}
