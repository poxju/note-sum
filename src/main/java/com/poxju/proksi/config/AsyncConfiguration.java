package com.poxju.proksi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuration for async task execution.
 * Prevents unbounded thread creation and provides proper resource management.
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Value("${spring.task.execution.pool.core-size:2}")
    private int corePoolSize;

    @Value("${spring.task.execution.pool.max-size:4}")
    private int maxPoolSize;

    @Value("${spring.task.execution.pool.queue-capacity:10}")
    private int queueCapacity;

    @Value("${spring.task.execution.thread-name-prefix:async-}")
    private String threadNamePrefix;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        
        // Rejection policy: Caller runs policy prevents task loss
        // This ensures tasks are executed even when pool is full
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Allow core threads to timeout to free resources when idle
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(60);
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }
}
