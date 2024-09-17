package com.uninote.backend.repository;

import com.uninote.backend.dto.QuestionDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCourse(Course course);


    @Query("SELECT DISTINCT qt.id, qt.typeName FROM Question q JOIN q.questionType qt WHERE q.course.id = :courseId")
    List<Object[]> findDistinctQuestionTypeIdsAndNamesByCourseId(Long courseId);

    @Query(value = "SELECT DISTINCT q.question_id, q.course_id AS courseId, q.question_type_id AS questionTypeId, DBMS_LOB.SUBSTR(q.question_text, 4000, 1) AS questionText, q.is_difficult AS isDifficult " +
    "FROM admin.Questions q JOIN admin.Question_Report qr ON q.question_id = qr.question_id " +
    "WHERE qr.status = 0", nativeQuery = true)
List<QuestionDTO> findDistinctReportedQuestionDTOs();
}
