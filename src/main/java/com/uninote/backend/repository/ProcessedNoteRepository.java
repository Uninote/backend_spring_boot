package com.uninote.backend.repository;

import com.uninote.backend.entity.ProcessedNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedNoteRepository extends JpaRepository<ProcessedNote, Long> {

    ProcessedNote findByNoteId(Long noteId);

}
