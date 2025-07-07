package com.uninote.backend.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "collection_saves" )
public class CollectionSave {

    @EmbeddedId
    private CollectionSaveId id;

    @ManyToOne
    @MapsId("collectionId")
    @JoinColumn(name = "collection_id", nullable = false)
    private NoteCollection collection;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name="saved_at",nullable = false)
    private LocalDateTime savedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public CollectionSave() {}

    public CollectionSave(NoteCollection collection, User user) {
        this.id = new CollectionSaveId(collection.getCollectionId(), user.getId());
        this.collection = collection;
        this.user = user;
        this.savedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.isActive = true; 
    }
    
    public CollectionSaveId getId() {
        return id;
    }

    public void setId(CollectionSaveId id) {
        this.id = id;
    }

    public NoteCollection getCollection() {
        return collection;
    }

    public void setCollection(NoteCollection collection) {
        this.collection = collection;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
