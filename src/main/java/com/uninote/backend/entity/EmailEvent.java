package com.uninote.backend.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "email_event", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "condition_sql"})})
public class EmailEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "condition_sql", nullable = false, columnDefinition = "TEXT")
    private String conditionSql;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public EmailEvent() {}
    public EmailEvent(Long userId, String conditionSql, LocalDateTime sentAt) {
        this.userId = userId;
        this.conditionSql = conditionSql;
        this.sentAt = sentAt;
    }
    // Getters and setters omitted for brevity
} 