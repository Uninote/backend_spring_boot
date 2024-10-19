package com.uninote.backend.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "uniscore_increase_logs", schema = "ADMIN")
public class UniscoreIncreaseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "increase_type_id", nullable = false)
    private UniscoreIncreaseType increaseType;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    

    public UniscoreIncreaseLog() {}

    public UniscoreIncreaseLog(User user, UniscoreIncreaseType increaseType) {
        this.user = user;
        this.increaseType = increaseType;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UniscoreIncreaseType getIncreaseType() {
        return increaseType;
    }

    public void setIncreaseType(UniscoreIncreaseType increaseType) {
        this.increaseType = increaseType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
