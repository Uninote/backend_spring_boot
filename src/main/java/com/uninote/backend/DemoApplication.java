package com.uninote.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;
import io.github.cdimascio.dotenv.Dotenv;
import com.uninote.backend.config.StripeConfig; // Add this import



@SpringBootApplication
@EnableAsync
public class DemoApplication {
    public static void main(String[] args) {
        String profile = System.getenv("SPRING_PROFILES_ACTIVE");

        if (profile == null || profile.isEmpty()) {
            profile = "development";
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
