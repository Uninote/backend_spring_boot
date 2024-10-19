package com.uninote.backend.entity;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "badge_types", schema = "ADMIN")
public class BadgeType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_type_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "type")
    private Set<Badge> badges;

    

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

    public Set<Badge> getBadges() {
        return badges;
    }

    public void setBadges(Set<Badge> badges) {
        this.badges = badges;
    }
}
