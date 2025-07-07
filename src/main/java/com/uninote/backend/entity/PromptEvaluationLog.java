package com.uninote.backend.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "prompt_evaluation_log")
public class PromptEvaluationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String promptVariant;

    @Column(name = "user_id")
    private String userId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "additional_data", columnDefinition = "text")
    private String additionalData; // JSON for extensibility

    public PromptEvaluationLog() {
        this.timestamp = LocalDateTime.now();
    }

    public PromptEvaluationLog(String promptVariant, String userId, String messageId, String requestId) {
        this();
        this.promptVariant = promptVariant;
        this.userId = userId;
        this.messageId = messageId;
        this.requestId = requestId;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPromptVariant() { return promptVariant; }
    public void setPromptVariant(String promptVariant) { this.promptVariant = promptVariant; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getAdditionalData() { return additionalData; }
    public void setAdditionalData(String additionalData) { this.additionalData = additionalData; }
} 