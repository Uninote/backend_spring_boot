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

    @Lob
    @Column(name = "badge_description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "requirement")
    private int requirement;

    @OneToMany(mappedBy = "badge")
    private List<UserBadge> userBadges;

    @ManyToOne
    @JoinColumn(name = "badge_type_id", nullable = false)
    private BadgeType type;
   

    
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

    public int getRequirement() {
        return requirement;
    }

    public void setRequirement(int requirement) {
        this.requirement = requirement;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<UserBadge> getUserBadges() {
        return userBadges;
    }

    public void setUserBadges(List<UserBadge> userBadges) {
        this.userBadges = userBadges;
    }

    public BadgeType getType() {
        return type;
    }

    public void setType(BadgeType type) {
        this.type = type;
    }   

}
