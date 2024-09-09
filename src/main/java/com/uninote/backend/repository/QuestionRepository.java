package com.uninote.backend.repository;

import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCourse(Course course);


    @Query("SELECT DISTINCT qt.id, qt.typeName FROM Question q JOIN q.questionType qt WHERE q.course.id = :courseId")
    List<Object[]> findDistinctQuestionTypeIdsAndNamesByCourseId(Long courseId);

    
}
