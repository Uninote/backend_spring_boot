package com.uninote.backend.repository;

import com.uninote.backend.dto.TrueFalseQuestionDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.TrueFalseQuestion;
import com.uninote.backend.interfaceProjection.TrueFalseQuestionProjection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrueFalseQuestionRepository extends JpaRepository<TrueFalseQuestion, Long> {

    @Query("SELECT tfq FROM TrueFalseQuestion tfq JOIN tfq.question q JOIN q.course c WHERE c = :course")
    List<TrueFalseQuestion> findByQuestionCourse(@Param("course") Course course);

    @Query("SELECT tf FROM TrueFalseQuestion tf JOIN tf.question q JOIN q.course c WHERE c = :course")
    List<TrueFalseQuestion> findByCourse(@Param("course") Course course);

     @Query(value = "SELECT t.question_id AS id, q.course_id AS courseId, q.question_type_id AS questionTypeId, substring(q.question_text, 1, 4000) AS questionText, q.is_difficult AS isDifficult, t.correct_answer AS correctAnswerRaw, t.image_url AS imageUrl " +
                   "FROM true_false_questions t " +
                   "JOIN questions q ON t.question_id = q.question_id " +
                   "WHERE q.course_id = :courseId " +
                   "ORDER BY RANDOM() " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<TrueFalseQuestionProjection> findRandomTrueFalseQuestionsByCourseId(@Param("courseId") Long courseId, @Param("limit") int limit);
    Optional<TrueFalseQuestion> findByQuestion_Id(Long questionId);    
}



