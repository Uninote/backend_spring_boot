package com.uninote.backend.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class EmbeddingConfig {

    @Autowired
    private AzureOpenAiConfig azureOpenAiConfig;

    @Bean
    public EmbeddingModel embeddingModel() {
        return AzureOpenAiEmbeddingModel.builder()
            .endpoint(azureOpenAiConfig.getAzureEndpoint())
            .apiKey(azureOpenAiConfig.getAzureApiKey())
            .deploymentName(azureOpenAiConfig.getEmbeddingDeployment())
            .build();
    }
}
