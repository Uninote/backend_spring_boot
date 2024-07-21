package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoteViewRepository extends JpaRepository<NoteView, Long> {
    Optional<NoteView> findByNoteIdAndUserId(Long noteId, Long userId);
}
