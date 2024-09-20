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
               "DBMS_LOB.SUBSTR(q.question_text, 4000, 1) AS questionText, " +
               "q.is_difficult AS isDifficultRaw, " +
               "mc.correct_choice_id AS correctChoiceId, " +
               "c.id AS choiceId, " +
               "DBMS_LOB.SUBSTR(c.choice_text, 4000, 1) AS choiceText, " +
               "c.choice_label AS choiceLabel, " +
               "mc.image_url AS imageUrl " +
               "FROM admin.multiple_choice_questions mc " +
               "JOIN admin.questions q ON mc.question_id = q.question_id " +
               "JOIN admin.choices c ON mc.multiple_choice_id = c.multiple_choice_id " +
               "WHERE q.course_id = :courseId " +
               "AND q.question_type_id = 3 " +
               "AND mc.question_id IN ( " +
               "    SELECT mc1.question_id " +
               "    FROM admin.multiple_choice_questions mc1 " +
               "    JOIN admin.questions q1 ON mc1.question_id = q1.question_id " +
               "    WHERE q1.course_id = :courseId " +
               "    AND q1.question_type_id = 3 " +
               "    ORDER BY DBMS_RANDOM.VALUE " +
               "    FETCH FIRST :limit ROWS ONLY" +
               ")",
       nativeQuery = true)
List<Object[]> findRandomMultipleChoiceQuestionsWithChoicesByCourseId(@Param("courseId") Long courseId, @Param("limit") int limit);


@Query(value = "SELECT mc.multiple_choice_id AS id, " +
               "mc.question_id AS questionId, " +
               "q.question_type_id AS questionTypeId, " +
               "DBMS_LOB.SUBSTR(q.question_text, 4000, 1) AS questionText, " +
               "q.is_difficult AS isDifficultRaw, " +
               "mc.correct_choice_id AS correctChoiceId, " +
               "c.id AS choiceId, " +
               "DBMS_LOB.SUBSTR(c.choice_text, 4000, 1) AS choiceText, " +
               "c.choice_label AS choiceLabel, " +
               "mc.image_url AS imageUrl " +
               "FROM admin.multiple_choice_questions mc " +
               "JOIN admin.questions q ON mc.question_id = q.question_id " +
               "JOIN admin.choices c ON mc.multiple_choice_id = c.multiple_choice_id " +
               "JOIN admin.question_report qr ON q.question_id = qr.question_id " +  
               "WHERE q.question_type_id = 3 " +  
               "AND qr.status = 0",  
       nativeQuery = true)
List<Object[]> findReportedMultipleChoiceQuestionsWithChoices();


Optional<MultipleChoiceQuestion> findByQuestion_Id(Long questionId);
}
