package com.uninote.backend.dto;

import java.sql.Timestamp;

public class ResourceChatResponseDTO {

    private Long id;
    private String title;
    private String uuid;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private String summary;
    private String url;
    private String type; // "file" or "youtube"

    public ResourceChatResponseDTO() {}

    public ResourceChatResponseDTO(
            Long id,
            String title,
            String uuid,
            Timestamp createdAt,
            Timestamp updatedAt,
            String summary,
            String url,
            String type
    ) {
        this.id = id;
        this.title = title;
        this.uuid = uuid;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.summary = summary;
        this.url = url;
        this.type = type;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
