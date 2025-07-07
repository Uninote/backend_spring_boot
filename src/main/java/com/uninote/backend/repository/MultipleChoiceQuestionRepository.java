package com.uninote.backend.repository;

import com.uninote.backend.entity.MultipleChoiceQuestion;
import com.uninote.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MultipleChoiceQuestionRepository extends JpaRepository<MultipleChoiceQuestion, Long> {

    
    List<MultipleChoiceQuestion> findByQuestion(Question question);

    
    @Query("SELECT mcq FROM MultipleChoiceQuestion mcq JOIN mcq.question q JOIN q.course c WHERE c.id = :courseId")
    List<MultipleChoiceQuestion> findByCourseId(@Param("courseId") Long courseId);


    @Query(value = "SELECT mc.multiple_choice_id AS id, " +
               "mc.question_id AS questionId, " +
               "q.question_type_id AS questionTypeId, " +
               "substring(q.question_text, 1, 4000) AS questionText, " +
               "q.is_difficult AS isDifficultRaw, " +
               "mc.correct_choice_id AS correctChoiceId, " +
               "c.id AS choiceId, " +
               "substring(c.choice_text, 1, 4000) AS choiceText, " +
               "c.choice_label AS choiceLabel, " +
               "mc.image_url AS imageUrl " +
               "FROM multiple_choice_questions mc " +
               "JOIN questions q ON mc.question_id = q.question_id " +
               "JOIN choices c ON mc.multiple_choice_id = c.multiple_choice_id " +
               "WHERE q.course_id = :courseId " +
               "AND q.question_type_id = 3 " +
               "AND mc.question_id IN ( " +
               "    SELECT mc1.question_id " +
               "    FROM multiple_choice_questions mc1 " +
               "    JOIN questions q1 ON mc1.question_id = q1.question_id " +
               "    WHERE q1.course_id = :courseId " +
               "    AND q1.question_type_id = 3 " +
               "    ORDER BY RANDOM() " +
               "    LIMIT :limit" +
               ")",
       nativeQuery = true)
List<Object[]> findRandomMultipleChoiceQuestionsWithChoicesByCourseId(@Param("courseId") Long courseId, @Param("limit") int limit);


@Query(value = "SELECT mc.multiple_choice_id AS id, " +
               "mc.question_id AS questionId, " +
               "q.question_type_id AS questionTypeId, " +
               "substring(q.question_text, 1, 4000) AS questionText, " +
               "q.is_difficult AS isDifficultRaw, " +
               "mc.correct_choice_id AS correctChoiceId, " +
               "c.id AS choiceId, " +
               "substring(c.choice_text, 1, 4000) AS choiceText, " +
               "c.choice_label AS choiceLabel, " +
               "mc.image_url AS imageUrl " +
               "FROM multiple_choice_questions mc " +
               "JOIN questions q ON mc.question_id = q.question_id " +
               "JOIN choices c ON mc.multiple_choice_id = c.multiple_choice_id " +
               "JOIN question_report qr ON q.question_id = qr.question_id " +  
               "WHERE q.question_type_id = 3 " +  
               "AND qr.status = false",  
       nativeQuery = true)
List<Object[]> findReportedMultipleChoiceQuestionsWithChoices();


Optional<MultipleChoiceQuestion> findByQuestion_Id(Long questionId);
}
