package com.uninote.backend.entity;
import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

@Embeddable
public class CollectionSaveId implements Serializable {

    private Long collectionId;
    private Long userId;

   
    public CollectionSaveId() {}

    public CollectionSaveId(Long collectionId, Long userId) {
        this.collectionId = collectionId;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    
    @Override
    public int hashCode() {
        return Objects.hash(collectionId, userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectionSaveId that = (CollectionSaveId) o;
        return Objects.equals(collectionId, that.collectionId) &&
               Objects.equals(userId, that.userId);
    }
}
