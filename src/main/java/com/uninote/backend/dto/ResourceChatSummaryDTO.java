package com.uninote.backend.dto;

import java.sql.Timestamp;

public class ResourceChatSummaryDTO {
    private Long chatId;
    private String uuid;
    private String resourceTitle;
    private String resourceType;
    private Timestamp createdAt;
    private String title;

    public ResourceChatSummaryDTO(Long chatId, String uuid, String resourceTitle, String resourceType, Timestamp createdAt, String title) {
        this.chatId = chatId;
        this.uuid = uuid;
        this.resourceTitle = resourceTitle;
        this.resourceType = resourceType;
        this.createdAt = createdAt;
        this.title = title;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getUuid() {
        return uuid;
    }

    public String getResourceTitle() {
        return resourceTitle;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getTitle() {
        return title;
    }
}
