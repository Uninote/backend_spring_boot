package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteClick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface NoteClickRepository extends JpaRepository<NoteClick, Long> {
    Optional<NoteClick> findByNoteIdAndUserId(Long noteId, Long userId);
    List<NoteClick> findByNoteId(Long noteId);
    List<NoteClick> findByUserId(Long userId);
}
