package com.uninote.backend;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;  

@SpringBootApplication(scanBasePackages = "com.uninote.backend")
//@EnableCaching
@EnableAsync
public class DemoApplication {
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        SpringApplication.run(DemoApplication.class, args);
    }
}