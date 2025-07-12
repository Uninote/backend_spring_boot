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
        executor.setCorePoolSize(4); // Reduced from 8 to prevent memory overload
        executor.setMaxPoolSize(8); // Reduced from 16 to prevent memory overload
        executor.setQueueCapacity(50); // Reduced from 100 to prevent memory buildup
        executor.setThreadNamePrefix("ContentGen-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // Add memory-aware rejection handler
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                // Log rejection and force GC before retrying
                System.gc();
                try {
                    // Wait a bit and try again
                    Thread.sleep(1000);
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
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(25); // Reduced from 50
        executor.setThreadNamePrefix("Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // Add memory-aware rejection handler
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.gc();
                try {
                    Thread.sleep(500);
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
        executor.setCorePoolSize(2); // Small pool for memory-intensive embedding tasks
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("Embedding-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120); // Longer timeout for embedding tasks
        
        // Add memory-aware rejection handler
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.gc();
                try {
                    Thread.sleep(2000); // Longer wait for embedding tasks
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