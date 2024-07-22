package com.uninote.backend.repository;

import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.TrueFalseQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrueFalseQuestionRepository extends JpaRepository<TrueFalseQuestion, Long> {

    @Query("SELECT tfq FROM TrueFalseQuestion tfq JOIN tfq.question q JOIN q.course c WHERE c = :course")
    List<TrueFalseQuestion> findByQuestionCourse(@Param("course") Course course);
}
