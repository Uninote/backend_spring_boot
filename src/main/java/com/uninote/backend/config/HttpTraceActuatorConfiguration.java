package com.uninote.backend.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpTraceActuatorConfiguration {

    // Disabled HTTP tracing to prevent embedding vector logging
    // @Bean
    // public HttpTraceRepository httpTraceRepository() {
    //     return new InMemoryHttpTraceRepository();
    // }
}
