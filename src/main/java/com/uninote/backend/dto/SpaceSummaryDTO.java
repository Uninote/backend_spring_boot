package com.uninote.backend.dto;

import java.sql.Timestamp;

public class SpaceSummaryDTO {
    private Long id;
    private String title;
    private Timestamp createdAt;
    private String uuid;
    public SpaceSummaryDTO(Long id, String title, Timestamp createdAt, String uuid) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.uuid = uuid;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getUuid(){
        return uuid;
    }
}
