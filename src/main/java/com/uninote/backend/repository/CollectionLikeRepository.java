package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.CollectionLike;
import com.uninote.backend.entity.CollectionLikeId;


@Repository
public interface CollectionLikeRepository extends JpaRepository<CollectionLike, CollectionLikeId> {
    
    CollectionLike findByIdCollectionIdAndIdUserId(Long collectionId, Long userId);
    
    @Query("SELECT COUNT(cl) FROM CollectionLike cl WHERE cl.collection.id = :collectionId AND cl.isActive = true")
    Long countActiveLikesByCollectionId(Long collectionId);
    
}
