package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoteSaveRepository extends JpaRepository<NoteSave, Long> {
    List<NoteSave> findByUserId(Long userId);
    List<NoteSave> findByNoteId(Long noteId);
    NoteSave findByNoteIdAndUserId(Long noteId, Long userId);
    List<NoteSave> findByCreatedAtAfter(LocalDateTime localDateTime);
}
