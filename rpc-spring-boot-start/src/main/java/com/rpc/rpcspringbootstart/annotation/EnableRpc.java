package com.rpc.rpcspringbootstart.annotation;

import com.rpc.rpcspringbootstart.bootstrap.RpcInitBootstrap;
import com.rpc.rpcspringbootstart.bootstrap.RpcProviderBootstrap;
import com.rpc.rpcspringbootstart.bootstrap.RpcReferenceBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 Rpc 注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcReferenceBootstrap.class})
public @interface EnableRpc {

    /**
     * 需要启动 server
     *
     * @return
     */
    boolean needServer() default true;
}
