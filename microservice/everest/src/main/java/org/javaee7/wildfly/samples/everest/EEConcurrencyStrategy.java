package org.javaee7.wildfly.samples.everest;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.enterprise.concurrent.ManagedThreadFactory;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;

/**
 * @author Heiko Braun
 * @since 01/06/16
 */
public class EEConcurrencyStrategy extends HystrixConcurrencyStrategy {

    public EEConcurrencyStrategy(ManagedThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixProperty<Integer> corePoolSize, HystrixProperty<Integer> maximumPoolSize, HystrixProperty<Integer> keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        // All threads will run as part of this application component.
        return new ThreadPoolExecutor(5, 10, 5, TimeUnit.SECONDS,  new ArrayBlockingQueue<Runnable>(10), threadFactory);
    }

    private final ManagedThreadFactory threadFactory;
}
