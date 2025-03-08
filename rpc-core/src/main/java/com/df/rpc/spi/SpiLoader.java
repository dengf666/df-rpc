package com.df.rpc.spi;


import cn.hutool.core.io.resource.ResourceUtil;
import com.df.rpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * spi 加载器
 */
@Slf4j
public class SpiLoader {

    /**
     * 存储已加载的类：接口名 =>（key => 实现类）
     */
    private static Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * 对象实例缓存（避免重复 new），类路径 => 对象实例，单例模式
     */
    private static Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * 系统 SPI 目录
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    /**
     * 用户自定义 SPI 目录
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};

    /**
     * 动态加载的类列表
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);


    /**
     * 加载所有类
     */
    public static void loadAll(){
        for (Class<?> loadClass : LOAD_CLASS_LIST) {
            load(loadClass);
        }
    }
    /**
     * 根据key获取实例
     * @param loadClass
     * @param key
     * @return
     */
    public static Object getInstance(Class<?> loadClass, String key) {
        load(loadClass);
        String name = loadClass.getName();
        if(!loaderMap.containsKey(name)) {
            throw new RuntimeException("未找到 SPI 类：" + name);
        }
        Map<String, Class<?>> classMap = loaderMap.get(name);
        if(!classMap.containsKey(key)) {
            throw new RuntimeException("未找到 SPI 类：" + name + " 的 key：" + key);
        }
        Class<?> clazz = classMap.get(key);
        if(!instanceCache.containsKey(clazz.getName())) {
            try {
                //将创建的实例对象加入缓存中
                instanceCache.put(clazz.getName(), clazz.newInstance());
            } catch (Exception e) {
                log.error("实例化 SPI 类失败：{}", clazz.getName());
                throw new RuntimeException(e);
            }
        }
        return instanceCache.get(clazz.getName());
    }

    /**
     * 加载类
     * @param loadClass
     * @return
     */
    public static Map<String,Map<String,Class<?>>> load(Class<?> loadClass) {
        String name = loadClass.getName();
        log.info("开始加载 SPI 类：{}", name);
        Map<String, Class<?>> classMap = new HashMap<>();
        for (String scanDir : SCAN_DIRS) {
            List<URL> resources = ResourceUtil.getResources(scanDir + name);
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null){
                        String[] split = line.split("=");
                        if(split.length > 1) {
                            String key = split[0];
                            String className = split[1];
                            try {
                                classMap.put(key, Class.forName(className));
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }
                } catch (Exception e) {
                    log.info("加载 SPI 类失败：{}", name);
                    throw new RuntimeException(e);
                }
            }
        }
        Map<String,Map<String,Class<?>>> result = new HashMap<>();
        result.put(name, classMap);
        loaderMap = result;
        return result;
    }
}
