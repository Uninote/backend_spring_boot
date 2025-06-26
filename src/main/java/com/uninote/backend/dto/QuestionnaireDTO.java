package com.uninote.backend.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.uninote.backend.entity.QuestionnaireStatus;

public class QuestionnaireDTO {
    private Long id;
    private String name;
    private String description;
    private String firebasePath;
    private LocalDateTime createdAt;
    private LocalDateTime triggerTime;
    private QuestionnaireStatus status;
    private Map<String, Object> parameters;

    // Constructors
    public QuestionnaireDTO() {}

    public QuestionnaireDTO(Long id, String name, String description, String firebasePath, 
                           LocalDateTime createdAt, LocalDateTime triggerTime, 
                           QuestionnaireStatus status, Map<String, Object> parameters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.firebasePath = firebasePath;
        this.createdAt = createdAt;
        this.triggerTime = triggerTime;
        this.status = status;
        this.parameters = parameters;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFirebasePath() {
        return firebasePath;
    }

    public void setFirebasePath(String firebasePath) {
        this.firebasePath = firebasePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(LocalDateTime triggerTime) {
        this.triggerTime = triggerTime;
    }

    public QuestionnaireStatus getStatus() {
        return status;
    }

    public void setStatus(QuestionnaireStatus status) {
        this.status = status;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
} 