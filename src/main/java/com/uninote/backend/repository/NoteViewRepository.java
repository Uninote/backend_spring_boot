package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NoteViewRepository extends JpaRepository<NoteView, Long> {
    Optional<NoteView> findByNoteIdAndUserId(Long noteId, Long userId);
    List<NoteView> findByNoteId(Long noteId);
    List<NoteView> findByUserId(Long userId);

    @Query("SELECT v.id, COUNT(v.id) as viewCount FROM NoteView v GROUP BY v.id ORDER BY viewCount DESC")
    List<Object []> findTop10ByOrderByViewCountDesc();

    List<NoteView> findByCreatedAtAfter(LocalDateTime localDateTime);

    
}
