package com.uninote.backend.service.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${pinecone.api-key}")
    private String pineconeApiKey;

    @Value("${pinecone.index-url}") 
    private String pineconeIndexUrl; 

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", pineconeApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

   
    public void upsertVectors(List<PineconeVector> vectors) {
        try {
            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> vectorList = new ArrayList<>();

            for (PineconeVector vec : vectors) {
                Map<String, Object> vectorData = new HashMap<>();
                vectorData.put("id", vec.getId());
                vectorData.put("values", vec.getValues());
                vectorData.put("metadata", vec.getMetadata());
                vectorList.add(vectorData);
            }

            payload.put("vectors", vectorList);
            payload.put("namespace", "default"); 

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, getHeaders());

            String url = pineconeIndexUrl + "/vectors/upsert";

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Upsert response: " + response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Failed to upsert vectors into Pinecone: " + e.getMessage(), e);
        }
    }


    public JsonNode searchVector(float[] queryVector, Map<String, Object> filter, int topK) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("vector", queryVector);
            payload.put("topK", topK);
            payload.put("includeMetadata", true);
            payload.put("namespace", "default");
            if (filter != null && !filter.isEmpty()) {
                payload.put("filter", filter);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, getHeaders());

            String url = pineconeIndexUrl + "/query";

            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, request, JsonNode.class);
            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to search vectors in Pinecone: " + e.getMessage(), e);
        }
    }
}
