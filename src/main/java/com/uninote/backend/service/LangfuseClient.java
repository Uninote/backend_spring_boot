package com.uninote.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Service
public class LangfuseClient {

    @Value("${langfuse.base-url}")
    private String baseUrl;

    @Value("${langfuse.public-key}")
    private String publicKey;

    @Value("${langfuse.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void logGeneration(String traceId, boolean isFirstMessage, String userId, String prompt, String completion, String model) {
        String url = baseUrl + "/api/public/ingestion";

        List<Map<String, Object>> batch = new ArrayList<>();

        // If first message, create the trace first
        if (isFirstMessage) {
            Map<String, Object> traceEvent = new HashMap<>();
            traceEvent.put("id", UUID.randomUUID().toString());
            traceEvent.put("timestamp", Instant.now().toString());
            traceEvent.put("type", "trace-create");

            Map<String, Object> traceBody = new HashMap<>();
            traceBody.put("id", traceId);
            traceBody.put("timestamp", Instant.now().toString());
            traceBody.put("name", "Chat Session");
            traceBody.put("userId", userId);
            traceBody.put("input", prompt);
            traceBody.put("output", completion);
            traceBody.put("sessionId", traceId);
            traceBody.put("environment", "production");
            traceBody.put("release", "1.0.0");
            traceBody.put("version", "1.0.0");
            traceBody.put("metadata", new HashMap<>());
            traceBody.put("tags", List.of("chat"));
            traceBody.put("public", true);

            traceEvent.put("body", traceBody);
            batch.add(traceEvent);
        }

        // Always add generation event
        Map<String, Object> generationEvent = new HashMap<>();
        generationEvent.put("id", UUID.randomUUID().toString());
        generationEvent.put("timestamp", Instant.now().toString());
        generationEvent.put("type", "generation-create");

        Map<String, Object> generationBody = new HashMap<>();
        generationBody.put("id", UUID.randomUUID().toString());
        generationBody.put("traceId", traceId);
        generationBody.put("name", "chat-completion");
        generationBody.put("startTime", Instant.now().toString());
        generationBody.put("input", prompt);
        generationBody.put("output", completion);
        generationBody.put("model", model);
        generationBody.put("provider", "azure-openai");

        generationEvent.put("body", generationBody);
        batch.add(generationEvent);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("batch", batch);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(publicKey, secretKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Langfuse logged successfully: " + response.getStatusCode());
            System.out.println("Langfuse ingestion response: " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
