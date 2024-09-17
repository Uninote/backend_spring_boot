package com.uninote.backend.repository;

import com.uninote.backend.entity.Choice;
import com.uninote.backend.entity.MultipleChoiceQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {

    
    List<Choice> findByMultipleChoiceQuestion(MultipleChoiceQuestion multipleChoiceQuestion);

    
    @Query("SELECT c FROM Choice c JOIN c.multipleChoiceQuestion mcq JOIN mcq.question q JOIN q.course cr WHERE cr.id = :courseId")
    List<Choice> findByCourseId(@Param("courseId") Long courseId);
    Optional<Choice> findByMultipleChoiceQuestionAndChoiceLabel(MultipleChoiceQuestion multipleChoiceQuestion, int choiceLabel);
    
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "INSERT INTO admin.choices (choice_label, choice_text, multiple_choice_id) VALUES (?1, ?2, ?3)")
    int batchInsertChoices(int choiceLabel, String choiceText, Long multipleChoiceId);

}
