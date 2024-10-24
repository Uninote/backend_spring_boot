package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.UserCourseGrade;

import java.util.List;
import java.util.Optional;

public interface UserCourseGradeRepository extends JpaRepository<UserCourseGrade, Long> {
    
    List<UserCourseGrade> findByUserId(Long userId);
    
    List<UserCourseGrade> findByCourseId(Long courseId);

    Optional<UserCourseGrade> findByUserIdAndCourseId(Long userId, Long courseId);

}
