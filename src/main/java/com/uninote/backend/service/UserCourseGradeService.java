package com.uninote.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserCourseGrade;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.UserCourseGradeRepository;
import com.uninote.backend.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserCourseGradeService {

    @Autowired
    private UserCourseGradeRepository userCourseGradeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    
    public UserCourseGrade assignGradeToUser(Long userId, Long courseId, Double grade) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

        UserCourseGrade userCourseGrade = new UserCourseGrade();
        userCourseGrade.setUser(user);
        userCourseGrade.setCourse(course);
        userCourseGrade.setGrade(grade);
        userCourseGrade.setDateAssigned(LocalDate.now());

        return userCourseGradeRepository.save(userCourseGrade);
    }

    /**
     * Get all grades for a specific user
     * 
     * @param userId  The ID of the user
     * @return List<UserCourseGrade>  List of grades for the user
     */
    public List<UserCourseGrade> getGradesForUser(Long userId) {
        return userCourseGradeRepository.findByUserId(userId);
    }

    
    public List<UserCourseGrade> getGradesForCourse(Long courseId) {
        return userCourseGradeRepository.findByCourseId(courseId);
    }

    
    public UserCourseGrade updateGrade(Long gradeId, Double newGrade) {
        UserCourseGrade userCourseGrade = userCourseGradeRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with ID: " + gradeId));
        userCourseGrade.setGrade(newGrade);
        return userCourseGradeRepository.save(userCourseGrade);
    }

    
    public void deleteGrade(Long gradeId) {
        userCourseGradeRepository.deleteById(gradeId);
    }

    public Optional<UserCourseGrade> getUserGradeForCourse(Long userId, Long courseId) {
        return userCourseGradeRepository.findByUserIdAndCourseId(userId, courseId);
    }
}
