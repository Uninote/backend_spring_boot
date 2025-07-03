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
@Table(name = "questionnaire_acknowledgments" )
public class QuestionnaireAcknowledgment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "acknowledgment_type", nullable = false, length = 50)
    private String acknowledgmentType;

    @Column(name = "acknowledgment_data", columnDefinition = "CLOB")
    private String acknowledgmentData;

    @Column(name = "acknowledged_at", nullable = false)
    private LocalDateTime acknowledgedAt;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "client_info", length = 500)
    private String clientInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public QuestionnaireAcknowledgment() {
        this.createdAt = LocalDateTime.now();
        this.acknowledgedAt = LocalDateTime.now();
    }

    public QuestionnaireAcknowledgment(Questionnaire questionnaire, User user, String acknowledgmentType) {
        this();
        this.questionnaire = questionnaire;
        this.user = user;
        this.acknowledgmentType = acknowledgmentType;
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

    public String getAcknowledgmentType() {
        return acknowledgmentType;
    }

    public void setAcknowledgmentType(String acknowledgmentType) {
        this.acknowledgmentType = acknowledgmentType;
    }

    public String getAcknowledgmentData() {
        return acknowledgmentData;
    }

    public void setAcknowledgmentData(String acknowledgmentData) {
        this.acknowledgmentData = acknowledgmentData;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public boolean isReceived() {
        return "RECEIVED".equals(acknowledgmentType);
    }

    public boolean isStarted() {
        return "STARTED".equals(acknowledgmentType);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(acknowledgmentType);
    }

    public boolean isDismissed() {
        return "DISMISSED".equals(acknowledgmentType);
    }

    public boolean isProgressUpdate() {
        return "PROGRESS_UPDATE".equals(acknowledgmentType);
    }

    @Override
    public String toString() {
        return "QuestionnaireAcknowledgment{" +
                "id=" + id +
                ", questionnaireId=" + (questionnaire != null ? questionnaire.getId() : null) +
                ", userId=" + (user != null ? user.getId() : null) +
                ", acknowledgmentType='" + acknowledgmentType + '\'' +
                ", acknowledgedAt=" + acknowledgedAt +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
} 