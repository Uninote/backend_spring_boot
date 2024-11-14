package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.UserNoteMatrixEntry;
import com.uninote.backend.entity.UserNoteMatrixEntryId;

public interface UserNoteMatrixRepository extends JpaRepository<UserNoteMatrixEntry, UserNoteMatrixEntryId> {

    
}
