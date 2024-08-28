package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteCollection;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaceProjection.CollectionProjection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteCollectionRepository extends JpaRepository<NoteCollection, Long> {
    List<NoteCollection> findByAdmin(User admin);
    List<NoteCollection> findByIsPublicTrue();

    @Query(value = "SELECT c.collection_id AS collectionId, c.name AS name, " +
       "(CASE WHEN c.is_public = 1 THEN 1 ELSE 0 END) AS isPublicRaw, " +  // Use CASE statement for boolean conversion
       "u.username AS adminUsername, " +
       "(SELECT COUNT(cl.collection_id) FROM collection_likes cl WHERE cl.collection_id = c.collection_id AND cl.is_active = 1) AS totalLikes, " +
       "(SELECT COUNT(nci.collection_id) FROM note_collection_items nci WHERE nci.collection_id = c.collection_id) AS noteNum, " +
       "DBMS_LOB.SUBSTR(c.description, 4000, 1) AS description " +
       "FROM note_collections c " +
       "JOIN users u ON c.admin_id = u.user_id " +
       "WHERE c.is_public = 1 AND c.deleted = 0", nativeQuery = true)
List<CollectionProjection> findPublicCollections();

@Query(value = "SELECT c.collection_id AS collectionId, c.name AS name, " +
        "(CASE WHEN c.is_public = 1 THEN 1 ELSE 0 END) AS isPublicRaw, " +  // Use CASE statement for boolean conversion
        "u.username AS adminUsername, " +
        "(SELECT COUNT(cl.collection_id) FROM collection_likes cl WHERE cl.collection_id = c.collection_id AND cl.is_active = 1) AS totalLikes, " +
        "(SELECT COUNT(nci.collection_id) FROM note_collection_items nci WHERE nci.collection_id = c.collection_id) AS noteNum, " +
        "DBMS_LOB.SUBSTR(c.description, 4000, 1) AS description " +
        "FROM note_collections c " +
        "JOIN users u ON c.admin_id = u.user_id " +
        "WHERE c.collection_id = :collectionId", nativeQuery = true)
CollectionProjection findCollectionProjectionById(@Param("collectionId") Long collectionId);









    @Query("SELECT c.collectionId AS collectionId, c.name AS name, c.description AS description, " +
            "c.isPublic AS isPublic, c.admin.username AS adminUsername, " +
            "(SELECT COUNT(cl) FROM CollectionLike cl WHERE cl.collection.collectionId = c.collectionId AND isActive =True) AS totalLikes, " +
            "(SELECT COUNT(nci) FROM NoteCollectionItem nci WHERE nci.collectionId = c.collectionId) AS noteNum " +
            "FROM NoteCollection c " +
            "WHERE c.admin.id = :userId AND c.deleted = false")
    List<CollectionProjection> findCollectionsByUser(Long userId);
}
