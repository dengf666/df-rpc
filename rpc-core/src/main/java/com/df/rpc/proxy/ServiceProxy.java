package com.df.rpc.proxy;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.df.rpc.RpcApplication;
import com.df.rpc.config.RegistryConfig;
import com.df.rpc.config.RpcConfig;
import com.df.rpc.model.RpcRequest;
import com.df.rpc.model.RpcResponse;
import com.df.rpc.model.ServiceMetaInfo;
import com.df.rpc.registry.Registry;
import com.df.rpc.registry.RegistryFactory;
import com.df.rpc.serializer.Serializer;
import com.df.rpc.spi.SpiLoader;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 服务代理（JDK 动态代理）
 *
 */
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 指定序列化器
        Serializer serializer = (Serializer) SpiLoader.getInstance(Serializer.class, RpcApplication.getRpcConfig().getSerializer());
        String serviceName = method.getDeclaringClass().getName();
        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 发送请求
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            RegistryConfig registryConfig = rpcConfig.getRegistry();
            Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion("1.0");
            List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if(serviceMetaInfos == null || serviceMetaInfos.isEmpty()) {
                throw new RuntimeException("未找到服务：" + serviceName);
            }
            ServiceMetaInfo serviceMetaInfo1 = serviceMetaInfos.get(0);
            try (HttpResponse httpResponse = HttpRequest.post(serviceMetaInfo1.getServiceAddress())
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
