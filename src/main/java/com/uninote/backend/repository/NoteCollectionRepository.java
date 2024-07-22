package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteCollection;
import com.uninote.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteCollectionRepository extends JpaRepository<NoteCollection, Long> {
    List<NoteCollection> findByAdmin(User admin);
}
