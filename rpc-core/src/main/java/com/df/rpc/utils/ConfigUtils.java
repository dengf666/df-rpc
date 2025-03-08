package com.df.rpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;


/**
 * 读取配置的工具类
 */
public class ConfigUtils {

    public static <T> T loadConfig(Class<T> clazz, String prefix) {
        return loadConfig(clazz, prefix, null);
    }

    /**
     * 读取配置
     * @param clazz
     * @param prefix
     * @param environment
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> clazz, String prefix, String environment) {
        StringBuilder fileName = new StringBuilder("application");
        if(StrUtil.isNotBlank(environment)) {
            fileName.append("-").append(environment);
        }
        fileName.append(".properties");

        Props props = new Props(fileName.toString());
        return props.toBean(clazz, prefix);
    }
}
