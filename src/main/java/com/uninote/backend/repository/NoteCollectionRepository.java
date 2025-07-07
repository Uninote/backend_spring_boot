package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteCollection;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaceProjection.CollectionProjection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NoteCollectionRepository extends JpaRepository<NoteCollection, Long> {
    List<NoteCollection> findByAdmin(User admin);
    List<NoteCollection> findByIsPublicTrue();

    @Query(value = "SELECT c.collection_id AS collectionId, c.name AS name, " +
       "(CASE WHEN c.is_public = true THEN true ELSE false END) AS isPublicRaw, " +  // Use CASE statement for boolean conversion
       "u.username AS adminUsername, " +
       "(SELECT COUNT(cl.collection_id) FROM collection_likes cl WHERE cl.collection_id = c.collection_id AND cl.is_active = true) AS totalLikes, " +
       "(SELECT COUNT(nci.collection_id) FROM note_collection_items nci WHERE nci.collection_id = c.collection_id) AS noteNum, " +
       "substring(c.description, 1, 4000) AS description " +
       "FROM note_collections c " +
       "JOIN users u ON c.admin_id = u.user_id " +
       "WHERE c.is_public = true AND c.deleted = false", nativeQuery = true)
List<CollectionProjection> findPublicCollections();

@Query(value = "SELECT c.collection_id AS collectionId, c.name AS name, " +
        "(CASE WHEN c.is_public = true THEN true ELSE false END) AS isPublicRaw, " +  // Use CASE statement for boolean conversion
        "u.username AS adminUsername, " +
        "(SELECT COUNT(cl.collection_id) FROM collection_likes cl WHERE cl.collection_id = c.collection_id AND cl.is_active = true) AS totalLikes, " +
        "(SELECT COUNT(nci.collection_id) FROM note_collection_items nci WHERE nci.collection_id = c.collection_id) AS noteNum, " +
        "substring(c.description, 1, 4000) AS description " +
        "FROM note_collections c " +
        "JOIN users u ON c.admin_id = u.user_id " +
        "WHERE c.collection_id = :collectionId", nativeQuery = true)
CollectionProjection findCollectionProjectionById(@Param("collectionId") Long collectionId);


        @Modifying
    @Transactional
    @Query("UPDATE NoteCollection c SET c.deleted = true WHERE c.admin.id = :userId")
    void softDeleteCollectionsByUserId(@Param("userId") Long userId);









    @Query("SELECT c.collectionId AS collectionId, c.name AS name, c.description AS description, " +
            "c.isPublic AS isPublic, c.admin.username AS adminUsername, " +
            "(SELECT COUNT(cl) FROM CollectionLike cl WHERE cl.collection.collectionId = c.collectionId AND isActive =True) AS totalLikes, " +
            "(SELECT COUNT(nci) FROM NoteCollectionItem nci WHERE nci.collectionId = c.collectionId) AS noteNum " +
            "FROM NoteCollection c " +
            "WHERE c.admin.id = :userId AND c.deleted = false")
    List<CollectionProjection> findCollectionsByUser(Long userId);
}
