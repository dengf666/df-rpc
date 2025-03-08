package com.df.rpc.provider;

import com.df.rpc.RpcApplication;
import com.df.rpc.common.service.UserService;
import com.df.rpc.config.RegistryConfig;
import com.df.rpc.config.RpcConfig;
import com.df.rpc.model.ServiceMetaInfo;
import com.df.rpc.registry.LocalRegistry;
import com.df.rpc.registry.Registry;
import com.df.rpc.registry.RegistryFactory;
import com.df.rpc.server.tcp.VertxTcpServer;

/**
 * 服务提供者示例
 */
public class ProviderExample {

    public static void main(String[] args) {
        // RPC 框架初始化
        RpcApplication.init();

        // 注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName,UserServiceImpl.class);

        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistry();
        Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 启动 TCP 服务
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(8123);
    }
}
