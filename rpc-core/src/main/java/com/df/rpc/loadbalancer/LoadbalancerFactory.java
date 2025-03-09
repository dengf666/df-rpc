package com.df.rpc.loadbalancer;

import com.df.rpc.spi.SpiLoader;

public class LoadbalancerFactory {

    public static LoadBalancer getInstance(String loadBalancerType){
        Object instance = SpiLoader.getInstance(LoadBalancer.class, loadBalancerType);
        return (LoadBalancer) instance;
    }
}
