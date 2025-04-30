package com.uninote.backend;

//import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import io.github.cdimascio.dotenv.Dotenv;


//import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;  


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