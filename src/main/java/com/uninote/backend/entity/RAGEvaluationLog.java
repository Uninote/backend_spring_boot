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

    @Column(name = "question_text", columnDefinition = "text")
    private String questionText;

    @Column(name = "ai_response", columnDefinition = "text")
    private String aiResponse;

    @Column(name = "retrieval_chunks", columnDefinition = "text")
    private String retrievalChunks; // JSON string

    @Column(name = "evaluation_metrics", columnDefinition = "text")
    private String evaluationMetrics; // JSON string

    @Column(name = "user_rating", precision = 1, scale = 0)
    private Double userRating; // 1-5 scale

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "additional_data", columnDefinition = "text")
    private String additionalData; // JSON string for extensibility

    // Enhanced evaluation fields
    @Column(name = "model_name", length = 100)
    private String modelName; // e.g., "gpt-4", "gpt-3.5-turbo"

    @Column(name = "model_deployment", length = 100)
    private String modelDeployment; // Azure deployment name

    @Column(name = "input_tokens")
    private Integer inputTokens; // Number of input tokens

    @Column(name = "output_tokens")
    private Integer outputTokens; // Number of output tokens

    @Column(name = "total_tokens")
    private Integer totalTokens; // Total tokens used

    @Column(name = "chunk_count")
    private Integer chunkCount; // Number of chunks retrieved

    @Column(name = "chunk_sources")
    private String chunkSources; // JSON array of resource IDs used

    @Column(name = "system_prompt_length")
    private Integer systemPromptLength; // Length of system prompt

    @Column(name = "question_length")
    private Integer questionLength; // Length of user question

    @Column(name = "response_length")
    private Integer responseLength; // Length of AI response

    @Column(name = "is_first_message", precision = 1, scale = 0)
    private Integer isFirstMessage; // Oracle boolean as NUMBER(1): 1=true, 0=false

    @Column(name = "chat_uuid", length = 100)
    private String chatUuid; // Chat session identifier

    @Column(name = "session_id", length = 100)
    private String sessionId; // User session identifier

    @Column(name = "user_agent", length = 500)
    private String userAgent; // User's browser/client info

    @Column(name = "ip_address", length = 45)
    private String ipAddress; // User's IP address

    @Column(name = "error_occurred", precision = 1, scale = 0)
    private Integer errorOccurred; // Oracle boolean as NUMBER(1): 1=true, 0=false

    @Column(name = "error_message", length = 1000)
    private String errorMessage; // Error message if any

    @Column(name = "retrieval_time_ms")
    private Long retrievalTimeMs; // Time spent on chunk retrieval

    @Column(name = "generation_time_ms")
    private Long generationTimeMs; // Time spent on response generation

    @Column(name = "temperature")
    private Double temperature; // Model temperature setting

    @Column(name = "max_tokens")
    private Integer maxTokens; // Max tokens setting

    @Column(name = "top_p")
    private Double topP; // Top-p setting

    @Column(name = "frequency_penalty")
    private Double frequencyPenalty; // Frequency penalty setting

    @Column(name = "presence_penalty")
    private Double presencePenalty; // Presence penalty setting

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

    // Getters and setters for existing fields
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

    // Getters and setters for new evaluation fields
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getModelDeployment() { return modelDeployment; }
    public void setModelDeployment(String modelDeployment) { this.modelDeployment = modelDeployment; }

    public Integer getInputTokens() { return inputTokens; }
    public void setInputTokens(Integer inputTokens) { this.inputTokens = inputTokens; }

    public Integer getOutputTokens() { return outputTokens; }
    public void setOutputTokens(Integer outputTokens) { this.outputTokens = outputTokens; }

    public Integer getTotalTokens() { return totalTokens; }
    public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }

    public Integer getChunkCount() { return chunkCount; }
    public void setChunkCount(Integer chunkCount) { this.chunkCount = chunkCount; }

    public String getChunkSources() { return chunkSources; }
    public void setChunkSources(String chunkSources) { this.chunkSources = chunkSources; }

    public Integer getSystemPromptLength() { return systemPromptLength; }
    public void setSystemPromptLength(Integer systemPromptLength) { this.systemPromptLength = systemPromptLength; }

    public Integer getQuestionLength() { return questionLength; }
    public void setQuestionLength(Integer questionLength) { this.questionLength = questionLength; }

    public Integer getResponseLength() { return responseLength; }
    public void setResponseLength(Integer responseLength) { this.responseLength = responseLength; }

    public Integer getIsFirstMessage() { return isFirstMessage; }
    public void setIsFirstMessage(Integer isFirstMessage) { this.isFirstMessage = isFirstMessage; }

    public String getChatUuid() { return chatUuid; }
    public void setChatUuid(String chatUuid) { this.chatUuid = chatUuid; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Integer getErrorOccurred() { return errorOccurred; }
    public void setErrorOccurred(Integer errorOccurred) { this.errorOccurred = errorOccurred; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Long getRetrievalTimeMs() { return retrievalTimeMs; }
    public void setRetrievalTimeMs(Long retrievalTimeMs) { this.retrievalTimeMs = retrievalTimeMs; }

    public Long getGenerationTimeMs() { return generationTimeMs; }
    public void setGenerationTimeMs(Long generationTimeMs) { this.generationTimeMs = generationTimeMs; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

    public Double getTopP() { return topP; }
    public void setTopP(Double topP) { this.topP = topP; }

    public Double getFrequencyPenalty() { return frequencyPenalty; }
    public void setFrequencyPenalty(Double frequencyPenalty) { this.frequencyPenalty = frequencyPenalty; }

    public Double getPresencePenalty() { return presencePenalty; }
    public void setPresencePenalty(Double presencePenalty) { this.presencePenalty = presencePenalty; }

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
                ", modelName='" + modelName + '\'' +
                ", chunkCount=" + chunkCount +
                ", totalTokens=" + totalTokens +
                ", errorOccurred=" + errorOccurred +
                '}';
    }
} 