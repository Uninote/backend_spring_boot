package com.uninote.backend.repository;

import com.uninote.backend.dto.FlashcardDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Flashcard;
import com.uninote.backend.interfaceProjection.FlashcardProjection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    @Query("SELECT f FROM Flashcard f JOIN f.question q JOIN q.course c WHERE c = :course")
    List<Flashcard> findByQuestionCourse(@Param("course") Course course);

    @Query("SELECT f FROM Flashcard f JOIN f.question q JOIN q.course c WHERE c = :course")
    List<Flashcard> findByCourse(@Param("course") Course course);

    @Query(value = "SELECT f.question_id AS id, " +
               "q.course_id AS courseId, " +
               "q.question_type_id AS questionTypeId, " +
               "substring(q.question_text, 1, 4000) AS questionText, " +  // Extract up to 4000 characters from question_text
               "q.is_difficult AS isDifficultRaw, " +
               "substring(f.answer, 1, 4000) AS answer " +  // Extract up to 4000 characters from answer
               "FROM flashcards f " +
               "JOIN questions q ON f.question_id = q.question_id " +
               "WHERE q.course_id = :courseId " +
               "ORDER BY RANDOM() " +
               "LIMIT :limit",
       nativeQuery = true)
List<FlashcardProjection> findRandomFlashcardsByCourseId(@Param("courseId") Long courseId, @Param("limit") int limit);

Optional<Flashcard> findByQuestion_Id(Long questionId);
}
