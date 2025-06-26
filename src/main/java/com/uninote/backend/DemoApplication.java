package com.uninote.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;



@SpringBootApplication
@EnableAsync
@EnableScheduling
public class DemoApplication {
    public static void main(String[] args) {
        String profile = System.getenv("SPRING_PROFILES_ACTIVE");

        if (profile == null || profile.isEmpty()) {
            profile = "dev";
        }

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // manually inject dotenv into system env if needed
        dotenv.entries().forEach(entry -> {
            if (System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(DemoApplication.class, args);
    }
}
