package com.uninote.backend.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for handling JSON operations to prevent double-encoding issues
 */
public class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse a JSON string into a JsonNode, handling null/empty values and malformed JSON
     */
    public static JsonNode parseJsonString(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return objectMapper.createArrayNode(); // Return empty array as default
        }

        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse JSON string: {}", e.getMessage());
            // If JSON parsing fails, return empty array
            return objectMapper.createArrayNode();
        }
    }

    /**
     * Parse a JSON string into an Object, handling null/empty values and malformed JSON
     */
    public static Object parseJsonStringToObject(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new ArrayList<>(); // Return empty array as default
        }

        try {
            return objectMapper.readValue(jsonString, Object.class);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse JSON string to object: {}", e.getMessage());
            // If JSON parsing fails, return empty array
            return new ArrayList<>();
        }
    }

    /**
     * Convert a JsonNode to a List of objects
     */
    public static List<Object> jsonNodeToList(JsonNode jsonNode) {
        if (jsonNode != null && jsonNode.isArray()) {
            try {
                return objectMapper.convertValue(jsonNode, List.class);
            } catch (Exception e) {
                logger.warn("Failed to convert JsonNode to List: {}", e.getMessage());
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    /**
     * Convert a List to a JsonNode
     */
    public static JsonNode listToJsonNode(List<?> list) {
        if (list != null) {
            return objectMapper.valueToTree(list);
        }
        return objectMapper.createArrayNode();
    }

    /**
     * Safely serialize an object to JSON string
     */
    public static String toJsonString(Object obj) {
        if (obj == null) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize object to JSON: {}", e.getMessage());
            return "[]";
        }
    }

    /**
     * Check if a string is valid JSON
     */
    public static boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }

        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * Get ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
