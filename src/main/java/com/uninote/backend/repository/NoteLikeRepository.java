package com.uninote.backend.repository;

import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteLikeId;
import com.uninote.backend.entity.NoteSave;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteLikeRepository extends JpaRepository<NoteLike, NoteLikeId> {
    
    @Query("SELECT CASE WHEN COUNT(nl) > 0 THEN TRUE ELSE FALSE END " +
           "FROM NoteLike nl WHERE nl.note.id = :noteId AND nl.user.id = :userId AND nl.isActive = TRUE")
    boolean existsByNoteIdAndUserIdAndIsActive(@Param("noteId") Long noteId, @Param("userId") Long userId);
    long countByNote(Note note);
    Optional<NoteLike> findByNoteIdAndUserId(Long noteId, Long userId);
    List<NoteLike> findByNoteId(Long noteId);
    List<NoteLike> findByUserId(Long userId);
    List<NoteLike> findByCreatedAtAfter(LocalDateTime localDateTime);

    @Modifying
    @Query("UPDATE NoteLike nl SET nl.isActive = false WHERE nl.user.id = :userId")
    void setInactiveByUserId(@Param("userId") Long userId);

    @Query("SELECT nl.userId FROM NoteView nl WHERE nl.user.id = :userId")
    Set<Long> findNotesLikedByUser(Long userId);


    @Query("SELECT COUNT(l) FROM NoteLike l WHERE l.user.id = :userId AND l.note.id = :noteId")
    int countLikesByUserAndNote(Long userId, Long noteId);

    @Query("SELECT COUNT(l) " +
       "FROM NoteLike l " +
       "WHERE l.user.id = :userId AND l.note.id = :noteId AND l.isActive = true")
    int countByUserIdAndNoteId(@Param("userId") Long userId, @Param("noteId") Long noteId);

    List<NoteLike> findAllByIsActiveTrue();

    
}
