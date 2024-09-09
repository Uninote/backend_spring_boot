package com.uninote.backend.repository;

import com.uninote.backend.dto.TrueFalseQuestionDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.TrueFalseQuestion;
import com.uninote.backend.interfaceProjection.TrueFalseQuestionProjection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrueFalseQuestionRepository extends JpaRepository<TrueFalseQuestion, Long> {

    @Query("SELECT tfq FROM TrueFalseQuestion tfq JOIN tfq.question q JOIN q.course c WHERE c = :course")
    List<TrueFalseQuestion> findByQuestionCourse(@Param("course") Course course);

    @Query("SELECT tf FROM TrueFalseQuestion tf JOIN tf.question q JOIN q.course c WHERE c = :course")
    List<TrueFalseQuestion> findByCourse(@Param("course") Course course);

     @Query(value = "SELECT t.question_id AS id, q.course_id AS courseId, q.question_type_id AS questionTypeId, DBMS_LOB.SUBSTR(q.question_text, 4000, 1) AS questionText, q.is_difficult AS isDifficult, t.correct_answer AS correctAnswerRaw, t.image_url AS imageUrl " +
                   "FROM true_false_questions t " +
                   "JOIN questions q ON t.question_id = q.question_id " +
                   "WHERE q.course_id = :courseId " +
                   "ORDER BY DBMS_RANDOM.VALUE " +
                   "FETCH FIRST :limit ROWS ONLY",
           nativeQuery = true)
    List<TrueFalseQuestionProjection> findRandomTrueFalseQuestionsByCourseId(@Param("courseId") Long courseId, @Param("limit") int limit);
    }



