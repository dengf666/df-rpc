package com.df.rpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.df.rpc.config.RegistryConfig;
import com.df.rpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Etcd注册中心
 */
public class EtcdRegistry implements Registry {

    private  Client client;

    private KV kvClient;
    /**
     * 本地注册节点集合·
     */
    private final Set<String> localRegisterNodeSet = new HashSet<>();
    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    private RegistryServiceCache registryServiceCache = new RegistryServiceCache();


    private Set<String> WatchingKeySet = new HashSet<>();
    /**
     * 初始化
     * @param registryConfig
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        if(client == null){
            client = Client.builder().endpoints(registryConfig.getAddress()).connectTimeout(Duration.ofMillis(registryConfig.getTimeout())).build();
            kvClient = client.getKVClient();
        }
        heartbeat();
    }

    @Override
    public void watch(String serviceKey){
        Watch watchClient = client.getWatchClient();
        if(WatchingKeySet.contains(serviceKey)){ return;}
        WatchingKeySet.add(serviceKey);
        //开启前缀匹配
        WatchOption watchOption = WatchOption.builder().isPrefix(true).build();
        watchClient.watch(ByteSequence.from(ETCD_ROOT_PATH + serviceKey + "/", StandardCharsets.UTF_8),watchOption,response -> {
            List<WatchEvent> events = response.getEvents();
            for (WatchEvent event : events) {
                switch (event.getEventType()){
                    case PUT:
//                            ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(new String(event.getKeyValue().getValue().getBytes()),ServiceMetaInfo.class);
//                            registryServiceCache.writeCache(serviceMetaInfo);
                        registryServiceCache.clearCache(serviceKey);
                        break;
                    case DELETE:
                        System.out.println("------------------------删除了");
                        registryServiceCache.clearCache(serviceKey);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 注册服务
     * @param serviceMetaInfo
     * @throws Exception
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        Lease leaseClient = client.getLeaseClient();

        //设置租约,30秒服务过期时间
        long leaseId = leaseClient.grant(30).get().getID();
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();

        //设置键值对
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        //将服务放到本地服务节点集合
        localRegisterNodeSet.add(registerKey);
    }

    /**
     * 服务注销
     * @param serviceMetaInfo
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(),StandardCharsets.UTF_8));
        //从当地节点移除
        localRegisterNodeSet.remove(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey());
    }

    /*
 * 服务发现
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        String keyPrefix = ETCD_ROOT_PATH + serviceKey + "/";
        //查寻缓存
        if (registryServiceCache != null && registryServiceCache.readCache(serviceKey) != null) {
            return registryServiceCache.readCache(serviceKey);
        }
        GetOption option = GetOption.builder().isPrefix(true).build();
        try {
            GetResponse getResponse = kvClient.get(ByteSequence.from(keyPrefix, StandardCharsets.UTF_8), option).get();
            List<KeyValue> kvs = getResponse.getKvs();
            List<ServiceMetaInfo> serviceMEtaInfos = kvs.stream()
                    .map(kv -> {
                        return JSONUtil.toBean(new String(kv.getValue().getBytes()), ServiceMetaInfo.class);
                    }).collect(Collectors.toList());
            //将结果写入缓存
            registryServiceCache.writeCache(serviceKey, serviceMEtaInfos);
            //监听服务
            watch(serviceKey);
            return serviceMEtaInfos;
        }catch (Exception e) {
            throw new RuntimeException("服务发现失败");
        }
    }

    @Override
    public void destroy() {
        //从本地遍历出所有的节点，并删除
        for (String registerKey : localRegisterNodeSet) {
            try {
                kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(registerKey + "服务注销失败");
            }
            System.out.println("关闭etcd连接");
            if (kvClient != null) {
                kvClient.close();
            }
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public void heartbeat() {
        CronUtil.schedule("*/10 * * * * ?", new Task() {
            @Override
            public void execute() {
                for (String registerKey : localRegisterNodeSet) {
                    try {
                        GetResponse getResponse = kvClient.get(ByteSequence.from(registerKey, StandardCharsets.UTF_8)).get();
                        List<KeyValue> kvs = getResponse.getKvs();
                        if(CollUtil.isEmpty(kvs)){
                            //服务一下线需要重新启动
                            continue;
                        }
                        //需要重新注册服务
                        KeyValue keyValue = kvs.get(0);
                        ByteSequence byteSequence = keyValue.getValue();
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(new String(byteSequence.getBytes()), ServiceMetaInfo.class);
                        //重新注册
                        register(serviceMetaInfo);
                    }catch (Exception e){
                        throw new RuntimeException("心跳检测失败");
                    }
                }
            }
        });
        //支持秒级别的定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }
}
