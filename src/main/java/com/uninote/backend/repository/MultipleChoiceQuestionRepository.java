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


    @Query(value = "SELECT mc.multiple_choice_id AS id, " +
               "mc.question_id AS questionId, " +
               "q.question_type_id AS questionTypeId, " +
               "DBMS_LOB.SUBSTR(q.question_text, 4000, 1) AS questionText, " +  // Extract up to 4000 characters from question_text
               "q.is_difficult AS isDifficultRaw, " +
               "mc.correct_choice_id AS correctChoiceId, " +
               "c.id AS choiceId, " +
               "DBMS_LOB.SUBSTR(c.choice_text, 4000, 1) AS choiceText, " +
               "c.choice_label AS choiceLabel, " +
               "mc.image_url AS imageUrl " +
               "FROM multiple_choice_questions mc " +
               "JOIN questions q ON mc.question_id = q.question_id " +
               "JOIN choices c ON mc.multiple_choice_id = c.multiple_choice_id " +
               "WHERE q.course_id = :courseId " +
               "ORDER BY DBMS_RANDOM.VALUE " +
               "FETCH FIRST :limit ROWS ONLY",
       nativeQuery = true)
List<Object[]> findRandomMultipleChoiceQuestionsWithChoicesByCourseId(@Param("courseId") Long courseId, @Param("limit") int limit);

}
