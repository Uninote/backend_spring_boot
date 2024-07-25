package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteClick;
import com.uninote.backend.entity.NoteLike;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

public interface NoteClickRepository extends JpaRepository<NoteClick, Long> {
    Optional<NoteClick> findByNoteIdAndUserId(Long noteId, Long userId);
    List<NoteClick> findByNoteId(Long noteId);
    List<NoteClick> findByUserId(Long userId);

    List<NoteClick> findByCreatedAtAfter(LocalDateTime localDateTime);
}
