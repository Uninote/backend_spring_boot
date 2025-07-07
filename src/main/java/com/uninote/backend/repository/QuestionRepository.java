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
    "substring(q.question_text, 1, 4000), " +
    "(CASE WHEN q.is_difficult = true THEN true ELSE false END) " +
    "FROM questions q " +
    "JOIN question_report qr ON q.question_id = qr.question_id " +
    "WHERE qr.status = false", nativeQuery = true)
List<Object[]> findDistinctReportedQuestions();


    @Query(value = "SELECT DISTINCT q.question_id, q.course_id, q.question_type_id, " +
        "substring(q.question_text, 1, 4000), " +
        "(CASE WHEN q.is_difficult = true THEN true ELSE false END), " +
        "substring(f.answer, 1, 4000) " +  
        "FROM questions q " +
        "JOIN question_report qr ON q.question_id = qr.question_id " +
        "JOIN flashcards f ON q.question_id = f.question_id " +  
        "WHERE qr.status = false AND q.question_type_id = 1", nativeQuery = true)
    List<Object[]> findReportedFlashcards() ;

    @Query(value = "SELECT DISTINCT q.question_id, q.course_id, q.question_type_id, " +
        "substring(q.question_text, 1, 4000), " +
        "(CASE WHEN q.is_difficult = true THEN true ELSE false END), " +
        "tf.correct_answer " +  
        "FROM questions q " +
        "JOIN question_report qr ON q.question_id = qr.question_id " +
        "JOIN true_false_questions tf ON q.question_id = tf.question_id " +
        "WHERE qr.status = false AND q.question_type_id = 2", nativeQuery = true)
    List<Object[]> findReportedTrueFalseQuestions();

    





}
