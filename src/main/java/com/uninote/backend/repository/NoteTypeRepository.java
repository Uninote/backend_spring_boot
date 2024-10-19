package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.NoteType;

public interface NoteTypeRepository extends JpaRepository<NoteType,Long>{

    

    
}
