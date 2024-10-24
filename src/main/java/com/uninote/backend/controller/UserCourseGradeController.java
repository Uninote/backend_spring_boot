package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uninote.backend.entity.UserCourseGrade;
import com.uninote.backend.service.UserCourseGradeService;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/grades")
public class UserCourseGradeController {

    @Autowired
    private UserCourseGradeService userCourseGradeService;

    @PostMapping("/assign")
    public ResponseEntity<UserCourseGrade> assignGrade(
            @RequestParam Long userId,
            @RequestParam Long courseId,
            @RequestParam Double grade) {
        UserCourseGrade userCourseGrade = userCourseGradeService.assignGradeToUser(userId, courseId, grade);
        return ResponseEntity.ok(userCourseGrade);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserCourseGrade>> getGradesForUser(@PathVariable Long userId) {
        List<UserCourseGrade> grades = userCourseGradeService.getGradesForUser(userId);
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<UserCourseGrade>> getGradesForCourse(@PathVariable Long courseId) {
        List<UserCourseGrade> grades = userCourseGradeService.getGradesForCourse(courseId);
        return ResponseEntity.ok(grades);
    }

    @PutMapping("/update")
    public ResponseEntity<UserCourseGrade> updateGrade(
            @RequestParam Long gradeId,
            @RequestParam Double newGrade) {
        UserCourseGrade updatedGrade = userCourseGradeService.updateGrade(gradeId, newGrade);
        return ResponseEntity.ok(updatedGrade);
    }

    @DeleteMapping("/delete/{gradeId}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long gradeId) {
        userCourseGradeService.deleteGrade(gradeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/course/{courseId}")
    public Double getUserGradeForCourse(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        Optional<UserCourseGrade> userCourseGrade = userCourseGradeService.getUserGradeForCourse(userId, courseId);

        if(userCourseGrade.isPresent()) {
            return userCourseGrade.get().getGrade();
        } else {
            return null;
        }
    }
}
