package com.uninote.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "https://www.uninote.gr/","https://uninote.gr","https://*.ngrok-free.app","https://uninote-creators-portal.vercel.app/","https://uninote-node-dashboard-2fa17dd3fc4c.herokuapp.com/")
                .allowedHeaders("*")
                .allowedOriginPatterns("https://*.ngrok-free.app","https://uninote-creators-portal-eiz6j2q68-uninotes-projects.vercel.app","https://uninote-creators-portal.vercel.app/*","https://uninote-node-dashboard-2fa17dd3fc4c.herokuapp.com/")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD")
                .allowCredentials(true);
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/health", "/info", "/actuator/**");
    }
}