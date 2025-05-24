package com.uninote.backend.dto;


import java.sql.Timestamp;
import java.util.Date;

public class SimpleChatSummaryDTO {
    private Long chatId;
    private String uuid;
    private Timestamp createdAt;
    private String title;

    public SimpleChatSummaryDTO(Long chatId, String uuid, Date createdAt, String title) {
        this.chatId = chatId;
        this.uuid = uuid;
        this.createdAt = createdAt != null ? new Timestamp(createdAt.getTime()) : null;
        this.title = title != null ? title : "";
    }

    public Long getChatId() {
        return chatId;
    }

    public String getUuid() {
        return uuid;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getTitle() {
        return title;
    }
}
