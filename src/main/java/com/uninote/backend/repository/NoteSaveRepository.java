package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteSaveRepository extends JpaRepository<NoteSave, Long> {
    List<NoteSave> findByUserId(Long userId);
    List<NoteSave> findByNoteId(Long noteId);
    Optional<NoteSave> findByNoteIdAndUserId(Long noteId, Long userId);
    List<NoteSave> findByCreatedAtAfter(LocalDateTime localDateTime);
    @Query("SELECT CASE WHEN COUNT(ns) > 0 THEN TRUE ELSE FALSE END " +
           "FROM NoteSave ns WHERE ns.note.id = :noteId AND ns.user.id = :userId AND ns.isActive = TRUE")
    boolean existsByNoteIdAndUserIdAndIsActive(@Param("noteId") Long noteId, @Param("userId") Long userId);
    @Query("SELECT ns FROM NoteSave ns JOIN ns.note n WHERE ns.user.id = :userId AND n.isPublic = true")
    List<NoteSave> findPublicSavedNotesByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE NoteSave ns SET ns.isActive = false WHERE ns.user.id = :userId")
    void setInactiveByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM NoteSave s WHERE s.userId = :userId AND s.noteId = :noteId")
    int countSavesByUserAndNote(@Param("userId") Long userId, @Param("noteId") Long noteId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM NoteSave s WHERE s.userId = :userId AND s.noteId = :noteId AND s.isActive = true")
    boolean existsByUserIdAndNoteId(@Param("userId") Long userId, @Param("noteId") Long noteId);

    List<NoteSave> findAllByIsActiveTrue();


}
