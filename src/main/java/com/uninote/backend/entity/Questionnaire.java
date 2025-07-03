package com.uninote.backend.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "questionnaires")
public class Questionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "CLOB")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "trigger_time")
    private LocalDateTime triggerTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private QuestionnaireStatus status;

    @Lob
    @Column(name = "questionnaire_json", columnDefinition = "TEXT")
    private String questionnaireJson; // Complete questionnaire JSON

    @Lob
    @Column(name = "criteria_query", columnDefinition = "TEXT")
    private String criteriaQuery; // SQL query for targeting users

    // Constructors
    public Questionnaire() {}

    public Questionnaire(String name, String description) {
        this.name = name;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.status = QuestionnaireStatus.ACTIVE;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getTriggerTime() { return triggerTime; }
    public void setTriggerTime(LocalDateTime triggerTime) { this.triggerTime = triggerTime; }

    public QuestionnaireStatus getStatus() { return status; }
    public void setStatus(QuestionnaireStatus status) { this.status = status; }

    public String getQuestionnaireJson() { return questionnaireJson; }
    public void setQuestionnaireJson(String questionnaireJson) { this.questionnaireJson = questionnaireJson; }

    public String getCriteriaQuery() { return criteriaQuery; }
    public void setCriteriaQuery(String criteriaQuery) { this.criteriaQuery = criteriaQuery; }

    // Helper methods
    public String getSanitizedName() {
        return name.replaceAll("[^a-zA-Z0-9_]", "_");
    }
} 