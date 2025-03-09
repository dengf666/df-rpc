package com.rpc.rpcspringbootstart.bootstrap;

import com.df.rpc.RpcApplication;
import com.df.rpc.config.RpcConfig;
import com.df.rpc.server.tcp.VertxTcpServer;
import com.rpc.rpcspringbootstart.annotation.EnableRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * Spring初始化执行，判断是否启动服务
 */
@Slf4j
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName());
        boolean needServer = (boolean)annotationAttributes.get("needServer");
        if (needServer) {
            // 启动服务端
            log.info("服务端启动");
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            VertxTcpServer vertxTcpServer = new VertxTcpServer();
            Integer serverPort = rpcConfig.getServerPort();
            vertxTcpServer.doStart(serverPort);
        }else {
            log.info("服务端未启动");
        }

    }
}
