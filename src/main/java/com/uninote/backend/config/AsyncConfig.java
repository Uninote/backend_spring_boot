package com.uninote.backend.config;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "contentGenerationExecutor")
    public Executor contentGenerationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Further reduced to prevent memory overload
        executor.setMaxPoolSize(4); // Further reduced to prevent memory overload
        executor.setQueueCapacity(20); // Further reduced to prevent memory buildup
        executor.setThreadNamePrefix("ContentGen-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // Add aggressive memory-aware rejection handler
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                // Force aggressive GC and wait longer
                System.gc();
                try {
                    // Wait longer and try again
                    Thread.sleep(3000);
                    executor.execute(r);
                } catch (Exception e) {
                    throw new RuntimeException("Task execution rejected after retry", e);
                }
            }
        });
        
        executor.initialize();
        return executor;
    }

    @Bean(name = "generalAsyncExecutor")
    public Executor generalAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10); // Further reduced
        executor.setThreadNamePrefix("Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // Add memory-aware rejection handler
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.gc();
                try {
                    Thread.sleep(2000); // Longer wait
                    executor.execute(r);
                } catch (Exception e) {
                    throw new RuntimeException("Task execution rejected after retry", e);
                }
            }
        });
        
        executor.initialize();
        return executor;
    }
    
    @Bean(name = "embeddingExecutor")
    public Executor embeddingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1); // Single thread for memory-intensive embedding tasks
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(5); // Very small queue
        executor.setThreadNamePrefix("Embedding-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120); // Longer timeout for embedding tasks
        
        // Add aggressive memory-aware rejection handler
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.gc();
                try {
                    Thread.sleep(5000); // Much longer wait for embedding tasks
                    executor.execute(r);
                } catch (Exception e) {
                    throw new RuntimeException("Embedding task execution rejected after retry", e);
                }
            }
        });
        
        executor.initialize();
        return executor;
    }
} 