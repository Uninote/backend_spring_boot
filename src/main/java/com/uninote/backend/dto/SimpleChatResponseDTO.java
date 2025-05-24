package com.uninote.backend.dto;


import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class SimpleChatResponseDTO {
    private Long id;
    private String title;
    private String uuid;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public SimpleChatResponseDTO(Long id, String title, String uuid, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.title = title;
        this.uuid = uuid;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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
}
