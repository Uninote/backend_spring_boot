package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.CollectionLike;
import com.uninote.backend.entity.CollectionLikeId;


@Repository
public interface CollectionLikeRepository extends JpaRepository<CollectionLike, CollectionLikeId> {
    
    CollectionLike findByIdCollectionIdAndIdUserId(Long collectionId, Long userId);
    
    @Query("SELECT COUNT(cl) FROM CollectionLike cl WHERE cl.collection.id = :collectionId AND cl.isActive = true")
    Long countActiveLikesByCollectionId(Long collectionId);


    @Query("SELECT CASE WHEN COUNT(cl) > 0 THEN TRUE ELSE FALSE END " +
           "FROM CollectionLike cl WHERE cl.collection.id = :collectionId AND cl.user.id = :userId AND cl.isActive = TRUE")
    boolean existsByCollectionIdAndUserIdAndIsActive(@Param("collectionId") Long collectionId, @Param("userId") Long userId);
    
}
