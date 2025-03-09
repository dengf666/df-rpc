package com.df.rpc.config;

import com.df.rpc.fault.retry.RetryStrategyKeys;
import com.df.rpc.fault.tolerant.TolerantStrategyKeys;
import com.df.rpc.loadbalancer.LoadBalancerKeys;
import lombok.Data;

/**
 * RPC 框架配置
 */
@Data
public class RpcConfig {

    /**
     * 名称
     */
    private String name = "UserService";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";
    
    /**
     * 服务器端口号
     */
    private Integer serverPort = 8123;

    /**
     * 序列化方式
     */
    private String serializer = "json";

    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyKeys.NO;

    /**
     * 容错策略
     */
    private String tolerantStrategy = TolerantStrategyKeys.FAIL_FAST;
    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.RANDOM;

    private RegistryConfig registry = new RegistryConfig();

    /**
     * 是否启用 mock
     */
    private boolean mock = false;

}
