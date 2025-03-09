package com.rpc.rpcspringbootstart.bootstrap;

import cn.hutool.aop.proxy.ProxyFactory;
import com.df.rpc.proxy.ServiceProxyFactory;
import com.rpc.rpcspringbootstart.annotation.RpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
 *  在添加了RpcReference注解的类中，会自动注入代理对象
 */
public class RpcReferenceBootstrap implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] declaredFields = beanClass.getDeclaredFields();
        for (Field field : declaredFields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);

            if (rpcReference != null) {
                Class<?> interfaceClass = rpcReference.interfaceClass();
                if(interfaceClass == void.class){
                    interfaceClass = field.getType();
                }
                //调用代理
                Object proxy = ServiceProxyFactory.getProxy(interfaceClass);
                field.setAccessible(true);
                try {
                    //将代理对象注入变量
                    field.set(bean,proxy);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
