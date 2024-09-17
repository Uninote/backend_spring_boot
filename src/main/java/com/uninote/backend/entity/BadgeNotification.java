package com.uninote.backend.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "badge_notification", schema = "ADMIN")
public class BadgeNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", nullable = false)
    private Long badgeId;      

    @Column(name = "user_id", nullable = false)
    private Long userId;     

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;   

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt; 

    @Column(name = "delivered", nullable = false)
    private boolean delivered; 

    // Default constructor required by JPA
    public BadgeNotification() {
    }

    // Constructor to initialize fields
    public BadgeNotification(Long badgeId, Long userId, LocalDateTime earnedAt) {
        this.badgeId = badgeId;
        this.userId = userId;
        this.earnedAt = earnedAt;
        this.delivered = false; 
        this.deliveredAt = null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(Long badgeId) {
        this.badgeId = badgeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(LocalDateTime earnedAt) {
        this.earnedAt = earnedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    // Method to mark the notification as delivered
    public void markAsDelivered() {
        this.delivered = true;
        this.deliveredAt = LocalDateTime.now();
    }
}
