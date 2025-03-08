package com.df.rpc.config;

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

    private String serializer = "json";

    private RegistryConfig registry = new RegistryConfig();

    /**
     * 是否启用 mock
     */
    private boolean mock = false;

}
