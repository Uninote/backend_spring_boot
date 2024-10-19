package com.uninote.backend.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="collection_likes", schema = "ADMIN")
public class CollectionLike {

    @EmbeddedId
    private CollectionLikeId id;

    @ManyToOne
    @MapsId("collectionId")
    @JoinColumn(name = "collection_id", insertable = false, updatable = false)
    private NoteCollection collection;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "liked_at",nullable = false)
    private LocalDateTime likedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public CollectionLike(){}
    

    public CollectionLike(NoteCollection collection, User user) {
        this.id = new CollectionLikeId(collection.getCollectionId(), user.getId());
        this.collection = collection;
        this.user = user;
        this.likedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.isActive = true; 
    }

    public CollectionLikeId getId() {
        return id;
    }

    public void setId(CollectionLikeId id) {
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

    public LocalDateTime getLikedAt() {
        return likedAt;
    }

    public void setLikedAt(LocalDateTime likedAt) {
        this.likedAt = likedAt;
    }
        
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}

