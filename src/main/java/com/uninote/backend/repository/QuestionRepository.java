package com.uninote.backend.repository;

import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCourse(Course course);

    
}
