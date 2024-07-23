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

    @Column(name = "image_url")
    private String imageUrl;

   
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

   
}
