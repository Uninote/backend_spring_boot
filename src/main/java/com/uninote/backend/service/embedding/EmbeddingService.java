package com.uninote.backend.service.embedding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.model.embedding.EmbeddingModel;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final EmbeddingModel embeddingModel;

    private static final String DEFAULT_NAMESPACE = "default";


    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }


    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Custom ObjectMapper for logging that excludes vector values
    private final ObjectMapper loggingObjectMapper = new ObjectMapper();

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

    public float[] embed(String text) {
        try {
            var result = embeddingModel.embed(text);
            return result.content().vector();
        } catch (Exception e) {
            throw new RuntimeException("Failed to embed text: " + e.getMessage(), e);
        }
    }
   
    public void upsertVectors(List<PineconeVector> vectors) {
        upsertVectors(vectors, DEFAULT_NAMESPACE);
    }

    public void upsertVectors(List<PineconeVector> vectors, String namespace) {
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
            payload.put("namespace", namespace != null ? namespace : DEFAULT_NAMESPACE);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, getHeaders());
            String url = pineconeIndexUrl + "/vectors/upsert";

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            // Removed logging of Pinecone response to reduce noise

        } catch (Exception e) {
            throw new RuntimeException("Failed to upsert vectors into Pinecone: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a safe payload for logging (excludes vector values)
     */
    private Map<String, Object> createSafePayloadForLogging(List<PineconeVector> vectors, String namespace) {
        Map<String, Object> safePayload = new HashMap<>();
        List<Map<String, Object>> safeVectorList = new ArrayList<>();

        for (PineconeVector vec : vectors) {
            Map<String, Object> safeVectorData = new HashMap<>();
            safeVectorData.put("id", vec.getId());
            safeVectorData.put("values", "[VECTOR_DATA_HIDDEN]");
            safeVectorData.put("metadata", vec.getMetadata());
            safeVectorList.add(safeVectorData);
        }

        safePayload.put("vectors", safeVectorList);
        safePayload.put("namespace", namespace != null ? namespace : DEFAULT_NAMESPACE);
        return safePayload;
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


    public List<Map<String, String>> searchSimilarChunks(String query, Long resourceId, int topK) {
        float[] queryVector = embed(query);
    
        Map<String, Object> filter = Map.of("resource_id", resourceId);
    
        JsonNode result = searchVector(queryVector, filter, topK);
    
        List<Map<String, String>> chunks = new ArrayList<>();
    
        if (result.has("matches")) {
            for (JsonNode match : result.get("matches")) {
                Map<String, String> chunkData = new HashMap<>();
                JsonNode metadata = match.get("metadata");
    
                if (metadata != null && metadata.has("chunk_text")) {
                    chunkData.put("chunk_text", metadata.get("chunk_text").asText());
                }
    
                chunks.add(chunkData);
            }
        }
    
        return chunks;
    }

    public List<Map<String, String>> searchSimilarChunksAcrossResources(String query, Set<Long> resourceIds, int topK) {
        float[] queryVector = embed(query);
    
        Map<String, Object> filter = Map.of(
            "resource_id", Map.of("$in", resourceIds)
        );
    
        JsonNode result = searchVector(queryVector, filter, topK);
    
        List<Map<String, String>> chunks = new ArrayList<>();
    
        if (result.has("matches")) {
            for (JsonNode match : result.get("matches")) {
                Map<String, String> chunkData = new HashMap<>();
                JsonNode metadata = match.get("metadata");
    
                if (metadata != null && metadata.has("chunk_text")) {
                    chunkData.put("chunk_text", metadata.get("chunk_text").asText());
                }
    
                if (metadata != null && metadata.has("resource_id")) {
                    chunkData.put("resource_id", metadata.get("resource_id").asText());
                }
    
                chunks.add(chunkData);
            }
        }
    
        return chunks;
    }
    
    
}
