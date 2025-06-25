package com.uninote.backend.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "rag_evaluation_log")
public class RAGEvaluationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String promptVariant;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "message_id", nullable = false, length = 100)
    private String messageId;

    @Column(name = "chat_type", length = 50)
    private String chatType;

    @Lob
    @Column(name = "question_text")
    private String questionText;

    @Lob
    @Column(name = "ai_response")
    private String aiResponse;

    @Lob
    @Column(name = "retrieval_chunks")
    private String retrievalChunks; // JSON string

    @Lob
    @Column(name = "evaluation_metrics")
    private String evaluationMetrics; // JSON string

    @Column(name = "user_rating", precision = 1, scale = 0)
    private Double userRating; // 1-5 scale

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Lob
    @Column(name = "additional_data")
    private String additionalData; // JSON string for extensibility

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // Default constructor
    public RAGEvaluationLog() {}

    // Constructor with required fields
    public RAGEvaluationLog(String promptVariant, String messageId) {
        this.promptVariant = promptVariant;
        this.messageId = messageId;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPromptVariant() { return promptVariant; }
    public void setPromptVariant(String promptVariant) { this.promptVariant = promptVariant; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getChatType() { return chatType; }
    public void setChatType(String chatType) { this.chatType = chatType; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getAiResponse() { return aiResponse; }
    public void setAiResponse(String aiResponse) { this.aiResponse = aiResponse; }

    public String getRetrievalChunks() { return retrievalChunks; }
    public void setRetrievalChunks(String retrievalChunks) { this.retrievalChunks = retrievalChunks; }

    public String getEvaluationMetrics() { return evaluationMetrics; }
    public void setEvaluationMetrics(String evaluationMetrics) { this.evaluationMetrics = evaluationMetrics; }

    public Double getUserRating() { return userRating; }
    public void setUserRating(Double userRating) { this.userRating = userRating; }

    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getAdditionalData() { return additionalData; }
    public void setAdditionalData(String additionalData) { this.additionalData = additionalData; }

    @Override
    public String toString() {
        return "RAGEvaluationLog{" +
                "id=" + id +
                ", promptVariant='" + promptVariant + '\'' +
                ", userId='" + userId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", chatType='" + chatType + '\'' +
                ", userRating=" + userRating +
                ", responseTimeMs=" + responseTimeMs +
                ", timestamp=" + timestamp +
                '}';
    }
} 