package com.df.rpc.registry;

import com.df.rpc.spi.SpiLoader;

public class RegistryFactory {

    static {
        // 加载自定义的注册中心实现类
        SpiLoader.load(Registry.class);
    }
    /**
     * 默认注册中心
     */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

    /**
     * 获取实例
     * @param registryType
     * @return
     */
    public static Registry getRegistry(String registryType) {
        return (Registry) SpiLoader.getInstance(Registry.class, registryType);
    }
}
