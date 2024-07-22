package com.uninote.backend.repository;

import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    @Query("SELECT f FROM Flashcard f JOIN f.question q JOIN q.course c WHERE c = :course")
    List<Flashcard> findByQuestionCourseId(@Param("course") Course course);
}
