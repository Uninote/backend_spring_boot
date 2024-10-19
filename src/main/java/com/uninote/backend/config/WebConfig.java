package com.uninote.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "https://www.uninote.gr/","https://uninote.gr","https://*.ngrok-free.app","https://uninote-creators-portal.vercel.app/")
                .allowedHeaders("*")
                .allowedOriginPatterns("https://*.ngrok-free.app","https://uninote-creators-portal-eiz6j2q68-uninotes-projects.vercel.app","https://uninote-creators-portal.vercel.app/*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD")
                .allowCredentials(true);
    }
}
