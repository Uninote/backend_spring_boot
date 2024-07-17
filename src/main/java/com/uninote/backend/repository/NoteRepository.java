package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.uninote.backend.entity.Note;


@Repository
public interface NoteRepository extends JpaRepository<Note,Long>{
    
}
