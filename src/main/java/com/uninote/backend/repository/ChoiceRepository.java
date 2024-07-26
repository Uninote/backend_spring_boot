package com.uninote.backend.repository;

import com.uninote.backend.entity.Choice;
import com.uninote.backend.entity.MultipleChoiceQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {

    
    List<Choice> findByMultipleChoiceQuestion(MultipleChoiceQuestion multipleChoiceQuestion);

    
    @Query("SELECT c FROM Choice c JOIN c.multipleChoiceQuestion mcq JOIN mcq.question q JOIN q.course cr WHERE cr.id = :courseId")
    List<Choice> findByCourseId(@Param("courseId") Long courseId);
}
