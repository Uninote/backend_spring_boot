package com.uninote.backend.dto;

import java.sql.Timestamp;

import com.uninote.backend.entity.MessageMedia;

public class MessageMediaDTO {
    private Long id;
    private String mediaUrl;
    private String mediaType;
    private String originalFilename;
    private Timestamp uploadedAt;

    public MessageMediaDTO(MessageMedia media) {
        this.id = media.getId();
        this.mediaUrl = media.getMediaUrl();
        this.mediaType = media.getMediaType();
        this.originalFilename = media.getOriginalFilename();
        this.uploadedAt = media.getUploadedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Timestamp getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @Override
    public String toString() {
        return "MessageMediaDTO{" +
                "id=" + id +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", originalFilename='" + originalFilename + '\'' +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
} 