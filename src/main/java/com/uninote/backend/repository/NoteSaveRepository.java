package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteSave;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
