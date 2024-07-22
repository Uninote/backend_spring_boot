package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.CollectionSave;
import com.uninote.backend.entity.CollectionSaveId;


public interface CollectionSaveRepository extends JpaRepository<CollectionSave, CollectionSaveId> {
    CollectionSave findByIdCollectionIdAndIdUserId(Long collectionId, Long userId);

}
