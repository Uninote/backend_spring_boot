package com.uninote.backend.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "badge_seq")
    @SequenceGenerator(name = "badge_seq", sequenceName = "badge_seq", allocationSize = 1)
    @Column(name = "badge_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "badge_name", nullable = false)
    private String name;

    @Column(name = "badge_description")
    private String description;

    @OneToMany(mappedBy = "badge")
    private List<UserBadge> userBadges;

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<UserBadge> getUserBadges() {
        return userBadges;
    }

    
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUserBadges(List<UserBadge> userBadges) {
        this.userBadges = userBadges;
    }
}
