package com.uninote.backend.dto;

import java.util.Date;

public class SpaceSummaryDTO {
    private Long id;
    private String title;
    private Date createdAt;
    private String uuid;

    public SpaceSummaryDTO(long id, String title, Date createdAt, String uuid) {
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getUuid() {
        return uuid;
    }
}
