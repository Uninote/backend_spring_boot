package com.uninote.backend.config;

import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpTraceActuatorConfiguration {

    @Bean
    public HttpTraceRepository httpTraceRepository() {
        InMemoryHttpTraceRepository repository = new InMemoryHttpTraceRepository();
        repository.setCapacity(100); // Limit to 100 traces to prevent memory leaks
        return repository;
    }
}
