package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.entity.CollectionSave;
import com.uninote.backend.entity.CollectionSaveId;


public interface CollectionSaveRepository extends JpaRepository<CollectionSave, CollectionSaveId> {
    CollectionSave findByIdCollectionIdAndIdUserId(Long collectionId, Long userId);


    @Query("SELECT CASE WHEN COUNT(sl) > 0 THEN TRUE ELSE FALSE END " +
           "FROM CollectionSave sl WHERE sl.collection.id = :collectionId AND sl.user.id = :userId AND sl.isActive = TRUE")
    boolean existsByCollectionIdAndUserIdAndIsActive(@Param("collectionId") Long collectionId, @Param("userId") Long userId);
}
