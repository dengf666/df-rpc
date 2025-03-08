package com.df.rpc.registry;
import com.df.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册中心服务本地缓存
 */
public class RegistryServiceCache {

    /**
     * 服务缓存
     */
    Map<String,List<ServiceMetaInfo>> serviceCache = new ConcurrentHashMap<>();

    /**
     * 写缓存
     *
     * @param
     * @return
     */
    void writeCache(String serviceName ,List<ServiceMetaInfo> serviceMetaInfos) {
        this.serviceCache.put(serviceName,serviceMetaInfos);
    }

    /**
     * 读缓存
     *
     * @return
     */
    List<ServiceMetaInfo> readCache(String serviceName) {
        if (serviceCache!= null && this.serviceCache.containsKey(serviceName)){
            return this.serviceCache.get(serviceName);
        }
        return null;
    }

    /**
     * 清空缓存
     */
    void clearCache(String serviceName) {
        this.serviceCache.remove(serviceName);
    }
}
