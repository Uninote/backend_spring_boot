package com.uninote.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.Role;

public interface ResourceRepository extends JpaRepository<Resource,Long>{
    @Transactional
    @Modifying
    @Query("UPDATE Resource r SET r.summary = :summary WHERE r.id = :id")
    void updateSummaryById(@Param("id") Long id, @Param("summary") String summary);

    @Transactional
    @Modifying
    @Query("UPDATE Resource r SET r.flashcards = :flashcards WHERE r.id = :id")
    void updateFlashcardsById(@Param("id") Long id, @Param("flashcards") String flashcards);

    @Transactional
    @Modifying
    @Query("UPDATE Resource r SET r.quiz = :quiz WHERE r.id = :id")
    void updateQuizById(@Param("id") Long id, @Param("quiz") String quiz);

    @Transactional
    @Modifying
    @Query("UPDATE Resource r SET r.chapters = :chapters WHERE r.id = :id")
    void updateChaptersById(@Param("id") Long id, @Param("chapters") String chapters);
}
