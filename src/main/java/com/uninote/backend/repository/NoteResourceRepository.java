package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteResourceRepository extends JpaRepository<NoteResource, Long> {
}
