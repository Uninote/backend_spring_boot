package com.uninote.backend.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_badges", schema = "ADMIN")
public class UserBadge {
    @EmbeddedId
    private UserBadgeId id = new UserBadgeId();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("badgeId")
    @JoinColumn(name = "badge_id")
    private Badge badge;

    private LocalDateTime awardedAt;

    // Getters and setters
    public UserBadgeId getId() {
        return id;
    }

    public void setId(UserBadgeId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        this.id.setUserId(user.getId());
    }

    public Badge getBadge() {
        return badge;
    }

    public void setBadge(Badge badge) {
        this.badge = badge;
        this.id.setBadgeId(badge.getId());
    }

    public LocalDateTime getAwardedAt() {
        return awardedAt;
    }

    public void setAwardedAt(LocalDateTime awardedAt) {
        this.awardedAt = awardedAt;
    }
}
