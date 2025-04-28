package com.uninote.backend.dto;


import java.sql.Timestamp;
import java.time.LocalDateTime;

public class SpaceDTO {

    private Long id;
    private String title;
    private String uuid;
    private Timestamp createdAt;

    // Constructor
    public SpaceDTO(Long id, String title, String uuid, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.uuid = uuid;
        this.createdAt = createdAt;
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
}
