package com.uninote.backend.repository;

import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteLikeId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    
    void deleteByUserId(Long userId);
}
