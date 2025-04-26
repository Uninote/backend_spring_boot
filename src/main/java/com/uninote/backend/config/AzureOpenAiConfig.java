package com.uninote.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "azure.openai")
public class AzureOpenAiConfig {

    private String azureEndpoint;
    private String azureApiKey;
    private String chatDeployment;

    public String getAzureEndpoint() {
        return azureEndpoint;
    }

    public void setAzureEndpoint(String azureEndpoint) {
        this.azureEndpoint = azureEndpoint;
    }

    public String getAzureApiKey() {
        return azureApiKey;
    }

    public void setAzureApiKey(String azureApiKey) {
        this.azureApiKey = azureApiKey;
    }

    public String getChatDeployment() {
        return chatDeployment;
    }

    public void setChatDeployment(String chatDeployment) {
        this.chatDeployment = chatDeployment;
    }
}
