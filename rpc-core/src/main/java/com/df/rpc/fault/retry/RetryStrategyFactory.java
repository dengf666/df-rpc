package com.df.rpc.fault.retry;
import com.df.rpc.spi.SpiLoader;

/**
 * 重试策略工厂（用于获取重试器对象）
 */
public class RetryStrategyFactory {

    static {
        SpiLoader.load(RetryStrategy.class);
    }

    /**
     * 默认重试器
     */
    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static RetryStrategy getInstance(String key) {
        return (RetryStrategy) SpiLoader.getInstance(RetryStrategy.class, key);
    }

}
