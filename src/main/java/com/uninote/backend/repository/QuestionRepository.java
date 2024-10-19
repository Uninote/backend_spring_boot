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

    @Query(value = "SELECT DISTINCT q.question_id, q.course_id, q.question_type_id, " +
    "DBMS_LOB.SUBSTR(q.question_text, 4000, 1), " +
    "(CASE WHEN q.is_difficult = 1 THEN 1 ELSE 0 END) " +
    "FROM admin.Questions q " +
    "JOIN admin.Question_Report qr ON q.question_id = qr.question_id " +
    "WHERE qr.status = 0", nativeQuery = true)
List<Object[]> findDistinctReportedQuestions();


    @Query(value = "SELECT DISTINCT q.question_id, q.course_id, q.question_type_id, " +
        "DBMS_LOB.SUBSTR(q.question_text, 4000, 1), " +
        "(CASE WHEN q.is_difficult = 1 THEN 1 ELSE 0 END), " +
        "DBMS_LOB.SUBSTR(f.answer, 4000, 1) " +  
        "FROM admin.Questions q " +
        "JOIN admin.Question_Report qr ON q.question_id = qr.question_id " +
        "JOIN admin.Flashcards f ON q.question_id = f.question_id " +  
        "WHERE qr.status = 0 AND q.question_type_id = 1", nativeQuery = true)
    List<Object[]> findReportedFlashcards() ;

    @Query(value = "SELECT DISTINCT q.question_id, q.course_id, q.question_type_id, " +
        "DBMS_LOB.SUBSTR(q.question_text, 4000, 1), " +
        "(CASE WHEN q.is_difficult = 1 THEN 1 ELSE 0 END), " +
        "tf.correct_answer " +  
        "FROM admin.Questions q " +
        "JOIN admin.Question_Report qr ON q.question_id = qr.question_id " +
        "JOIN admin.True_False_Questions tf ON q.question_id = tf.question_id " +  
        "WHERE qr.status = 0 AND q.question_type_id = 2", nativeQuery = true)
    List<Object[]> findReportedTrueFalseQuestions();

    





}
