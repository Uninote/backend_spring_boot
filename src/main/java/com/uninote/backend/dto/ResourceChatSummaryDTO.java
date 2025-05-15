package com.uninote.backend.dto;

import java.sql.Timestamp;
import java.util.Date;

public class ResourceChatSummaryDTO {
    private Long chatId;
    private String uuid;
    private String resourceTitle;
    private String resourceType;
    private Timestamp createdAt;
    private String title;


    public ResourceChatSummaryDTO(Long chatId, String uuid, String resourceTitle, Date createdAt, String title) {
        this.chatId = chatId;
        this.uuid = uuid;
        this.resourceTitle = resourceTitle != null ? resourceTitle : "";
        this.createdAt = createdAt != null ? new Timestamp(createdAt.getTime()) : null;
        this.title = title != null ? title : "";
        this.resourceType = "";
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
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getTitle() {
        return title;
    }
}
