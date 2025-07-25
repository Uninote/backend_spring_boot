package com.uninote.backend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PosthogService {
    private static final String POSTHOG_URL = "https://us.i.posthog.com/capture/";

    @Value("${posthog.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void captureEvent(String eventName, String distinctId, Map<String, Object> properties, String isoTimestamp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("api_key", apiKey);
        body.put("event", eventName);
        Map<String, Object> props = new HashMap<>(properties != null ? properties : Map.of());
        props.put("distinct_id", distinctId);
        body.put("properties", props);
        if (isoTimestamp != null && !isoTimestamp.isEmpty()) {
            body.put("timestamp", isoTimestamp);
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(POSTHOG_URL, request, String.class);
            System.out.println("PostHog event sent: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("Failed to send PostHog event: " + e.getMessage());
        }
    }

    public void captureEvent(String eventName, String distinctId, Map<String, Object> properties) {
        captureEvent(eventName, distinctId, properties, null);
    }

    public void captureEvent(String eventName, String distinctId) {
        captureEvent(eventName, distinctId, null, null);
    }
} 