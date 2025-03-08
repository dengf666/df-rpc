package com.df.rpc.serializer;

import com.df.rpc.spi.SpiLoader;

/**
 * 序列化器工厂
 */
public class SerializerFactory {

    public static Serializer getInstance(String serializerName){

        Object instance = SpiLoader.getInstance(Serializer.class, serializerName);
        return (Serializer) instance;
    }
}
