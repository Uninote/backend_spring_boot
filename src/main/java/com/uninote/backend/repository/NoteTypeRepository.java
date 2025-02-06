package com.uninote.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.entity.NoteType;
import com.uninote.backend.entity.NoteTypeName;

public interface NoteTypeRepository extends JpaRepository<NoteType,Long>{

    
    @Query("SELECT DISTINCT tnn FROM NoteType nt " +
           "JOIN nt.typeNames tnn " +
           "JOIN tnn.language l " +
           "WHERE l.code = :languageCode and nt.typeId != 61")
    List<NoteTypeName> findAllByLanguageCode(@Param("languageCode") String languageCode);
    
}   
