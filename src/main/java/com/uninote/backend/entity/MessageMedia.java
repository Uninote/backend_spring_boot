package com.uninote.backend.entity;

import java.sql.Timestamp;

import javax.persistence.*;

@Entity
@Table(name = "MESSAGE_MEDIA")
public class MessageMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MESSAGE_MEDIA_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MESSAGE_ID")
    private Message message;

    @Column(name = "MEDIA_URL")
    private String mediaUrl;

    @Column(name = "MEDIA_TYPE")
    private String mediaType = "image";

    @Column(name = "ORIGINAL_FILENAME")
    private String originalFilename;

    @Column(name = "UPLOADED_AT")
    private Timestamp uploadedAt;

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
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
        return "MessageMedia{" +
                "id=" + id +
                ", message=" + message.getId() +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", originalFilename='" + originalFilename + '\'' +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}
