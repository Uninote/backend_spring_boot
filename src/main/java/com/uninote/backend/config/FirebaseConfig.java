package com.uninote.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Firebase initialization using Application Default Credentials (ADC).
 * On Elastic Beanstalk, a predeploy hook writes the service-account JSON to:
 *   /opt/elasticbeanstalk/private/google-sa.json
 * and sets GOOGLE_APPLICATION_CREDENTIALS to point to that file.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    /**
     * Your default bucket. Override via:
     *  - application.yml: firebase.storage.bucket=uninote-app.appspot.com
     *  - or EB env var:   FIREBASE_STORAGE_BUCKET=uninote-app.appspot.com
     */
    @Value("${firebase.storage.bucket:uninote-app.appspot.com}")
    private String storageBucket;

    /**
     * Optional explicit path to credentials if you want to override ADC.
     * Normally you don't need this because GOOGLE_APPLICATION_CREDENTIALS is set by the EB hook.
     */
    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Reuse existing instance if already initialized (e.g., in tests or hot reload)
        if (!FirebaseApp.getApps().isEmpty()) {
            FirebaseApp existing = FirebaseApp.getInstance();
            log.debug("Reusing existing FirebaseApp: {}", existing.getName());
            return existing;
        }

        GoogleCredentials credentials;
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            // Optional explicit file path override
            try (FileInputStream fis = new FileInputStream(credentialsPath)) {
                credentials = GoogleCredentials.fromStream(fis);
            }
            log.info("Initialized Firebase credentials from explicit path.");
        } else {
            // Application Default Credentials (honors GOOGLE_APPLICATION_CREDENTIALS)
            credentials = GoogleCredentials.getApplicationDefault();
            log.info("Initialized Firebase credentials via Application Default Credentials.");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setStorageBucket(storageBucket)
                .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);
        log.info("FirebaseApp initialized. Storage bucket: {}", storageBucket);
        return app;
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp app) {
        // Ensure FirebaseApp bean is created first
        return FirebaseAuth.getInstance(app);
    }
}
