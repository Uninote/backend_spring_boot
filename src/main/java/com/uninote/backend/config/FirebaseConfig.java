package com.uninote.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

   

    @Value("${firebase.config.base64:}")
    private String firebaseConfigBase64;


    @PostConstruct
    public void init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
    
            if (firebaseConfigBase64 == null || firebaseConfigBase64.isEmpty()) {
                Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
                firebaseConfigBase64 = dotenv.get("FIREBASE_CONFIG_BASE64");
            }
    
            if (firebaseConfigBase64 == null || firebaseConfigBase64.isEmpty()) {
                throw new IllegalStateException("FIREBASE_CONFIG_BASE64 is not set.");
            }
    
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(firebaseConfigBase64);
    
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new java.io.ByteArrayInputStream(decodedBytes)))
                .setStorageBucket("uninote-app.appspot.com")
                .build();
    
            FirebaseApp.initializeApp(options);
        }
    }
    

    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance(); 
    }
}

