package com.rpc.rpcspringbootstart.bootstrap;

import com.df.rpc.RpcApplication;
import com.df.rpc.config.RpcConfig;
import com.df.rpc.model.ServiceMetaInfo;
import com.df.rpc.registry.LocalRegistry;
import com.df.rpc.registry.Registry;
import com.df.rpc.registry.RegistryFactory;
import com.rpc.rpcspringbootstart.annotation.RpcReference;
import com.rpc.rpcspringbootstart.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.logging.ErrorManager;

public class RpcProviderBootstrap implements BeanPostProcessor {

    /**
     * 在bean初始化完成后执行，进行服务注册，
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //获取类型
        Class<?> aClass = bean.getClass();
        RpcService rpcService = aClass.getAnnotation(RpcService.class);
        if(rpcService != null){
            //需要进行服务注册
            Class<?> interfaceClass = rpcService.interfaceClass();
            if(interfaceClass == void.class){
                interfaceClass = aClass.getInterfaces()[0];
            }
            //本地注册服务
            LocalRegistry.register(interfaceClass.getName(),aClass);
            
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();

            serviceMetaInfo.setServiceName(interfaceClass.getName());
            serviceMetaInfo.setServiceVersion(rpcService.serviceVersion());
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            serviceMetaInfo.setServiceGroup(null);
            
            try {
                //远程注册服务
                Registry registry = RegistryFactory.getRegistry(rpcConfig.getRegistry().getRegistry());
                registry.register(serviceMetaInfo);
            }catch (Exception e){
                throw new RuntimeException("服务注册失败");
            }

        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
