package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteCollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteCollectionItemRepository extends JpaRepository<NoteCollectionItem, Long> {
    List<NoteCollectionItem> findByCollectionId(Long collectionId);

    @Query("SELECT COUNT(n) FROM NoteCollectionItem n WHERE n.collectionId = :collectionId")
    Long countNotesInCollection(@Param("collectionId") Long collectionId);
}
