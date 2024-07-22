package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteCollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteCollectionItemRepository extends JpaRepository<NoteCollectionItem, Long> {
    List<NoteCollectionItem> findByCollectionId(Long collectionId);
}
