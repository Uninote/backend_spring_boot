package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.entity.UserCourseGrade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


public interface UserCourseGradeRepository extends JpaRepository<UserCourseGrade, Long> {
    
    List<UserCourseGrade> findByUserId(Long userId);
    
    List<UserCourseGrade> findByCourseId(Long courseId);

    Optional<UserCourseGrade> findByUserIdAndCourseId(Long userId, Long courseId);


    @Query(value = "SELECT COUNT(*) FROM ADMIN.USER_COURSE_GRADES WHERE USER_ID = :userId", nativeQuery = true)
    BigDecimal countByUserId(@Param("userId") Long userId);


    @Query(value = "SELECT COUNT(*) FROM ADMIN.USER_COURSE_GRADES WHERE USER_ID = :userId AND COURSE_ID = :courseId", nativeQuery = true)
    BigDecimal existsByCourseIdAdnUserId(@Param("userId") Long userId, @Param("courseId") Long courseId);

}
