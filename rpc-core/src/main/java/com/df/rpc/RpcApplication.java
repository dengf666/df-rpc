package com.df.rpc;

import com.df.rpc.config.RpcConfig;
import com.df.rpc.constant.RpcConstant;
import com.df.rpc.registry.Registry;
import com.df.rpc.registry.RegistryFactory;
import com.df.rpc.utils.ConfigUtils;

public class RpcApplication {

    private static volatile RpcConfig rpcConfig;

    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;

        //注册中心初始化
        Registry registry = RegistryFactory.getRegistry(rpcConfig.getRegistry().getRegistry());
        registry.init(rpcConfig.getRegistry());

        //创建shutdown钩子 JVM退出时执行
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }

    public static void init(){
        RpcConfig newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);

        if(newRpcConfig != null) {
            init(newRpcConfig);
        }
    }

    public static RpcConfig getRpcConfig() {
        if(rpcConfig == null) {
            synchronized (RpcApplication.class){
                if(rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
