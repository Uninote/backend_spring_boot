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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserCourseGradeService {

    @Autowired
    private UserCourseGradeRepository userCourseGradeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    
    
    public UserCourseGrade assignGradeToUser(Long userId, Long courseId, Double grade) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));
        if (userCourseGradeRepository.existsByCourseIdAdnUserId(userId, courseId).compareTo(BigDecimal.ZERO) > 0 ) {
            return null;
        }
        UserCourseGrade userCourseGrade = new UserCourseGrade();
        userCourseGrade.setUserId(userId);
        userCourseGrade.setCourseId(courseId);
        userCourseGrade.setGrade(grade);
        userCourseGrade.setDateAssigned(LocalDate.now());

        UserCourseGrade usg  = userCourseGradeRepository.save(userCourseGrade);
        userService.ceritfyUser(userId);
        return usg;
    }

   
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
