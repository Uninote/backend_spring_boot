package com.uninote.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.firebase.database.FirebaseDatabase;

@Configuration
public class FirebaseDatabaseConfig {

    @Value("${firebase.database.url:https://uninote-app-default-rtdb.firebaseio.com}")
    private String databaseUrl;

    @Bean
    public FirebaseDatabase firebaseDatabase() {
        return FirebaseDatabase.getInstance(databaseUrl);
    }
} 