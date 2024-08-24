package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteCollection;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaceProjection.CollectionProjection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoteCollectionRepository extends JpaRepository<NoteCollection, Long> {
    List<NoteCollection> findByAdmin(User admin);
    List<NoteCollection> findByIsPublicTrue();

    @Query("SELECT c.collectionId AS collectionId, c.name AS name, c.description AS description, " +
            "c.isPublic AS isPublic, c.admin.username AS adminUsername, " +
            "(SELECT COUNT(cl) FROM CollectionLike cl WHERE cl.collection.collectionId = c.collectionId AND isActive =True) AS totalLikes, " +
            "(SELECT COUNT(nci) FROM NoteCollectionItem nci WHERE nci.collectionId = c.collectionId) AS noteNum " +
            "FROM NoteCollection c " +
            "WHERE c.isPublic = true")
    List<CollectionProjection> findPublicCollections();
}
