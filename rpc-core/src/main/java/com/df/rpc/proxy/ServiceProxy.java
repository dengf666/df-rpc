package com.df.rpc.proxy;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.df.rpc.RpcApplication;
import com.df.rpc.config.RpcConfig;
import com.df.rpc.constant.RpcConstant;
import com.df.rpc.fault.retry.RetryStrategy;
import com.df.rpc.fault.retry.RetryStrategyFactory;
import com.df.rpc.loadbalancer.LoadBalancer;
import com.df.rpc.loadbalancer.LoadbalancerFactory;
import com.df.rpc.model.RpcRequest;
import com.df.rpc.model.RpcResponse;
import com.df.rpc.model.ServiceMetaInfo;
import com.df.rpc.protocol.*;
import com.df.rpc.registry.Registry;
import com.df.rpc.registry.RegistryFactory;
import com.df.rpc.serializer.Serializer;
import com.df.rpc.serializer.SerializerFactory;
import com.df.rpc.server.tcp.VertxTcpClient;
import com.df.rpc.spi.SpiLoader;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;



/**
 * 服务代理（JDK 动态代理）
 *
 */
public class ServiceProxy implements InvocationHandler {


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 从注册中心获取服务提供者请求地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getRegistry(rpcConfig.getRegistry().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            System.out.println("-------------------------" + serviceMetaInfo.getServiceKey());
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("暂无服务地址");
            }
            Map<String,Object> requestParam = new HashMap<>();
            requestParam.put("methodName",rpcRequest.getMethodName());
            LoadBalancer loadBalancer = LoadbalancerFactory.getInstance(rpcConfig.getLoadBalancer());
            ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParam, serviceMetaInfoList);
            // 发送 TCP 请求
           //进行重试
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            RpcResponse rpcResponse = retryStrategy.doRetry(() -> (RpcResponse) VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo));
            return rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

//    /**
//     * 调用http请求代理
//     *
//     * @return
//     * @throws Throwable
//     */
//    @Override
//    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        // 指定序列化器
//        Serializer serializer = (Serializer) SpiLoader.getInstance(Serializer.class, RpcApplication.getRpcConfig().getSerializer());
//        String serviceName = method.getDeclaringClass().getName();
//        // 构造请求
//        RpcRequest rpcRequest = RpcRequest.builder()
//                .serviceName(serviceName)
//                .methodName(method.getName())
//                .parameterTypes(method.getParameterTypes())
//                .args(args)
//                .build();
//        try {
//            // 序列化
//            byte[] bodyBytes = serializer.serialize(rpcRequest);
//            // 发送请求
//            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
//            RegistryConfig registryConfig = rpcConfig.getRegistry();
//            Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());
//            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
//            serviceMetaInfo.setServiceName(serviceName);
//            serviceMetaInfo.setServiceVersion("1.0");
//            List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
//            if(serviceMetaInfos == null || serviceMetaInfos.isEmpty()) {
//                throw new RuntimeException("未找到服务：" + serviceName);
//            }
//            ServiceMetaInfo serviceMetaInfo1 = serviceMetaInfos.get(0);
//            try (HttpResponse httpResponse = HttpRequest.post(serviceMetaInfo1.getServiceAddress())
//                    .body(bodyBytes)
//                    .execute()) {
//                byte[] result = httpResponse.bodyBytes();
//                // 反序列化
//                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
//                return rpcResponse.getData();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
}
