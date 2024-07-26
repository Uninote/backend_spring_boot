package com.uninote.backend.repository;

import com.uninote.backend.entity.MultipleChoiceQuestion;
import com.uninote.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MultipleChoiceQuestionRepository extends JpaRepository<MultipleChoiceQuestion, Long> {

    
    List<MultipleChoiceQuestion> findByQuestion(Question question);

    
    @Query("SELECT mcq FROM MultipleChoiceQuestion mcq JOIN mcq.question q JOIN q.course c WHERE c.id = :courseId")
    List<MultipleChoiceQuestion> findByCourseId(@Param("courseId") Long courseId);
}
