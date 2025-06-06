package com.uninote.backend.service;

import com.uninote.backend.entity.UserProfile;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.uninote.backend.config.AzureOpenAiConfig;

import java.util.List;

@Service
public class UserProfileService {

   /* private final ChatLanguageModel model;

    @Autowired
    private AzureOpenAiConfig azureConfig;

    public UserProfileService() {
        this.model = AzureOpenAiChatModel.builder()
                .endpoint(azureConfig.getAzureEndpoint())
                .apiKey(azureConfig.getAzureApiKey())
                .deploymentName(azureConfig.getChatDeployment())
                .temperature(0.0)
                .build();
    }

    public UserProfile analyzeConversation(List<String> chatHistory) {
        String prompt = buildPrompt(chatHistory);
        return model.generate(prompt, StructuredOutputParser.json(UserProfile.class));
    }

    private String buildPrompt(List<String> chatHistory) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an AI profile builder assistant. ");
        sb.append("You are analyzing this user's conversation history. ");
        sb.append("Based on the provided messages, extract the following fields and return as JSON: \n\n");
        sb.append("{\n");
        sb.append("  \"interests\": [],\n");
        sb.append("  \"goals\": [],\n");
        sb.append("  \"personality_traits\": [],\n");
        sb.append("  \"common_questions\": [],\n");
        sb.append("  \"shortcomings\": [],\n");
        sb.append("  \"engagement_level\": \"\",\n");
        sb.append("  \"general_description\": \"\"\n");
        sb.append("}\n\n");
        sb.append("Be concise but informative. Assume you are building a long-term behavioral profile.\n");
        sb.append("Here is the chat history:\n");

        for (String message : chatHistory) {
            sb.append("- ").append(message).append("\n");
        }

        return sb.toString();
    } */
}
