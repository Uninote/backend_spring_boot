package com.uninote.backend.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "questionnaire_responses", schema = "ADMIN")
public class QuestionnaireResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "response_data", columnDefinition = "CLOB")
    private String responseData; // JSON string

    @Column(name = "firebase_response_id", nullable = false, unique = true)
    private String firebaseResponseId;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

    @Column(name = "is_complete")
    private Boolean isComplete = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public QuestionnaireResponse() {
        this.createdAt = LocalDateTime.now();
        this.completedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    public void setQuestionnaire(Questionnaire questionnaire) {
        this.questionnaire = questionnaire;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public String getFirebaseResponseId() {
        return firebaseResponseId;
    }

    public void setFirebaseResponseId(String firebaseResponseId) {
        this.firebaseResponseId = firebaseResponseId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setTimeTakenSeconds(Integer timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public Boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(Boolean isComplete) {
        this.isComplete = isComplete;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 