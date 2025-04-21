package com.uninote.backend.dto;

import java.util.List;

public class ChatHistoryDto {
    private Long chatId;
    private String resourceTitle;
    private ResourceDTO resource;

    private List<MessageDTO> messages;

    public ChatHistoryDto(Long chatId, String resourceTitle, List<MessageDTO> messages) {
        this.chatId = chatId;
        this.resourceTitle = resourceTitle;
        this.messages = messages;
    }
    public ChatHistoryDto(Long chatId, String resourceTitle, ResourceDTO resource, List<MessageDTO> messages) {
        this.chatId = chatId;
        this.resourceTitle = resourceTitle;
        this.resource = resource;
        this.messages = messages;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getResourceTitle() {
        return resourceTitle;
    }

    public List<MessageDTO> getMessages() {
        return messages;
    }
    public ResourceDTO getResource() {
        return resource;
    }
}
