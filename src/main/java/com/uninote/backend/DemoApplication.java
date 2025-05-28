package com.uninote.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;
import io.github.cdimascio.dotenv.Dotenv;
import com.uninote.backend.config.StripeConfig; // Add this import



@SpringBootApplication

//@EnableCaching
@EnableAsync
public class DemoApplication {
    public static void main(String[] args) {
         String profile = System.getenv("SPRING_PROFILES_ACTIVE");

        if (profile == null || profile.isEmpty()) {
            profile = "development";
        }

        if ("development".equalsIgnoreCase(profile)) {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

        } 
        //Security.addProvider(new BouncyCastleProvider());
        SpringApplication.run(DemoApplication.class, args);
    }
}