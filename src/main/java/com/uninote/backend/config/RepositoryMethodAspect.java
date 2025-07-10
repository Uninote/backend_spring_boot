package com.uninote.backend.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RepositoryMethodAspect {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryMethodAspect.class);
    private static final Logger queryLogger = LoggerFactory.getLogger("QUERY_PERFORMANCE");

    @Autowired
    private QueryExecutionTimeInterceptor queryExecutionTimeInterceptor;

    /**
     * Intercept all methods in repository interfaces
     */
    @Around("execution(* com.uninote.backend.repository.*Repository.*(..))")
    public Object logRepositoryMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String fullMethodName = className + "." + methodName;
        
        // Get method arguments for logging
        String argsInfo = getMethodArgumentsInfo(joinPoint);
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log the execution time
            logRepositoryMethodExecution(fullMethodName, executionTime, argsInfo, result);
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log error with execution time
            logRepositoryMethodError(fullMethodName, executionTime, argsInfo, e);
            throw e;
        }
    }

    /**
     * Intercept all methods in JPA repositories (extends JpaRepository)
     */
    @Around("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public Object logJpaRepositoryMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String fullMethodName = className + "." + methodName;
        
        // Get method arguments for logging
        String argsInfo = getMethodArgumentsInfo(joinPoint);
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log the execution time
            logRepositoryMethodExecution(fullMethodName, executionTime, argsInfo, result);
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log error with execution time
            logRepositoryMethodError(fullMethodName, executionTime, argsInfo, e);
            throw e;
        }
    }

    private void logRepositoryMethodExecution(String methodName, long executionTime, String argsInfo, Object result) {
        // Use the existing query execution time interceptor
        queryExecutionTimeInterceptor.logQueryExecution("REPOSITORY", methodName, executionTime, argsInfo);
        
        // Additional detailed logging based on execution time
        if (executionTime > 5000) {
            logger.error("VERY SLOW REPOSITORY METHOD - {} took {}ms with args: {}", 
                methodName, executionTime, argsInfo);
        } else if (executionTime > 1000) {
            logger.warn("SLOW REPOSITORY METHOD - {} took {}ms with args: {}", 
                methodName, executionTime, argsInfo);
        } else if (executionTime > 500) {
            logger.info("MODERATE REPOSITORY METHOD - {} took {}ms with args: {}", 
                methodName, executionTime, argsInfo);
        } else {
            logger.debug("FAST REPOSITORY METHOD - {} took {}ms with args: {}", 
                methodName, executionTime, argsInfo);
        }
    }

    private void logRepositoryMethodError(String methodName, long executionTime, String argsInfo, Exception e) {
        logger.error("REPOSITORY METHOD ERROR - {} failed after {}ms with args: {}, Error: {}", 
            methodName, executionTime, argsInfo, e.getMessage());
        
        // Also log to query performance interceptor
        queryExecutionTimeInterceptor.logQueryExecution("REPOSITORY", methodName, executionTime, 
            "ERROR: " + e.getMessage() + " | Args: " + argsInfo);
    }

    private String getMethodArgumentsInfo(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "no args";
        }
        
        StringBuilder argsInfo = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) argsInfo.append(", ");
            
            if (args[i] == null) {
                argsInfo.append("null");
            } else if (args[i] instanceof Iterable) {
                Iterable<?> iterable = (Iterable<?>) args[i];
                int size = 0;
                for (Object item : iterable) {
                    size++;
                    if (size > 10) break; // Limit to first 10 items
                }
                argsInfo.append("Collection[").append(size).append(" items]");
            } else if (args[i].getClass().isArray()) {
                Object[] array = (Object[]) args[i];
                argsInfo.append("Array[").append(array.length).append(" items]");
            } else {
                String argStr = args[i].toString();
                if (argStr.length() > 100) {
                    argStr = argStr.substring(0, 100) + "...";
                }
                argsInfo.append(argStr);
            }
        }
        
        return argsInfo.toString();
    }
} 