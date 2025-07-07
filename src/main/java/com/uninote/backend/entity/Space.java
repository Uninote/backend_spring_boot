package com.uninote.backend.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "SPACES")
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SPACE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION", columnDefinition = "text")
    private String description;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;

    @Column(name = "UPDATED_AT")
    private Timestamp updatedAt;

    @Column(name = "UUID")
    private String uuid;

    // Optional: Bidirectional relationship with space chats
    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SpaceChat> spaceChats = new ArrayList<SpaceChat>();

    // Optional: relationship with resources (via join table)
    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SpaceResource> spaceResources;

    // Constructors
    public Space() {}

    // Getters and Setters
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<SpaceChat> getSpaceChats() {
        return spaceChats;
    }

    public void setSpaceChats(List<SpaceChat> spaceChats) {
        this.spaceChats = spaceChats;
    }

    public List<SpaceResource> getSpaceResources() {
        return spaceResources;
    }

    public void setSpaceResources(List<SpaceResource> spaceResources) {
        this.spaceResources = spaceResources;
    }

    @Override
    public String toString() {
        return "Space{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", user=" + (user != null ? user.getId() : null) +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
