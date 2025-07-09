package com.uninote.backend.config;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String START_TIME_ATTRIBUTE = "startTime";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestId = UUID.randomUUID().toString();
        request.setAttribute(REQUEST_ID_HEADER, requestId);
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String userAgent = request.getHeader("User-Agent");
        String remoteAddr = getClientIpAddress(request);
        
        logger.info("=== HTTP REQUEST START ===");
        logger.info("Request ID: {}", requestId);
        logger.info("Method: {} {}", method, uri + (queryString != null ? "?" + queryString : ""));
        logger.info("Remote Address: {}", remoteAddr);
        logger.info("User-Agent: {}", userAgent);
        logger.info("Content-Type: {}", request.getContentType());
        logger.info("Content-Length: {}", request.getContentLength());
        
        // Log headers (excluding sensitive ones)
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            if (!isSensitiveHeader(headerName)) {
                logger.debug("Header {}: {}", headerName, request.getHeader(headerName));
            }
        });
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // This method is called after the handler is executed but before the view is rendered
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestId = (String) request.getAttribute(REQUEST_ID_HEADER);
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
        
        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        logger.info("=== HTTP REQUEST END ===");
        logger.info("Request ID: {}", requestId);
        logger.info("Method: {} {} - Status: {} - Duration: {}ms", method, uri, status, duration);
        
        if (ex != null) {
            logger.error("Exception occurred during request processing: {}", ex.getMessage(), ex);
        }
        
        logger.info("=== END REQUEST ===\n");
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private boolean isSensitiveHeader(String headerName) {
        String lowerHeader = headerName.toLowerCase();
        return lowerHeader.contains("authorization") || 
               lowerHeader.contains("cookie") || 
               lowerHeader.contains("password") || 
               lowerHeader.contains("secret") ||
               lowerHeader.contains("key");
    }
} 