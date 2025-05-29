package com.uninote.backend.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class PromptService {

    
    public String loadPromptTemplate(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource("prompts/" + fileName);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    
    public String fillPromptTemplate(String template, Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            template = template.replace(placeholder, entry.getValue());
        }
        return template;
    }

    
    public String getFilledPrompt(String fileName, Map<String, String> params) throws IOException {
        String rawTemplate = loadPromptTemplate(fileName);
        return fillPromptTemplate(rawTemplate, params);
    }
    public String createResourceSystemPrompt(String resourceTitle, String resourceContent) throws IOException {
        Map<String, String> params = Map.of(
            "resourceTitle", resourceTitle,
            "resourceContent", resourceContent
        );
        return getFilledPrompt("tutie_resource.txt", params);
    }

    public String createContentGenerationPrompt(String resourceTitle, String resourceType, String content) throws IOException {
        Map<String, String> params = Map.of(
            "resourceTitle", resourceTitle,
            "resourceType", resourceType,
            "content", content
        );
        return getFilledPrompt("tutie_content.txt", params);
    }

    public String createSimpleSystemPrompt() throws IOException {
        Map<String, String> params = Map.of(
            
        );
        return getFilledPrompt("tutie_general.txt", params);
    }

    public String createSpaceSystemPrompt(String resourcesSummary, String resourceSummaries) throws IOException {
        Map<String, String> params = Map.of(
            "resourceSummaries", resourceSummaries,
            "resourcesSummary", resourcesSummary
        );
        return getFilledPrompt("tutie_space.txt", params);
    }

    public String createLargeContentGenerationPrompt(String resourceTitle, String resourceType, int totalSections, int sectionNumber, String sectionText) throws IOException {
        Map<String, String> params = Map.of(
            "resourceTitle", resourceTitle,
            "resourceType", resourceType,
            "totalSections", String.valueOf(totalSections),
            "sectionNumber", String.valueOf(sectionNumber),
            "sectionText", sectionText
        );
        return getFilledPrompt("tutie_content_large.txt", params);
    }

    public String createLargeResourceSystemPrompt(String resourceTitle, String resourcesSummary, String chunksSection) throws IOException {
        Map<String, String> params = Map.of(
            "resourceTitle", resourceTitle,
            "resourcesSummary", resourcesSummary,
            "chunksSection",chunksSection
        );
        return getFilledPrompt("tutie_resource.txt", params);
    }
}